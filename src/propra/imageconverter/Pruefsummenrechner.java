package propra.imageconverter;

import java.util.List;

/**
 * ehemals Klasse zur Berechnung der Prüfsumme, nun obsolet geworden,
 * da die Prüfsummenberechnung innerhalb der Klasse Propra durchgeführt wird.
 * @author Annika Nöding
 *
 */
public class Pruefsummenrechner {
	private final int X = 65521;
	private int startDatensegment;
	
	public long berechnePruefsumme(List<Byte> data, long n, String filetype) {
		switch (filetype) {
		case "tga":
			this.startDatensegment = 18;
			break;
		case "propra":
			this.startDatensegment = 30;
			break;

		default:
			break;
		}

		return (long) (berechneA(data, n, startDatensegment) * Math.pow(2, 16) + berechneB(data, n, startDatensegment));
	}

	public long berechneA(List<Byte> data, long n, int startDatensegm) {
		long ergebnis = 0;

		for (int i = 1; i <= n; i++) {
			ergebnis = (ergebnis + i + Byte.toUnsignedInt(data.get(i - 1 + startDatensegm)));
		}
		return ergebnis % X;
	}

	public long berechneB(List<Byte> data, long n, int startDatensegm) {
		long ergebnis = 0;
		long summeBytes = 0;
		long j; // braucht man an Stelle *)
		for (int i = 0; i <= n; i++) {
			if (i == 0)
				ergebnis += 1;
			else {
				j = i;
				summeBytes = summeBytes + Byte.toUnsignedInt(data.get(i - 1 + startDatensegm));
				ergebnis = ergebnis + (j * (j + 1) / 2) + summeBytes; // *) int i l�uft bei gro�en Datenmengen �ber
			}
		}
		return ergebnis % X;
	}
}
