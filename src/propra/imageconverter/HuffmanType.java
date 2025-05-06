package propra.imageconverter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HuffmanType extends CompressionType {
//
//	public static HuffmanTree tree;
//	private boolean treeLoaded = false;
//	private HashMap<Byte, String> decodeDictionary;
//	private StringBuilder bitString = new StringBuilder();
//	public static BufferedInputStream bis;
//
//	public HuffmanType() {
//		bis = new BufferedInputStream(FILE_MNG.fis);
//	}
//
//
//
//	@Override
//	public byte[] compress(byte[] buffer) {
//		int intNumber;
//		List<Byte> outputList = new ArrayList<Byte>();
//
//		if (!treeLoaded) { // für das erste Schreiben, muss zunächst das Code-Wörterbuch erstellt werden und der Baum in die Datei geschrieben werden
//			decodeDictionary = tree.getDictionary();
////			System.out.println("decodeDictionary" + decodeDictionary);
//			bitString.append(tree.getTreeCode());
//			while(bitString.length()>=8) { //könnte auch raus, wird im Prinzip am Ende der Datei automatisch gemacht. Finde es aber so übersichtlicher
//				intNumber = Integer.parseInt(bitString.substring(0, 8),2);
//				outputList.add((byte) intNumber);
//				bitString.delete(0, 8);
//			}
//			treeLoaded = true;
//		}
//		for(byte b : buffer) {
//			bitString.append(decodeDictionary.get(b));
//			while (bitString.length()>=8) {
//				intNumber = Integer.parseInt(bitString.substring(0, 8),2);
//				outputList.add((byte) intNumber);
//				bitString.delete(0, 8);
//			}
//		}
//		if (FILE_MNG.remaining == 0 && bitString.length()!=0) {
//			while (bitString.length()!=8)
//				bitString.append("0");
//			intNumber = Integer.parseInt(bitString.toString(),2);
//			outputList.add((byte) intNumber);
//		}
//		byte[] outputBuffer = new byte[outputList.size()];
//		int i = 0;
//		for (byte b : outputList)
//			outputBuffer[i++] = b;
//		return outputBuffer;
//	}
//
//	@Override
//	public byte[] decompress(byte[] buffer) throws IOException {
//		byte[] returnBuffer = new byte[3*1024];
//		byte currentByte;
//		if (FILE_MNG.tree == null) {
//			FILE_MNG.raf.seek(30); //TODO: wieso muss man hier erst die position suchen?
//			tree = new HuffmanTree(FILE_MNG.fis);
//		}
//		if (FILE_MNG.fis.available()== 0) { //fis= fileInputStream ; bis = BufferedInputStream
//			//rest aus dem bis auslesen
//			returnBuffer = new byte[bis.available()*8];
//			byte[] tempBuffer; 
//			for (int i = 0; i<returnBuffer.length; i++) {
//				int value = tree.readPath();
//				if (value == -1) {
//					tempBuffer = new byte[i];
//					for (int j= 0; j<i; j++)
//						tempBuffer[j] = returnBuffer[j]; 
//					returnBuffer = tempBuffer;
//					break;
//				}
//				else {
//					currentByte = (byte) value;
//					returnBuffer[i] = currentByte;
//				}
//			}
//			FILE_MNG.remaining = 0;
//		}
//		else {
//			for (int i = 0; i<returnBuffer.length; i++) {
//				currentByte = (byte) tree.readPath();
//				returnBuffer[i] = currentByte;
//			}
//		}
//		return returnBuffer;
//	}
//	
//	public static void erstelleHuffmanCode(int headerSize) throws IOException {
//		byte b;
//		HashMap<Byte, Integer> treeDictionary = new HashMap<Byte, Integer>();
//		FILE_MNG.raf.seek(headerSize); 
//		long datasize = bis.available();
//		long remaining = bis.available();
//		while (remaining > 0) {
//			b = (byte) bis.read();
//			if (!treeDictionary.containsKey(b)) {
//				treeDictionary.put(b, 1);
//			}
//			else
//				treeDictionary.put(b, treeDictionary.get(b)+1);
//			remaining--;
//		}
////		System.out.println(treeDictionary);
//		tree = new HuffmanTree();
//		tree.erstelleCodeBaum(treeDictionary, datasize); //TODO: Code für die Blätter: while(location != null) += 2^i++ 
//	}
//
}
