package propra.imageconverter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Klasse, die zum Auslesen einzelner bits aus einem Datenstrom (<code>FileInputStream</code>) verwendet werden kann.  
 * Dem BufferedBitReader liegt ein <code>BufferedInputStream</code> zu Grunde. Der es ermöglicht die Daten gepuffert auszulesen.
 * @author Annika Nöding
 *
 */
public class BufferedBitReader extends BufferedInputStream{
	
	private int[] bits = new int[8];
	private int currentBit = 0;
	/**
	 * Gibt die Gesamtanzahl der auslesbaren bits aus.
	 */
	public long remaining;
	
	public BufferedBitReader(FileInputStream fis) throws IOException {
		super(fis);
		remaining = super.available()*8;
	}
	
	@Override
	public int read() throws IOException {
		int b;
		if (currentBit == 8 && remaining != 0)
			currentBit = 0;
		if (currentBit == 0) { //wenn 8 bit auslgelesen wurden, muss wieder ein neues Byte aus dem BufferedReader gelesen werden
			b = super.read();
			int bit;
			for (int i = 0; i<8; i++) {
				bit = (b >>> 7-i) & 0x1;
				this.bits[i] = bit;
			}
		}
		remaining --;    
		return bits[currentBit++];
	}
}
