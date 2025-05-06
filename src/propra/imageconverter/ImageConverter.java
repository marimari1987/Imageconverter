package propra.imageconverter;

import java.io.IOException;

public class ImageConverter {
	static String inputPath;
	static String outputPath;
	static String compression;
	static CompressionType toCompressionType;
	static String codingType;
	static Bilddatei fromType;
	static Bilddatei toType;

	public static void main(String[] args) {
		long time = System.currentTimeMillis();

		try {
			
			for (String arg : args) {
				System.out.print(arg + " ");
			}
			Path_MNG path_mng = new Path_MNG(args);
			if (path_mng.hasArguments) {
				inputPath = path_mng.getInputString();
				outputPath = path_mng.getOutputString();
				compression = path_mng.getCompression();
				toCompressionType = path_mng.getToCompressionType();
				codingType = path_mng.getCodingType();
				fromType = path_mng.getType(inputPath);
				toType = path_mng.getType(outputPath);
				
				if (codingType != "") {
					switch (codingType) {
					case "decode": {
						FILE_MNG file_mng;
						if (inputPath.endsWith(".base-32")) {
							String outputPath = inputPath.substring(0, inputPath.length()-8);
							System.out.println("Die Datei wird gespeichert unter: "+outputPath);
							file_mng = new FILE_MNG();
						}else {
							String outputPath = inputPath.substring(0, inputPath.length()-7);
							System.out.println("Die Datei wird gespeichert unter: "+outputPath);
							file_mng = new FILE_MNG();
						}
						file_mng.decode();
						break;
					}
					case "encode":{
						String base = path_mng.getBase();
						FILE_MNG file_mng;
						if (base.startsWith("base-32")) {
							file_mng = new FILE_MNG();
							file_mng.encode();
						}
						else {
							file_mng = new FILE_MNG();
							file_mng.encode(path_mng.getAlphabet());
						}
						break;
					}
					default:
						throw new IllegalArgumentException("Unexpected value: " + codingType);
					}
					
				}
				else {
					if (toCompressionType instanceof HuffmanType) {
						FILE_MNG filemng = new FILE_MNG();
						filemng.erstelleHuffmanCode(fromType.getHeaderSize());
					}  
					fromType.konvertiere(toType, compression);     
				}
			}
			else {
				path_mng.getPossibleArguments();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			System.exit(123);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(123);

//		} catch (InconsistentFileException e) {
//			System.out.println(e.getMessage());
//			System.exit(123);
		} catch (WrongInputException e) {
			System.out.println(e.getMessage());
			System.exit(123);
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(123);
		}
		long durationtime = System.currentTimeMillis() - time;
		System.out.println("Durationtime: " +durationtime);

	}

}
