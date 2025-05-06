package test;

import propra.imageconverter.ImageConverter;

public class ImageConverterTester {
	public static void main(String[] args) {
		long time = System.currentTimeMillis();  
		String[][] dateien = {
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_02_rle.tga","--output=C:/Users/Bernd/Desktop/Test_Konvertiert/test_02_uncompressed.propra","--compression=uncompressed"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_05_uncompressed.tga","--output=C:/Users/Bernd/Desktop/Test_Konvertiert/test_05_rle.propra","--compression=rle"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_05_uncompressed.propra","--output=C:/Users/Bernd/Desktop/Test_Konvertiert/test_05_rle.tga","--compression=rle"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_05_rle.propra","--output=C:/Users/Bernd/Desktop/Test_Konvertiert/test_05_rle_nach_rle.tga","--compression=rle"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_05_huffman.propra","--output=C:/Users/Bernd/Desktop/Test_Konvertiert/test_05_uncompressed_from_huffman.propra","--compression=uncompressed"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_02_rle.tga","--encode-base-32"},
				{"--input=C:/Users/Bernd/Desktop/Abschlusstest/test_05_base32.tga.base-32","--decode-base-32"}
		};
		for (String[] datei : dateien)
			ImageConverter.main(datei);
	}
	
}
