package propra.imageconverter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RLE_CompressionType extends CompressionType {

	private static byte[] remainingBuffer = new byte[0];
	private static ArrayList<Integer> indexSpeicher = new ArrayList<Integer>();
	private static byte[] steuerbyteBuffer = new byte[0];

	/**
	 * erstellt ein lauflängen kodiertes byte-Array aus dem eingegebenen byte-Array.
	 * Das eingegebene byte-Array wird komplett komprimiert. Die Übergänge zwischen
	 * den einzelnen Datensegmenten werden nicht gesondert betrachtet.
	 * 
	 * @param eingabeBuffer das eingelesene Teilstück des gesamten Datenstroms
	 * @return byte[] rle komprimiert des eingelesenen Teilstückes
	 */
	@Override
	public byte[] compress(byte[] eingabeBuffer) {

		byte[] pixel = new byte[3];
		int position = 0;
		ByteBuffer unterschiedlichBucket = ByteBuffer.allocate(128 * 3); // maximal 127+1 unterschriedliche
																			// hintereinander
		ByteBuffer wiederholungsBucket = ByteBuffer.allocate(128 * 3); // maximal 128+1 gleiche hintereinander
		ArrayList<Byte> ausgabeListe = new ArrayList<Byte>();

		if (eingabeBuffer.length > 0) {
			// erstes pixel setzen
			pixel[0] = eingabeBuffer[position];
			pixel[1] = eingabeBuffer[position + 1];
			pixel[2] = eingabeBuffer[position + 2];

			position += 3;
			while (position < eingabeBuffer.length) {

				if ((eingabeBuffer[position] == pixel[0] // das nachfolgende Pixel ist das gleiche
						&& eingabeBuffer[position + 1] == pixel[1] && eingabeBuffer[position + 2] == pixel[2])) {
					if (wiederholungsBucket.remaining() == 0) { // der Bucket ist voll
						wiederholungsBucket = bucketLeeren(wiederholungsBucket, ausgabeListe, "wiederholung");
					}
					if (unterschiedlichBucket.position() != 0) { // bisher waren es unterschiedliche Pixel
						unterschiedlichBucket = bucketLeeren(unterschiedlichBucket, ausgabeListe, "unterschiedlich");
					}
					for (int i = 0; i < 3; i++)
						wiederholungsBucket.put(pixel[i]);
					if (wiederholungsBucket.remaining() == 0)
						wiederholungsBucket = bucketLeeren(wiederholungsBucket, ausgabeListe, "wiederholung");

				} else { // das nachfolgende pixel ist ein unterschiedliches Pixel
					if (unterschiedlichBucket.remaining() == 0) { // TODO: ist die stelle hier richtig?
						unterschiedlichBucket = bucketLeeren(unterschiedlichBucket, ausgabeListe, "unterschiedlich");
					}
					if (wiederholungsBucket.position() != 0) { // bisher waren es gleiche Pixel, dann z�hlt das
																// gespeicherte Pixel noch zu den gleichen
						for (int i = 0; i < 3; i++)
							wiederholungsBucket.put(pixel[i]);
						wiederholungsBucket = bucketLeeren(wiederholungsBucket, ausgabeListe, "wiederholung");
					} else {
						for (int i = 0; i < 3; i++)
							unterschiedlichBucket.put(pixel[i]);
					}
				}
				// n�chstes Pixel setzen
				pixel[0] = eingabeBuffer[position];
				pixel[1] = eingabeBuffer[position + 1];
				pixel[2] = eingabeBuffer[position + 2];

				position += 3;
			}
		}
		// das letzte Pixel in de richtigen Bucket sortieren und den letzten Bucket
		// leeren
		if (unterschiedlichBucket.position() != 0) {// wenn der letzte ein unterschiedlicher Pixel war
			if (unterschiedlichBucket.remaining()==0)
				unterschiedlichBucket = bucketLeeren(unterschiedlichBucket, ausgabeListe, "unterschiedlich");
			for (int i = 0; i < 3; i++)
				unterschiedlichBucket.put(pixel[i]);
			unterschiedlichBucket = bucketLeeren(unterschiedlichBucket, ausgabeListe, "unterschiedlich");
		} else {
			for (int i = 0; i < 3; i++)
				wiederholungsBucket.put(pixel[i]);
			wiederholungsBucket = bucketLeeren(wiederholungsBucket, ausgabeListe, "wiederholung");
		}
		// byte[] für die ausgabe erstellen
		byte[] ausgabe = new byte[ausgabeListe.size()];
		for (int i = 0; i < ausgabe.length; i++)
			ausgabe[i] = ausgabeListe.get(i);
		return ausgabe;
	}

	/**
	 * dekomprimiert einen lauflängen komprimierten Byte-Array und gibt ihn als byte
	 * Array dekomprimiert wieder aus. Die Daten aus dem EingabeBuffer werden als
	 * Teildaten des gesamten Datenstroms behandelt. Daten, die nicht vollständig im
	 * aktuellen eingabeBuffer vorhanden sind, werden im this-Objekt gespeichert und
	 * im darauf folgenden Lauf vorne angefügt. Ist der Datenstrom komplett
	 * ausgelesen, werden alle Daten verarbeitet.
	 * 
	 * @param eingabeBuffer rle komprimiert
	 * @return byte[] mit den dekomprimierten Daten.
	 * @throws IOException
	 */
	@Override
	public byte[] decompress(byte[] eingabeBuffer) {
		if (remainingBuffer.length != 0) {
			byte[] bufferAndRemaining = new byte[remainingBuffer.length + eingabeBuffer.length];
			for (int i = 0; i < remainingBuffer.length; i++)
				bufferAndRemaining[i] = remainingBuffer[i];
			for (int j = remainingBuffer.length; j < bufferAndRemaining.length; j++)
				bufferAndRemaining[j] = eingabeBuffer[j - remainingBuffer.length];
			eingabeBuffer = bufferAndRemaining;
			remainingBuffer = new byte[0];
			bufferAndRemaining = new byte[0];
		} 
		List<Byte> ausgabeList = new ArrayList<Byte>();
		int position = 0;
		int counter = 0;
		while (position != eingabeBuffer.length) {
			counter = eingabeBuffer[position] < 0 ? eingabeBuffer[position] + 128 + 1 : eingabeBuffer[position] + 1;
			if (eingabeBuffer[position] < 0) { // das nachfolgende Pixel soll mehrfach geschrieben werden
				if (eingabeBuffer.length < position + 4 && FILE_MNG.remaining != 0) {
					remainingBuffer = new byte[eingabeBuffer.length - position];
					for (int i = position; i < eingabeBuffer.length; i++)
						remainingBuffer[i - position] = eingabeBuffer[i];
					break;
				} else {
					position += 1;
					for (int i = 0; i < counter; i++) {
						ausgabeList.add(eingabeBuffer[position]);
						ausgabeList.add(eingabeBuffer[position + 1]);
						ausgabeList.add(eingabeBuffer[position + 2]);
					}
				}
				position += 3;
			} else if (eingabeBuffer[position] >= 0) { // mehrere Pixel werden direkt �bernommen
				if (eingabeBuffer.length < position + 1 + (counter * 3) && FILE_MNG.remaining != 0) {
					remainingBuffer = new byte[eingabeBuffer.length - position];
					for (int i = position; i < eingabeBuffer.length; i++)
						remainingBuffer[i - position] = eingabeBuffer[i];
					break;
				} else {
					position += 1;
					for (int i = 0; i < counter; i++) {
						ausgabeList.add(eingabeBuffer[position]);
						ausgabeList.add(eingabeBuffer[position + 1]);
						ausgabeList.add(eingabeBuffer[position + 2]);
						position += 3;
					}
				}
			}
		}
		byte[] ausgabe = new byte[ausgabeList.size()];
		for (int i = 0; i < ausgabe.length; i++)
			ausgabe[i] = ausgabeList.get(i);
		return ausgabe;
	}
	
	/**
	 * leert einen der Buckets aus der Methode komprimiere_rle in die ausgabeListe
	 * 
	 * @param bucket       ByteBuffer, der geleert werden soll
	 * @param ausgabeListe in die der Bucket geleert werden soll
	 * @param bucketart    String "unterschiedlich" für den unterschiedlichBucket
	 *                     "wiederholung" für den wiederholungsBucket
	 * @return den geleerten Bucket mit ByteBuffer.position() = 0
	 */
	private ByteBuffer bucketLeeren(ByteBuffer bucket, ArrayList<Byte> ausgabeListe, String bucketart) {
		int anzahlBytes = 0;
		if (bucketart.equals("unterschiedlich")) {
			anzahlBytes = bucket.position();
			ausgabeListe.add((byte) (anzahlBytes / 3 - 1)); // die Position gibt an wie viele byte Gespeichert sind
			for (int i = 0; i < anzahlBytes; i++)
				ausgabeListe.add(bucket.get(i));
			bucket.clear();
		} else {
			anzahlBytes = bucket.position();
			ausgabeListe.add((byte) (anzahlBytes / 3 - 129)); // AnzahlBytes-1 -128 um die negative Zahl zu bekommen
			for (int i = 0; i < 3; i++)
				ausgabeListe.add(bucket.get(i));
			bucket.clear();
		}

		return bucket;
	}
	
	/**
	 * Nimmt eine rle Kodiertes byte-Array entgegen und gibt ein byte-Array aus, das
	 * nur die Pixel enthält. Die Steuerbytes werden im this-objekt gespeichert und
	 * müssen anschließend mit steuerbytes() abgefragt werden und können
	 * anschließend mit fusion(buffer, steuerbytes) zusammengefügt werden.
	 * 
	 * @param eingabeBuffer rle-Komprimierte Daten
	 * @return byte[] isolierte Pixel aus den rle-Komprimierten Daten
	 */
	public static byte[] extrahierePixel(byte[] eingabeBuffer) {
		ArrayList<Byte> ausgabeList = new ArrayList<Byte>();
		int position = 0;
		int counter;
		ArrayList<Byte> steuerbytes = new ArrayList<Byte>();

		if (remainingBuffer.length != 0) {
			byte[] bufferAndRemaining = new byte[remainingBuffer.length + eingabeBuffer.length];
			for (int i = 0; i < remainingBuffer.length; i++)
				bufferAndRemaining[i] = remainingBuffer[i];
			for (int j = remainingBuffer.length; j < bufferAndRemaining.length; j++)
				bufferAndRemaining[j] = eingabeBuffer[j - remainingBuffer.length];
			eingabeBuffer = bufferAndRemaining;
			remainingBuffer = new byte[0];
			bufferAndRemaining = new byte[0];
		}

		while (position != eingabeBuffer.length) {
			if (eingabeBuffer[position] < 0) { // das n�chste Pixel soll mehrfach geschrieben werden
				if (eingabeBuffer.length < position + 4 && FILE_MNG.remaining != 0) {
					remainingBuffer = new byte[eingabeBuffer.length - position];
					for (int i = position; i < eingabeBuffer.length; i++)
						remainingBuffer[i - position] = eingabeBuffer[i];
					break;
				} else {
					steuerbytes.add(eingabeBuffer[position]);
					position += 1;
					for (int i = 0; i < 3; i++) { // das n�chste Pixel wird extrahiert
						ausgabeList.add(eingabeBuffer[position]);
						position += 1;
					}
				}
			} else {
				counter = eingabeBuffer[position] + 1;
				if (eingabeBuffer.length < position + 1 + counter * 3 && FILE_MNG.remaining != 0) {
					remainingBuffer = new byte[eingabeBuffer.length - position];
					for (int i = position; i < eingabeBuffer.length; i++)
						remainingBuffer[i - position] = eingabeBuffer[i];
					break;
				} else {
					steuerbytes.add(eingabeBuffer[position]);
					position += 1;
					for (int i = 0; i < counter; i++) { // die n�chsten unterschiedlichen Pixel werden �bernommen
						ausgabeList.add(eingabeBuffer[position]);
						ausgabeList.add(eingabeBuffer[position + 1]);
						ausgabeList.add(eingabeBuffer[position + 2]);
						indexSpeicher.add(position);
						indexSpeicher.add(position + 1);
						indexSpeicher.add(position + 2);
						position += 3;
					}
				}
			}
		}
		steuerbyteBuffer = new byte[steuerbytes.size()];
		for (int i = 0; i < steuerbyteBuffer.length; i++) {
			steuerbyteBuffer[i] = steuerbytes.get(i);
		}
		byte[] ausgabe = new byte[ausgabeList.size()];
		for (int i = 0; i < ausgabeList.size(); i++)
			ausgabe[i] = ausgabeList.get(i);
		return ausgabe;
	}
	
	/**
	 * methode um die Steuerbytes auszugeben, die in der Methode
	 * extrahierePixel(buffer) gespeichert wurden. Falls keine Pixel extrahiert
	 * wurden, ist steuerbytes.length = 0
	 * 
	 * @return byte[] steuerbytes
	 */
	public static byte[] getSteuerbytes() {
		return steuerbyteBuffer;
	}
	
	/**
	 * Bringt einen byte-Array, der extrahierte Pixel enthält wieder mit seinen
	 * steuerbytes zusammen und gibt sie in der richtigen Reihenfolge für rle
	 * Kompimierung wieder aus
	 * 
	 * @param buffer      byte[] mit den extraierten Pixeln
	 * @param steuerbytes byte[] mit den zugehörigen Steuerbytes
	 * @return buffer rle komprimiert
	 */
	public static byte[] fusion(byte[] buffer, byte[] steuerbytes) {
		int position = 0;
		int counter;
		ArrayList<Byte> ausgabeList = new ArrayList<Byte>();
		for (int i = 0; i < steuerbytes.length; i++) {
			if (steuerbytes[i] < 0) {
				ausgabeList.add(steuerbytes[i]);
				for (int j = 0; j < 3; j++) {
					ausgabeList.add(buffer[position]);
					if (position + 1 != buffer.length)
						position += 1;
				}
			} else {
				ausgabeList.add(steuerbytes[i]);
				counter = steuerbytes[i] + 1;
				for (int j = 0; j < counter; j++) {
					ausgabeList.add(buffer[position]);
					ausgabeList.add(buffer[position + 1]);
					ausgabeList.add(buffer[position + 2]);
					if (position + 3 != buffer.length)
						position += 3;
				}
			}
		}
		byte[] ausgabe = new byte[ausgabeList.size()];
		for (int i = 0; i < ausgabe.length; i++)
			ausgabe[i] = ausgabeList.get(i);
		return ausgabe;
	}

}
