package propra.imageconverter;

import java.io.BufferedInputStream;        
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Klasse FILE_MNG: kümmert sich um die Umwandlung und Verarbeitung von Daten.
 * 
 * @author Annika Nöding
 *
 */
public class FILE_MNG {

	private String fromPath;
	private String toPath;
	public static RandomAccessFile raf;
	public static FileInputStream fis;
	public static BufferedInputStream bis;
	private FileChannel is;
	private FileChannel fo;
	public static long remaining;
	private byte[] remainingBuffer = new byte[0];
	private ArrayList<Integer> indexSpeicher = new ArrayList<Integer>();
	private byte[] steuerbyteBuffer = new byte[0];
	private boolean treeLoaded = false;
	private HashMap<Byte, String> decodeDictionary;
	private StringBuilder bitString = new StringBuilder();
	
	/**
	 *HuffmanTree, der für alle Instanzen von FILE_MNG zugreifbar ist 
	 */
	public static HuffmanTree tree;

	/**
	 * Erstellt ein FILE_MNG Objekt, das sich um das Lesen und Schreiben von Strömen
	 * von fromPath nach toPath kümmert. Die Input und Output Ströme können
	 * anschließend gemeinsam über closeStream() geschlossen werden.
	 * 
	 * @param fromPath Ursprungsort der zu lesenden Datei
	 * @param toPath   Zielort der zu schreibenden Datei
	 * @throws IOException
	 */
	public FILE_MNG() throws IOException {
		this.fromPath = Path_MNG.getInput();
		this.toPath = Path_MNG.getOutput();
		raf = new RandomAccessFile(new File(this.fromPath), "r");
		fis = new FileInputStream(raf.getFD());
		bis = new BufferedInputStream(fis);
		File file = new File(toPath);
		//falls eine Datei mit gleichem Namen bereits existiert
		if (!file.createNewFile()) { 
			int i = 2;
			String filename = file.getName();
			int splitpoint = filename.lastIndexOf(".");
			String absolutPath = file.getAbsolutePath();
			int absolutpathlength = absolutPath.length();
			int filenameLength = filename.length();
			toPath = absolutPath.substring(0,absolutpathlength-filenameLength);
			filename = filename.substring(0,splitpoint)+"("+i+")"+filename.substring(splitpoint);
			toPath += filename;
			file = new File(toPath);
			while (!file.createNewFile()) {
				i++;
				toPath = absolutPath.substring(0,absolutpathlength-filenameLength);
				filename = filename.substring(0,splitpoint)+"("+i+")"+filename.substring(splitpoint);
				toPath += filename;
				file = new File(toPath);
			}
		}
		fo = new FileOutputStream(toPath).getChannel();
		remaining = fis.available();
		if (fis.available() == 0)
			throw new IOException("leere Datei.");
	}

	/**
	 * liest eine angegebene Menge Bytes aus seinem gespeicherten Channel aus.
	 * Beginnend bei der gespeicherten Position. Ist die Anzahl der Bytes, die
	 * gelesen werden sollen, gr��er als die verbleibende Menge der zu lesenden
	 * Bytes, wird wird ein entsprechend kleineres Byte Array ausgegeben.
	 * 
	 * @param length Menge der Byte, die ausgelesen werden sollen
	 * @return byte[] gibt die gelesenen Bytes als Array aus.
	 * @throws IOException
	 */
	public byte[] read(int length) throws IOException {
		if (remaining < length)
			length = (int) remaining;
//		ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//		is.read(byteBuffer);
//		byte[] buffer = byteBuffer.array();
		byte[] buffer = new byte[length];
		bis.read(buffer);
		remaining -= length;
		return buffer;
	}

	/**
	 * liest eine angegebene Anzahl bytes aus, beginnend bei einer angegebenen
	 * Postion
	 * 
	 * @param length   anzahl der auszulesenden Bytes
	 * @param position erste Position ab der gelesen werden soll
	 * @return byte[] das die ausgelesenen bytes enthält
	 * @throws IOException
	 */
	public byte[] read(int length, int position) throws IOException {
		raf.seek(position);
//		is.position(position);
		byte[] buffer = new byte[length];
		bis.read(buffer);
		return buffer;
	}

	/**
	 * <b>Anmerkung</b>
	 * </br>
	 * Wird nicht mehr genutzt
	 * </br>
	 * schließt für einen FILE_MNG, der lesen und schreiben kann die Input und
	 * Output Ströme, um Resourcen frei zu geben.
	 * 
	 * @throws IOException
	 */
	public void closeStream() throws IOException { //TODO: könnte hinfällig werden
		is.close();
		fo.close();
	}

	/**
	 * Dient zum Erstellen von Platzhaltern (zum Beispiel um den Header nach dem
	 * Datensegment zu schreiben). Bis zur angegeben Position werden 0en
	 * geschrieben. Die Schreibeposition wird somit vor gerückt.
	 * 
	 * @param position die Position ab der begonnen werden soll zu schreiben
	 * @throws IOException
	 */
	public void setPosition(int position) throws IOException {
		byte[] temp = new byte[position];
		ByteBuffer platzhalter = ByteBuffer.wrap(temp);
		fo.write(platzhalter);
	}

	/**
	 * schreibt an der ihm übergebenen Position weiter
	 * 
	 * @param buffer
	 * @param position
	 * @throws IOException
	 */
	public void write(byte[] buffer, long position) throws IOException {
		ByteBuffer bBuffer = ByteBuffer.wrap(buffer);
		fo.position(position);
		fo.write(bBuffer);
	}

	/**
	 * schreibt an der gespeicherten Position weiter
	 * 
	 * @param buffer
	 * @throws IOException
	 */
	public void write(byte[] buffer) throws IOException {
		ByteBuffer bBuffer = ByteBuffer.wrap(buffer);
		fo.write(bBuffer);  
	}

	/**
	 * gibt die Anzahl bytes zurück, die noch nicht gelesen wurden
	 * 
	 * @return long Anzahl bytes, die noch nicht gelesen wurden
	 */
	public static long remaining() {
		return remaining;
	}

	/**
	 * dekodiert eine Base-32 Datei
	 * 
	 * @throws IOException
	 */
	public void decode() throws IOException { 
		StringBuffer binaryString;  
		ByteBuffer byteBuffer;
		int position; 
		String alphabet;

		if (fromPath.endsWith("base-32")) {
			alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
		} else {  
			BufferedReader br = new BufferedReader(new FileReader(fromPath));
			alphabet = br.readLine().toLowerCase();
			br.close(); 
			System.out.println(alphabet);  
			raf.seek(alphabet.length() + 1);
//			is.position(alphabet.length() + 1);
			remaining -= alphabet.length() + 1;
		}   
		int bitNumber = (int) (Math.log(alphabet.length())/Math.log(2));
		while (remaining != 0) {
			byte[] buffer = read(8 * 1024); // Anzahl muss durch 8 teilbar sein
			binaryString = new StringBuffer(buffer.length * bitNumber);
			// erzeugen eines Strings mit den bitwerten der Datei
			for (int i = 0; i < buffer.length; i++) {
				position = alphabet.indexOf(buffer[i]); // gibt die Zahl des Buchstaben aus dem übergebenen Alphabet
				      	  	     		 						// zurück
				binaryString.append(String.format("%"+bitNumber+"s", Integer.toBinaryString(position)).replace(' ', '0')); 
				// erzeugt einen String mit log2(alphabet(length) Binärzahlen der Alphabetsposition
			}
			// erzeugen eines ByteBuffers, der die ursprünglichen Werte enthält
			while (binaryString.length() % 8 != 0) { // die bei der codierung hinten angeh�ngten 0en entfernen
				binaryString.deleteCharAt(binaryString.length() - 1);
			}
			byteBuffer = ByteBuffer.allocate(binaryString.length() / 8);
			for (int i = 0; i < binaryString.length(); i += 8) {
				String output = binaryString.substring(i, i + 8); 
				byte activeByte = (byte) (Integer.parseInt(output, 2));
//				if (activeByte != -1)
				byteBuffer.put(activeByte); // Integer.parseInt um signed byte zu erhalten
			}
			byteBuffer.compact();
			write(byteBuffer.array());
			byteBuffer.clear();

		}
	}

	/**
	 * Kodiert eine Datei Base-n mit dem Alphabet aus der Eingabe. 
	 * @param alphabet das Alphabet, das zum kodieren benutzt werden soll
	 * @throws IOException
	 */
	public void encode(String alphabet) throws IOException {
		if (!alphabet.toLowerCase().equals("0123456789abcdefghijklmnopqrstuv")) {
			alphabet = alphabet+"\n";
			write(alphabet.getBytes()); //TODO: nur bei base-n oder mit anderem als standardalphabet
		}
		int bitNumber = (int) (Math.log(alphabet.length()) / Math.log(2));
		while (remaining != 0) {
			int codingnumber;
			byte[] buffer = read(bitNumber * 1024); // soll durch log2(alphabet.length) teilbar sein und die schreibgröße ist dann immer 3kB
			StringBuffer binaryString = new StringBuffer(buffer.length);
			StringBuffer ausgabeBuffer = new StringBuffer(buffer.length);
			for (int j = 0; j < buffer.length; j++) {
				binaryString.append(String.format("%8s", Integer.toBinaryString(buffer[j] & 0xFF)).replace(' ', '0'));
			}
			while (binaryString.length() % bitNumber != 0) { // falls man am Ende des Datensatzes ist, muss eine 5
				binaryString.append(0);
			}
			for (int i = 0; i < binaryString.length(); i += bitNumber) {
				codingnumber = Integer.parseInt(binaryString.substring(i, i + bitNumber), 2);
				ausgabeBuffer.append(alphabet.charAt(codingnumber));
			}
			String ausgabeString = ausgabeBuffer.toString();
			write(ausgabeString.getBytes());
		}
//		closeStream();
	}
	
	/**
	 * kodiert die Datei mt dem Standardalphabet "0123456789ABCDEFGHIJKLMNOPQRSTUV" base-32
	 * @throws IOException
	 */
	public void encode() throws IOException {
		String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUV";
		encode(alphabet);
	}

	public byte[] dekomprimiere_huffman() throws IOException {
		byte[] returnBuffer = new byte[3*1024];
		byte currentByte;
		if (FILE_MNG.tree == null) {
			this.raf.seek(30); //TODO: wieso muss man hier erst die position suchen?
			tree = new HuffmanTree(fis);
		}
		if (fis.available()== 0) { //fis= fileInputStream ; bis = BufferedInputStream
			//rest aus dem bis auslesen
			returnBuffer = new byte[bis.available()*8];
			byte[] tempBuffer; 
			for (int i = 0; i<returnBuffer.length; i++) {
				int value = tree.readPath();
				if (value == -1) {
					tempBuffer = new byte[i];
					for (int j= 0; j<i; j++)
						tempBuffer[j] = returnBuffer[j]; 
					returnBuffer = tempBuffer;
					break;
				}
				else {
					currentByte = (byte) value;
					returnBuffer[i] = currentByte;
				}
			}
			remaining = 0;
		}
		else {
			for (int i = 0; i<returnBuffer.length; i++) {
				currentByte = (byte) tree.readPath();
				returnBuffer[i] = currentByte;
			}
		}
		return returnBuffer;
	}
	
	public void erstelleHuffmanCode(int headerSize) throws IOException {
		byte b;
		HashMap<Byte, Integer> treeDictionary = new HashMap<Byte, Integer>();
		raf.seek(headerSize); 
		long datasize = bis.available();
		long remaining = bis.available();
		while (remaining > 0) {
			b = (byte) bis.read();
			if (!treeDictionary.containsKey(b)) {
				treeDictionary.put(b, 1);
			}
			else
				treeDictionary.put(b, treeDictionary.get(b)+1);
			remaining--;
		}
//		System.out.println(treeDictionary);
		tree = new HuffmanTree();
		tree.erstelleCodeBaum(treeDictionary, datasize); //TODO: Code für die Blätter: while(location != null) += 2^i++ 
	}

	public byte[] komprimiere_huffman(byte[] buffer) { 
		int intNumber;
		List<Byte> outputList = new ArrayList<Byte>();

		if (!treeLoaded) { // für das erste Schreiben, muss zunächst das Code-Wörterbuch erstellt werden und der Baum in die Datei geschrieben werden
			decodeDictionary = tree.getDictionary();
			System.out.println("decodeDictionary" + decodeDictionary);
			bitString.append(tree.getTreeCode());
			while(bitString.length()>=8) { //könnte auch raus, wird im Prinzip am Ende der Datei automatisch gemacht. Finde es aber so übersichtlicher
				intNumber = Integer.parseInt(bitString.substring(0, 8),2);
				outputList.add((byte) intNumber);
				bitString.delete(0, 8);
			}
			treeLoaded = true;
		}
		for(byte b : buffer) {
			bitString.append(decodeDictionary.get(b));
			while (bitString.length()>=8) {
				intNumber = Integer.parseInt(bitString.substring(0, 8),2);
				outputList.add((byte) intNumber);
				bitString.delete(0, 8);
			}
		}
		if (remaining == 0 && bitString.length()!=0) {
			while (bitString.length()!=8)
				bitString.append("0");
			intNumber = Integer.parseInt(bitString.toString(),2);
			outputList.add((byte) intNumber);
		}
		byte[] outputBuffer = new byte[outputList.size()];
		int i = 0;
		for (byte b : outputList)
			outputBuffer[i++] = b;
		return outputBuffer;
	}

}
