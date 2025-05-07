# Bildkonverter

Konsolenbasiertes Programm zur Bildkonvertierung und Kompression

## Propra Format

![image](https://github.com/user-attachments/assets/53bdb204-b9b5-4228-a66e-736bee0e9c99)


## Features

- Konvertiert TGA, Propa
- Kompression mittels Huffman-Kodierung
- Kompression mittels Lauflängen Kodierung (RLE)
- Base32-Kodierung (und BaseN-Kodierung) zur Umwandlung von Bildern in Textform
- Das Programm kann mit begrenztem Speicher auch sehr große Bilder korrekt verarbeiten
- CLI-Interface

## Fehlererkennung
- inkonsistente Bilddimension (Nullbreite/Nullhöhe)
- nicht unterstützter/unbekannter Bildtyp/Kompression
- fehlende Daten: Pixelanzahl aus Daten im Header passt nicht mit der Datenmenge
- zu viele Bilddaten: Pixelanzahl aus Daten im Header passt nicht mit der Datenmenge (nur ProPra-Format)
- Überprüfen der Prüfsumme (nur ProPra-Format)
- falsche Dateigröße im Header (nur ProPra-Format)

## Ausführung (CLI)

Das Programm wird über die Kommandozeile gestartet:

```bash
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE1_TestBilder/test_01_uncompressed.tga --output=../KE1_Konvertiert/test_01.propra
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE2_TestBilder/test_01_uncompressed.tga --output=../KE2_Konvertiert/test_01.propra --compression=rle
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE2_TestBilder/test_02_rle.tga          --output=../KE2_Konvertiert/test_02.propra --compression=uncompressed
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE2_TestBilder/test_05_base32.tga.base-32    --decode-base-32
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE2_TestBilder/test_02_rle.tga               --encode-base-32
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE3_TestBilder/test_05_huffman.propra      --output=../KE3_Konvertiert/test_05.tga  --compression=rle


