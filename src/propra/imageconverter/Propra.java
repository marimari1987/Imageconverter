package propra.imageconverter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Klasse Propra Subtyp von Bilddatei.
 * @author Annika Nöding
 *
 */
public class Propra implements Bilddatei {

	private static final int X = 65521;
	private byte[] header = new byte[30];
	byte[] bildhoehe;
	byte[] bildbreite;  
	byte bitsProBildpunkt;
	byte[] formatkennung;
	byte kompressionstyp;
	long datensegmentgroesse;
	long pruefsumme; 
	private byte[] buffer;
	private FILE_MNG mng;      
	private long a = 0;
	private long b = 1;
	private long currentPosition = 0;
	private String toCompression;
	private CompressionType toCompressionType;  
	
	public Propra() {
		toCompressionType = Path_MNG.getToCompressionType();
	}
	
	@Override
	public void konvertiere(Bilddatei toType, String toCompression) //TODO: der FILE_MNG soll die Daten fromPath, ToPath direkt beim Path_MNG holen eventuell auch die compression
			throws FileNotFoundException, IOException, InconsistentFileException {
		mng = new FILE_MNG();
		formatkennung = mng.read(12);
		kompressionstyp = mng.read(1)[0];
		bildbreite = mng.read(2);
		bildhoehe = mng.read(2);
		bitsProBildpunkt = mng.read(1)[0];
		datensegmentgroesse = byteToLong(mng.read(8));
		pruefsumme = byteToLong(mng.read(4));
//		System.out.println("Prüfsumme: "+pruefsumme);
		this.toCompression = toCompression;  
		this.toCompressionType = Path_MNG.getToCompressionType();
		System.out.println("Daten eingelesen.");
				
		mng.setPosition(toType.getHeaderSize());  
		erstelleDatensegment(toType);
		long pruefsumme_berechnet = (long) (this.a * Math.pow(2, 16) + this.b);
//		System.out.println("Prüfsumme: "+pruefsumme);
//		System.out.println("Berechnete Prüfsumme: " + pruefsumme_berechnet);
//		if (pruefsumme != pruefsumme_berechnet)
//			throw new InconsistentFileException("Prüfsumme stimmt nicht mit den Daten überein");
		toType.setHeader(bildbreite, bildhoehe, bitsProBildpunkt, toCompression); 
		mng.write(toType.getHeader(),0);

//		aufInkonsistenzPruefen();

		System.out.println("Neue Datei erfolgreich erstellt.");

//		mng.closeStream();

	}


	/**
	 * konvertiert die Bytereihenfolge der Pixel in die Standardisierte
	 * Form RGB im Little Endian und schickt diese an Das Zielformat
	 * zur Verarbeitung in sein Format und veranlasst schließlich das Schreiben
	 * 
	 * @param Bilddatei toType Das Zielformat
	 * @throws IOException 
	 */
	private  void erstelleDatensegment(Bilddatei toType) throws IOException{
		while (FILE_MNG.remaining() != 0) { 
			if (kompressionstyp ==2) {  
				buffer = mng.dekomprimiere_huffman(); //TODO: Prüfsumme des ausgangsbildes berechnen
			} 
			else {              		  
				buffer = mng.read(3*1024);    //TODO: mit dem BufferedInputStream muss man das nicht mehr mit noch einem Buffer machen
				//Prüfsumme berechnen  
				for (int i = 0; i < buffer.length; i++) {
					this.a = (this.a + ((i) + 1 + currentPosition) + Byte.toUnsignedInt(buffer[i])) % X;
					this.b = (this.b + this.a) % X;
				}  
				this.currentPosition += buffer.length;  
				
				CompressionType fromCompressionType = getCompressionType();
				boolean isRleCompressed = fromCompressionType instanceof RLE_CompressionType;
				
				
//				if(kompressionstyp == 1 && toCompression.equals("uncompressed")) { //von rle nach uncompressed
//					buffer = RLE_CompressionType.dekomprimiere_rle(buffer);
//				}
				if (isRleCompressed && toCompressionType instanceof RLE_CompressionType) { //von rle nach rle ohne vorher zu dekomprimieren
					buffer = RLE_CompressionType.extrahierePixel(buffer);
				}
				else
					buffer = fromCompressionType.decompress(buffer);  
			}
			
			byte temp;
			for (int i = 0; i < buffer.length; i+=3) { // (RBG in RGB umwandeln)
				//reihenfolge ändern
				temp = buffer[i];
				buffer[i] = buffer[i+1];
				buffer[i+1] = buffer [i+2];
				buffer[i+2] = temp;    

			}
			toType.setBuffer(buffer, toCompression, RLE_CompressionType.getSteuerbytes()); 
			
			mng.write(toType.getBuffer());    
			
		}
	}
	
	@Override
	public void setBuffer(byte[] buffer, String toCompression, byte[] steuerbytes) { 
		// umwandeln von RGB in little endian nach RBG in little endian
		this.buffer = new byte[buffer.length];
		
		for (int i = 0; i < buffer.length; i+=3) {
			
			this.buffer[i] = buffer[i+2];
			this.buffer[i+1] = buffer[i];
			this.buffer[i+2] = buffer[i+1];
			
		}
		if (toCompressionType instanceof HuffmanType)
			this.buffer = mng.komprimiere_huffman(this.buffer);
		else
			this.buffer = toCompressionType.compress(this.buffer);

//		if (toCompression.toLowerCase().equals("rle")) { 
//			FILE_MNG filemng = new FILE_MNG();
//			this.buffer = filemng.komprimiere_rle(this.buffer);
//		}
//		
//		if (toCompression.toLowerCase().equals("huffman")) {
////			FILE_MNG filemng = new FILE_MNG();
//			this.buffer = mng.komprimiere_huffman(this.buffer);
//		}
		
		//Prüfsumme berechnen  
		for (int i = 0; i < this.buffer.length; i++) {

			this.a = (this.a + ((i+0) + 1 + currentPosition) + Byte.toUnsignedInt(this.buffer[i]))%X;
			this.b = (this.b + this.a)%X;
		}
		  
		currentPosition += this.buffer.length;
		
		
	}


	/** 
	 * prüft ob in der eingelesenen Datei Inkonsitenzen vorliegen
	 * 
	 * @param buffer byte[] im Eingabe-Format propra
	 * @throws InconsistentFileException
	 */
	private void aufInkonsistenzPruefen() throws InconsistentFileException {

		byte[] formatkennung_soll = { (byte) 80, (byte) 114, (byte) 111, (byte) 80, (byte) 114, (byte) 97, (byte) 87,
				(byte) 105, (byte) 83, (byte) 101, (byte) 50, (byte) 50 };  
		if (!Arrays.equals(formatkennung, formatkennung_soll))
			throw new InconsistentFileException("Formatkennung ist nicht richtig");
		int bildbreiteInt = (((bildbreite[1] & 0xFF) << 8) | (bildbreite[0] & 0xFF));
		int bildhoeheInt = (((bildhoehe[1] & 0xFF) << 8) | (bildhoehe[0] & 0xFF));
		if (bildbreiteInt == 0 || bildhoeheInt == 0)
			throw new InconsistentFileException("Keine Bilddaten verfügbar.");
		if (datensegmentgroesse != this.currentPosition)
			throw new InconsistentFileException("Datengrößen im Header stimmen nicht überein.");
		if(kompressionstyp == 0) { //geht nur bei unkomprimierten Bildern

			if (datensegmentgroesse > (mng.remaining()))
				throw new InconsistentFileException("Datenübertragungsfehler.");
			if (datensegmentgroesse < (mng.remaining()))
				throw new InconsistentFileException("Achtung: Daten sind Kompromitiert!");
		}
			
	}


	@Override 
	public void setHeader(byte[] bildbreite, byte[] bildhoehe, byte bitsProBildpunkt, String toCompression) {
		formatkennung = "ProPraWiSe22".getBytes(StandardCharsets.UTF_8);
		if(toCompression.toLowerCase().equals("rle"))
			kompressionstyp = 1;
		else if (toCompression.toLowerCase().equals("huffman"))
			kompressionstyp = 2;
		else
			kompressionstyp = 0;    
		this.bildbreite = bildbreite;
		this.bildhoehe = bildhoehe;
		this.bitsProBildpunkt = bitsProBildpunkt;
		int bildbreiteLong = (((this.bildbreite[1] & 0xFF) << 8) | (this.bildbreite[0] & 0xFF));
		int bildhoeheLong = (((this.bildhoehe[1] & 0xFF) << 8) | (this.bildhoehe[0] & 0xFF));
		this.datensegmentgroesse = this.currentPosition; //TODO: eventuell umbenennen
		byte[] segmentgrArray = longToBytes(datensegmentgroesse);
		
		
		// Header erstellen:
		for (int i = 0; i<12; i++)
			header[i] = formatkennung[i];
		header[12] = kompressionstyp;
		header[13] = bildbreite[0];
		header[14] = bildbreite[1];
		header[15] = bildhoehe[0];
		header[16] = bildhoehe[1];
		header[17] = bitsProBildpunkt;
		for (int i =0; i< 8; i++)
			header[i+18] = segmentgrArray[i];
		for (int i = 26; i<30; i++)
			header[i] = 0;
	}
	
	/**
	 * berechnet den long wert eines Arrays im little endian Format
	 * @param buffer     byte[] Byte-Array im Eingabe-Format propra
	 * @return den Wert als long
	 */
	private long byteToLong(byte[] buffer) {

		long ergebnis = 0;
		for (int i = (buffer.length - 1); i >= 0; i--) {
			ergebnis <<= 8;
			ergebnis |= (buffer[i] & 0xFF);
		}
		return ergebnis;
	}
	
	/**
	 * gibt einen long-Wert im little Endian Format als byte-Array aus
	 * @param x der zu konvertiernde long-Wert
	 * @return byte[] long wert in little endian
	 */
	private byte[] longToBytes(long x) {
		
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(x);
		return buffer.array();
	}

	@Override
	public byte[] getHeader() {
		this.pruefsumme = (long) (this.a * Math.pow(2, 16) + this.b);
//		System.out.println("Prüfsumme: "+pruefsumme);
		byte[] pruefsummearray=longToBytes(pruefsumme);
		for (int i = 26; i<30; i++)
			header[i] = pruefsummearray[i-26];
		return header;
	}



	@Override
	public byte[] getBuffer() {
		return buffer;
	}

	@Override
	public int getHeaderSize() {  
		return header.length;
	}

	@Override
	public CompressionType getCompressionType() {
		CompressionType compressionType = new CompressionType();
		if (kompressionstyp == 1)
			compressionType = new RLE_CompressionType();
		return compressionType;  
	}

}
