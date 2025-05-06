package propra.imageconverter;

/**
 * Klasse InconsistentFileException Subtyp von Exception
 * Für Inkonsistenzen aus Bilddateien.
 * @author Annika Nöding
 *
 */
public class InconsistentFileException extends Exception{

	public InconsistentFileException(String string) {
		super(string);
	}
	
}
