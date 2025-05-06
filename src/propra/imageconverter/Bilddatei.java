package propra.imageconverter;
import java.io.IOException;

/**
 * Interface, Supertyp aller Klassen, die Formate für Bilddateien darstellen. 
 * Stellt Methoden zur Verfügung, die konvertierungen von Bilddateien ermöglichen ohne den direkten Subtyp zu kennen.
 * @author Annika Nöding
 *
 */
public interface Bilddatei { //eventuell mit statischen methoden belegen
	/**
	 * liest eine Bilddatei von Pfad fromPath ein und schreibt eine fertig konvertierte
	 * Datei an einem angegebenen Pfad toPath.
	 * Möglichkeiten für auszugebene Kompressionen sind: "uncompressed" oder "rle" für 24-bit Farben Lauflängenkodierung. 
	 * @param Bilddatei toType Bilddatei-Typ in den konvertiert werden soll (Subtyp von Bilddatei)
	 * @param String fromPath Dateipfad der einzulesenden Datei
	 * @param String toPath Dateipfad und Name der Zieldatei
	 * @param String toCompression String mit dem gewünschten Ausgabeformat. Möglichkeiten : "uncompressed" oder "rle"
	 * @throws IOException wenn die Datei nicht gelesen oder geschrieben werden konnte
	 * @throws InconsistentFileException wenn in der Datei Inkonsistenzen sind
	 *
	 */
	public void konvertiere(Bilddatei toType, String toCompression)throws IOException, InconsistentFileException;
 
	/**
	 * bekommt die Werte für die Bildbreite, die Bildhöhe und die bits pro Bildpunkt im Standardformat Little Endian
	 * erstellt aus den Werten seinen spezifischen Header.
	 *  
	 * @param bildbreite Bildbreite in Little Endian
	 * @param bildhoehe Bildhöhe in Little Endian
	 * @param bitsProBildpunkt bits pro Bildpunkt
	 * @param toCompression gibt an welcher Kompressionstyp ausgegeben werden soll ("rle" oder "uncompressed")
	 */
	public void setHeader(byte[] bildbreite, byte[] bildhoehe, byte bitsProBildpunkt, String toCompression);

	/**
	 * Gibt den spezifischen Header als byte-Array aus.
	 * @return byte[] mit dem fertigen Header
	 */
	public byte[] getHeader();

	/**
	 * bekommt als Standardreihenfolge die Pixel in RGB als Little Endian ByteArray übergeben und erstellt daraus
	 * einen spezifischen Datensegment Puffer in der gewünschten Kompression toCompression. 
	 * Bei konvertierung von rle zu rle werden die Steuerbytes und die Pixeldaten aus dem buffer wieder zusammengefügt.
	 * @param buffer Pixel in RGB Little Endian
	 * @param toCompression gibt an welche Komprimierung die fertige Datei haben soll
	 * @param steuerbytes nicht leer, wenn von rle nach rle konvertiert werden soll, 
	 * dann enthält sie die von den Pixeldaten isolierten Steuerbytes als byte-Array
	 */
	public void setBuffer(byte[] buffer, String toCompression, byte[] steuerbytes);    
	  
	/**
	 * gibt den gespeicherten Puffer mit einem Teilstück des Datensegments zurück.
	 * @return byte[] buffer
	 */ 
	public byte[] getBuffer();  

	/**
	 * gibt die Größe des Headers zurück
	 * @return int größe des Headers
	 */
	public int getHeaderSize();
	
	public CompressionType getCompressionType();

}
