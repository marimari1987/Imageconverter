package propra.imageconverter;

import java.io.IOException;

/**
 * Klasse Path_MNG kümmert sich um die extrahierung der Daten aus String args[] der Hauptklasse und isoliert die 
 * relevanten Teile zur weiteren Bearbeitung
 * @author Annika Nöding
 *
 */
public class Path_MNG {
	private static String input = "";
	private static String output = "";
	private static String toCompression = ""; //TODO: eventuell direkt vom FILEMNG aus drauf zugreifen
	private static CompressionType toCompressionType;
	private String codingType ="";
	private String base = "";
	public boolean hasArguments = false;
	private String[] possibleArguments = {
			"--input",
			"--output",
			"--compression",
			"--decode",
			"--encode"
	};
	private String[] possibleCompressions = { 
			"uncompressed",
			"rle",
			"huffman"
	};
	
	public Path_MNG(String[] args) throws WrongInputException, IOException {
		if (args.length >= 2) {
			hasArguments = true;
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--input")) {
					input = args[i].substring(8);
				}
				else if (args[i].startsWith("--output"))
						output = args[i].substring(9);
				else if (args[i].startsWith("--compression")) {
					toCompression = args[i].substring(14);
					toCompressionType = getCompressionType(toCompression);
					if (!isPossibleCompression(toCompression)) //TODO: ich denke das ist doppelt
//						if (!compression.equals("")&&!compression.toLowerCase().equals("uncompressed")&&!compression.toLowerCase().equals("rle"))
							throw new WrongInputException("Kompressionstyp "+ toCompression + " wird nicht unterstützt.");
					if (toCompressionType instanceof HuffmanType && !output.endsWith(".propra"))
						throw new WrongInputException("Huffman-Kodierung nur bei ProPra-Format möglich.");
				}
				else if (args[i].startsWith("--decode"))
					codingType = "decode";
				else if (args[i].startsWith("--encode")) {
					codingType = "encode";
					base = args[i].substring(9);
					if (base.startsWith("base-32")) {
						output = input+".base-32";
					}
					else
						output = input+".base-n";
				}
				else {
					System.err.print("Falsche Eingabe. Mögliche Eingaben sind: \n");
					getPossibleArguments();
					throw new WrongInputException("");
				}
			}
			if (input == "")
				throw new WrongInputException("Keine Inputdatei übergeben.");
			if (codingType == "decode" && (!(input.endsWith("base-n") || input.endsWith("base-32"))))
				throw new WrongInputException("Nur base-n oder base-32 kodierte Dateien können Base-Dekodiert werden.");
			if (toCompression == "")
				toCompression = "uncompressed";
		}
		
	}
	
	public Path_MNG() {
		
	}

	private CompressionType getCompressionType(String compressionString) throws WrongInputException, IOException {
		CompressionType type;
		if (compressionString.equals("uncompressed"))
			type = new CompressionType();
		else if (compressionString.equals("rle"))
			type = new RLE_CompressionType();
		else if (compressionString.equals("huffman"))
			type = new HuffmanType();
		else 
			throw new WrongInputException("Nicht unterstützte Kompression");
		return type;
	}

	public String getInputString() { //TODO: kann dann weg, wenn alles klapt
		return input;
	}

	public String getOutputString() {
		return output;
	}

	/**
	 * Gibt einen String aus, der die gewünschte Zielkompression enthält. 
	 * Werte sind: <code>uncompressed</code>, <code>rle</code> oder <code>huffman</code>.
	 * @return String gewünschte Zielkompression
	 * @throws WrongInputException
	 */
	public String getCompression() throws WrongInputException {
		return toCompression;
	}

	private boolean isPossibleCompression(String s) {
		for (String compression : possibleCompressions)
			if (compression.toLowerCase().equals(s))
				return true;
		return false;
	}

	public String getCodingType() {
		return codingType;
	}

	/**
	 * Erstellt ein leeres Bilddatei Objekt von Typ filename ("propra" oer "tga")
	 * @param filename "propra" oder "tga"
	 * @return Bilddatei-Objekt vom Typ des Strings
	 */
	public Bilddatei getType(String filename) { 
		if (filename != null) {
			if (filename.endsWith(".propra".toLowerCase()))
				return new Propra();
			else if (filename.endsWith(".tga".toLowerCase()))
				return new TargaImageFile();    
			else
				return null;
		}
		else 
			return null;
	}

	public String getBase() {
		
		return base;
	}

	public String getAlphabet() throws WrongInputException {
		if (base.length()<8)
			throw new WrongInputException("Bei Base-n bitte das zu verwendende Alphabet angeben!");
		return base.substring(7);
	}
	
	/**
	 * Gibt auf der Konsole eine Auflistung möglicher Eingaben aus.
	 */
	public void getPossibleArguments() {
		System.out.println("Möglichkeiten für Eingaben:");
		for (String s : possibleArguments)
			System.out.println(s);
		System.out.println();
		System.out.println("Möglichkeiten für Kompression:");
		for (String s : possibleCompressions)
			System.out.println(s);
		System.out.println("huffman-codierung nur be Propra möglich.");
		System.out.println();
		System.out.println("Base-Kodierung: (Die Ausgabedatei wird jeweils am gleichen Ort wie die Eingabedatei erstellt.)");
		System.out.println("automatisches Standardalphabet bei Base-32: 0123456789ABCDEFGHIJKLMNOPQRSTUV");
		System.out.println("--decode-base-32 (Es wird das automatische Standardalphabet verwendet.Das Ausgabeformat ist die Datei ohne Suffix '.basse-32'");
		System.out.println("--encode-base-32 (Es wird das autoatische Standardalphabet vewendet. Das Ausgabeformat hat zusätzlich das Suffix '.base-32'");
		System.out.println("--decode-base-n (Das Alphabet wird aus der ersten Zeile der Datei ausgelesen. Es können nur Alphabete mit einer Länge einer 2er Potenz bearbeitet werden.");
		System.out.println("--encode-base-n=<alphabet> (Das Alphabet wird übergeben. Die Länge muss dabei eine 2er Potenz sein.");

	}

	public static CompressionType getToCompressionType() {
		return toCompressionType;
	}
	public static String getInput() {
		return input;
	}

	public static String getOutput() {
		return output;
	}

	public static String getToCompression() {
		return toCompression;
	}

	public boolean isHasArguments() {
		return hasArguments;
	}

	public String[] getPossibleCompressions() {
		return possibleCompressions;
	}


}
