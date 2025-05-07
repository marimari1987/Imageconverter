# Bildkonverter

Ein Python-Tool zur Konvertierung von Bildern in verschiedene Formate.

## Features

- Konvertiert TGA, Propa
- Kompression mittels Huffman-Kodierung, Lauflängen Kodierung (RLE)
- CLI-Interface

## Ausführung (CLI)

Das Programm wird über die Kommandozeile gestartet:

```bash
java -Xmx256m propra.imageconverter.ImageConverter --input=../KE1_TestBilder/test_01_uncompressed.tga --output=../KE1_Konvertiert/test_01.propra

