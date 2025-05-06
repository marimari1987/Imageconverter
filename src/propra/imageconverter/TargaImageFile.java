package propra.imageconverter;

import java.io.*;


/**
 * Klasse TargaImageFile Subtyp von Bilddatei.
 * @author Annika Nöding
 *
 */
public class TargaImageFile implements Bilddatei {
	
	private byte[] header = new byte[18];
	private byte[] buffer;
	private FILE_MNG mng;
	private byte laengeDerBildID;
	private byte farbpalettentyp;
	private byte bildtyp;
	private byte[] palettenbeginn;
	private byte[] palettenlaenge;
	private byte groessePaletteneintrag;
	private byte[] x_koordinate;
	private byte[] y_koordinate;
	private byte[] bildbreite;
	private byte[] bildhoehe;
	private byte bitsProBildpunkt;
	private byte bildAttribute;
	private String toCompression;
	private CompressionType toCompressionType;
	
	public TargaImageFile() {
		toCompressionType = Path_MNG.getToCompressionType();
	}
	
	
	@Override
	public void konvertiere(Bilddatei toType,String toCompression) throws IOException, InconsistentFileException {
		
		mng = new FILE_MNG();
		
		laengeDerBildID = mng.read(1)[0];
		 farbpalettentyp = mng.read(1)[0];
		 bildtyp = mng.read(1)[0];
		palettenbeginn = mng.read(2);
		palettenlaenge = mng.read(2);
		 groessePaletteneintrag = mng.read(1)[0];
		 x_koordinate = mng.read(2);
		 y_koordinate = mng.read(2);
		 bildbreite = mng.read(2);
		 bildhoehe = mng.read(2);
		 bitsProBildpunkt = mng.read(1)[0];
		 bildAttribute = mng.read(1)[0];
		 this.toCompression = toCompression;
		 this.toCompressionType = Path_MNG.getToCompressionType();
		 
		 System.out.println("Daten eingelesen.");
		
//		 aufInkonsistenzPruefen();

		mng.setPosition(toType.getHeaderSize());
		erstelleDatensegment(toType);
		toType.setHeader(bildbreite, bildhoehe, bitsProBildpunkt, toCompression);
		mng.write(toType.getHeader(),0);

		System.out.println("Datei erfolgreich erstellt.");
//		mng.closeStream();
	}

	/**
	 * gibt das Datensegment in Standardformat RGB little endian aus und gibt es zur Konvertierung an die 
	 * Klasse des Zielformats weiter und veranlasst anschließend das Schreiben des Datensegments in die Zieldatei.
	 * @param toType Subtyp von Bilddatei Zielformat
	 * @throws IOException
	 */
	private void erstelleDatensegment(Bilddatei toType) throws IOException {
		CompressionType fromCompressionType = getCompressionType();
		boolean isRleCompressed = fromCompressionType instanceof RLE_CompressionType;
		while (FILE_MNG.remaining() != 0) {
			buffer = mng.read(6*1024);
//			if (isRleCompressed && toCompression.equals("uncompressed")) { // wenn es rle komprimiert ist
//				buffer = mng.dekomprimiere_rle(buffer);
//			}
			if (isRleCompressed && toCompressionType instanceof RLE_CompressionType) {//rle nach rle
				buffer = RLE_CompressionType.extrahierePixel(buffer);
//				buffer = mng.extrahierePixel(buffer);
			}
			else 
				buffer = fromCompressionType.decompress(buffer);
			toType.setBuffer(buffer, toCompression, RLE_CompressionType.getSteuerbytes()); 
			mng.write(toType.getBuffer());
		}
	}

	/**
	 * prüft ob in der eingelesenen Datei Inkonsitenzen vorliegen
	 * 
	 * @param buffer
	 * @throws InconsistentFileException
	 */
	private void aufInkonsistenzPruefen() throws InconsistentFileException {
		int bildbreiteInt = (((bildbreite[1] & 0xFF) << 8) | (bildbreite[0] & 0xFF));
		int bildhoeheInt = (((bildhoehe[1] & 0xFF) << 8) | (bildhoehe[0] & 0xFF));
		if (bildbreiteInt == 0 || bildhoeheInt == 0)
			throw new InconsistentFileException("keine Bilddaten vorhanden.");
		if (bildtyp != (byte) 2 && bildtyp != (byte) 10) {
			throw new InconsistentFileException("Bildtyp wird nicht unterstützt");
		}
		if (bildtyp == (byte) 2) {
			if ((long) (bildbreiteInt * bildhoeheInt * 3) > FILE_MNG.remaining())
				throw new InconsistentFileException("Datenmenge stimmt nicht mit den Angaben überein.");
		}
	}


	@Override
	public void setHeader(byte[] bildbreite, byte[] bildhoehe, byte bitsProBildpunkt, String toCompression) {
		header[0] =(byte) 0; // BildID
		header[1]= (byte) 0; // Farbpalette
		if (toCompression.toLowerCase().equals("rle"))
			header[2]=(byte) 10;
		else
			header[2]=(byte) 2; // Bildtyp
		header[3]=(byte) 0; // Palettenbeginn
		header[4]=(byte) 0; // "
		header[5]=(byte) 0; // Palettenlänge
		header[6]=(byte) 0; // "
		header[7]=(byte) 0; // Größe Paletteneintrag
		header[8]=(byte) 0; // x-Koordinate
		header[9]=(byte) 0; // "
		header[10]=bildhoehe[0]; // y-Koordinate
		header[11]=bildhoehe[1]; // "
		header[12]=bildbreite[0]; // Bildbreite
		header[13]=bildbreite[1]; // "
		header[14]=bildhoehe[0]; // Bildhöhe
		header[15]=bildhoehe[1]; // "
		header[16]=bitsProBildpunkt; // Bits pro Bildpunkt
		header[17]=(byte) 32; // Dez 32 = Hex 20 = Bin 0010 0000
		
	}

	
	@Override
	public void setBuffer(byte[] buffer, String toCompression, byte[] steuerbytes) {
		
//		if(toCompression.toLowerCase().equals("rle") && steuerbytes.length == 0) { //es wurden keine Pixel extrahiert
////			FILE_MNG filemng = new FILE_MNG();
////			buffer = mng.komprimiere_rle(buffer);
//		}
		if (toCompression.toLowerCase().equals("rle") && steuerbytes.length >0){
			buffer = RLE_CompressionType.fusion(buffer,steuerbytes); //TODO: das klappt irgendwie nicht richtig
		}
		else
			buffer = toCompressionType.compress(buffer);
		this.buffer = buffer;
	}
	

	/**
	 * berechnet aus den Byte-Daten des headers im tga-Format die Größe des
	 * Datensegments
	 * @param buffer byte[] mit den Daten des Eingabe-Formats (tga)
	 * @return long, die berechnete Größe des Datensegments
	 */
	private long berechneDatesegmentgroesse(byte[] buffer) {
		
		int bildbreite = (((buffer[13] & 0xFF) << 8) | (buffer[12] & 0xFF));
		int bildhoehe = (((buffer[15] & 0xFF) << 8) | (buffer[14] & 0xFF));
		long datensegmentgroesse = bildbreite * bildhoehe * 3;
		return datensegmentgroesse;
	}

	@Override
	public byte[] getHeader() {
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
		if (bildtyp == (byte) 10)
			compressionType = new RLE_CompressionType();
		return compressionType;
	}

}
