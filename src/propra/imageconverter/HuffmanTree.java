package propra.imageconverter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;


/**
 * Klasse zur Dekodierung von Huffman-kodierten Dateien
 * @author Annika Nöding
 *
 */
public class HuffmanTree {
	private Node root;
	private BufferedBitReader bbr;
	private long size;
	private HashMap<Byte, String> codeMap = new HashMap<Byte,String>();
	private StringBuilder treeCode = new StringBuilder();
	
	/**
	 * Konstruktor der Klasse <code>HuffmanTree</code>. Bekommt einen <code>FileInputStream</code> fis übergeben.
	 * Aus dem FileInputStream wird ein <code>BufferedBitReader</code> erstellt, der die Datei bitweise auslesen kann.
	 * @param fis <code>FileInputStream</code> der zu dekodierenden Datei
	 * @throws IOException
	 */
	public HuffmanTree(FileInputStream fis) throws IOException { 
		bbr = new BufferedBitReader(fis);
		this.root = buildDecodeTree(bbr, bbr.read());//TODO: das muss vermutlich aus dem Konstruktor herausgenommen werden.
	}
	public HuffmanTree() { 
	}
	
	/**
	 * Private rekursive Methode der Klasse <code>HuffmanTree</code> zur Erstellung des Dekodierbaums aus der Huffman-kodierten Datei.
	 * @param bbr BufferedBitReader, der die Datei ausliest
	 * @param bit der nächste ausgelesene bit-Wert aus der Datei
	 * @return Knoten <code> Node </code> mit zwei Söhnen. Bei Terminierung liefert die Methode den kompletten Baum
	 * Dabei sind in den Blättern die Byte-Werte gespeichert, die mit dem Baum kodiert sind.
	 * @throws IOException
	 */
	private Node buildDecodeTree(BufferedBitReader bbr, int bit) throws IOException{
		Node node = new Node();
		if (bit == 0) {
			node.left = buildDecodeTree(bbr, bbr.read());
			node.right = buildDecodeTree(bbr, bbr.read());
		}
		else {
			int[] bits = new int[8];
			for (int i = 0; i<8; i++)
				bits[i] = bbr.read();
			node.value = getByte(bits);
		}
		return node;
	}
	
	/**
	 * Private Methode der Klasse <code>HuffmanTree</code>. Wird in der privaten Klasse <code>buildTree</code> verwendet
	 * um einen byte-Wert aus einem eingelesenen bit-Array mit 8 Werten zu erstellen und sie im Blatt des dekodierungs-Baums
	 * zu speichern.
	 * @param bits2
	 * @return byte-Wert des ausgelesenen byte-Arrays bits.
	 */
	private byte getByte(int[] bits) {
		int result = 0;
		for (int i= 0; i<8; i++) {
			if (bits[i] == 1)
				result += Math.pow(2, 7-i);
		}
		return (byte) result;
	}
	
	/**
	 * Nutzt den aus der Huffman-Kodierten Datei erstellten Dekodierungsbaum und ließt so lange bits aus
	 * bis der Pfad im Baum zu einem Blatt führt. Gibt dann den im Blatt gespeicherten Byte-Wert aus oder -1
	 * wenn das Ende der Datei erreicht wurde.
	 * @return byte-Wert des nächsten kodierten Blattes oder -1 wenn alle Daten ausgelesen sind.
	 * @throws IOException
	 */
	public int readPath() throws IOException {
		Node currentNode = this.root;
		int currentBit;
		while (currentNode.left != null && (bbr.remaining > 0)) {
			currentBit = bbr.read();
			if (currentBit == 0)
				currentNode = currentNode.left;
			else
				currentNode = currentNode.right;
		}
		if (bbr.remaining==0)
			return -1;
		return currentNode.value;
	}
	
	
	/**
	 * Innere Klasse der Klasse <code>HuffmanTree</code> erstellt einen Knoten mit zwei Söhnen, 
	 * die mit null initialisiert sind, hat einen Speicherort <code>value</code> für den zu speichernden Wert, 
	 * wenn es sich um ein Blatt handelt.
	 * @author Annika Nöding
	 *
	 */
	private class Node{
		private Node left;
		private Node right;
		private int value;
		private float frequency;
		private Node father;
		public int location;
		
		private Node() {
			this.left = null;
			this.right = null;
		}
	}


	public void erstelleCodeBaum(HashMap<Byte, Integer> treeDictionary, long datasize) {
		this.size = datasize;
	    DictionaryList warteschlange = new DictionaryList();

		for(Entry<Byte, Integer> entry : treeDictionary.entrySet()) {
		    Byte key = entry.getKey();
		    Integer value = entry.getValue();
		    
		    warteschlange.sortiertEinfuegen(key, value, null); //jetzt ist dl aufsteigend sortiert nach der häufigkeit der bytewerte
		}
		byte b,c;
		int i, j;
		Node tp1, tp2;
		Node n, m;
		//Sonderfall: es gibt nur ein einziges Byte, das kodiert werden soll, dann muss ein Vaterknoten erstellt werden
		if (warteschlange.length == 1) {
			b=warteschlange.getNextByte();
			n = new Node();
			n.value = b;
			n.location = 0;
			//erstelle ein Dummy-Blatt für den rechten Kindknoten des Vaters
			m = new Node();
			m.value = 0;
			m.location = 1;
			Node father = new Node();
			father.left = n;
			father.right = m;
			n.father=father;
			m.father=father;
			warteschlange.deleteFirst();
			warteschlange.sortiertEinfuegen(0, father);
		}
		while (warteschlange.length > 1) {
			//Auslesen des nächsten byte-Wertes und dessen Häufigkeit
			b = warteschlange.getNextByte();
			i = warteschlange.getNextInt();
			tp1 = warteschlange.getTreePosition();
			warteschlange.deleteFirst();
			c = warteschlange.getNextByte();
			j = warteschlange.getNextInt();
			tp2 = warteschlange.getTreePosition();
			warteschlange.deleteFirst();
			//neue Knoten erstellen oder an einen bestehenden Vaterknoten anhängen
			if (tp1 == null) {
				n = new Node();
				n.value = b;
			} else
				n = tp1;
			if (tp2 == null) {	
				m = new Node();
				m.value = c;
			} else
				m = tp2;
			
			//neue Wurzel für die Knoten erstellen
			Node father = new Node();
			father.left = n;
			father.right = m;
			//Daten für die suche von Blatt nach Wurzel einfügen
			n.father = father;
			n.location = 0;
			m.father = father;
			m.location = 1;
			//Kiste neu einfügen
			warteschlange.sortiertEinfuegen(i+j, father);		
		}
		this.root = warteschlange.getTreePosition();
	}
	private class DictionaryList{
		private Kiste start;
		private int length = 0;

		private class Kiste{
			private byte key;
			private int value;
			private Node node;
			private Kiste next;
			
			private Kiste(byte key, int value, Node node) {
				this.key = key;
				this.value = value;
				this.node = node;
			}
		}	
		private void sortiertEinfuegen(byte key, int value, Node node) {
			Kiste neueKiste = new Kiste(key, value, node); 
			if (start == null) { // es ist noch keine Liste angefangen
				start = neueKiste;
			}
			else if (start.value >= value) { // vorne anfügen
				Kiste position = start;
				neueKiste.next = position;
				start = neueKiste;
			}
			else { // mitte oder hinten anfügen
				Kiste vorherigeKiste = start;
				Kiste position = start.next;
				while (position !=null && position.value < value) {
					vorherigeKiste = position;
					position = position.next;
				}
				vorherigeKiste.next = neueKiste;
				neueKiste.next = position;
			}
			this.length++;
		}
		
		public void sortiertEinfuegen(int value, Node father) {
			sortiertEinfuegen((byte) 0, value, father);
		}

		public byte getNextByte() {
			return start.key;
		}
		
		public int getNextInt() {
			return start.value;
		}
		
		public void deleteFirst() {
			start = start.next;
			length--;
		}
		
		/**
		 * Gibt den Vaterknoten zurück
		 * @return Node Vaterknoten
		 */
		public Node getTreePosition() {
			return start.node;
		}
	
		@Override
		public String toString() {
			String s = "";
			Kiste position = start;
			while (position != null) {
				s+=position.key+"="+position.value+ ", ";
				position = position.next;
			}
			return s;
		}
	}
	private Node searchLeave(Node node) {
		if(node.left != null ||node.right != null) {
			searchLeave(node.left);
			searchLeave(node.right);
		}
		return node;
	}


	public HashMap<Byte, String> getDictionary() {
		makeCodeMap(root, "");
		return this.codeMap;
	}
	
	/**
	 * erstellt ein Dictionary mit Huffmancodes für die Bytes
	 * @param node
	 * @param path
	 */
	private void makeCodeMap(Node node, String path) {
		if (node.left == null) {
			this.treeCode.append("1");
			this.treeCode.append(String.format("%8s", Integer.toBinaryString(node.value & 0xFF)).replace(' ', '0')); //der ByteWert, der das Blatt kodiert wird angehangen
			this.codeMap.put((byte) node.value, path); //beim sonderfall, dass es nur ein byte gibt, wird hier der code überschrieben, das ist aber nicht weiter schlimm
			path = ""; //TODO: kann womöglch weg
		}
		else {
			path+="0";
			this.treeCode.append("0");
			makeCodeMap(node.left, path);
			path = path.substring(0,path.length()-1); //man muss einen schritt zurück gehen, also letztes char weg
			path+="1";
			makeCodeMap(node.right, path);
		}
	}
	
	public StringBuilder getTreeCode() {
		return treeCode;
	}

}

