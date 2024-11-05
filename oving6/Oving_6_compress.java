import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Oving_6_compress {
  public static void main(String[] args) throws IOException {
    Date start = new Date();

    File file = new File(args[0]);
    lempelZiv(file);
    huffmann();

    new File("lzcompressed").delete();

    Date end = new Date();
    System.out.println(end.getTime() - start.getTime());
  }

  static int[] findRepetitive(int i, byte[] data) {
    int resultJ = -1;
    int resultJSeq = -1;

    for (int j = i - 65_536; j < i; j++) {
      if (j < 0) {
        j = 0;
      }

      int jSeq;
      boolean foundSeq = false;

      if (data[i] == data[j]) {
        jSeq = j + 1;
        int iSeq = i + 1;
        while (!foundSeq) {
          if (jSeq - j < 32_767 && iSeq < data.length && data[iSeq] == data[jSeq]) {
            jSeq++;
            iSeq++;
          } else {
            jSeq--;
            foundSeq = true;
            if (resultJSeq - resultJ <= jSeq - j) {
              resultJSeq = jSeq;
              resultJ = j;
            }
          }
        }
      }
    }
    return new int[] {resultJ, resultJSeq};
  }

  static void lempelZiv(File file) throws IOException {
    try (DataInputStream inputFile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
         DataOutputStream outputStream = new DataOutputStream(
             new BufferedOutputStream(new FileOutputStream("lzcompressed")))) {

      byte[] data = new byte[(int) file.length()];
      inputFile.read(data);
      byte[] seqBuffer = new byte[65_536];
      int seqBufferIndex = 1;

      int resultJ;
      int resultJSeq;

      seqBuffer[0] = data[0];
      for (int i = 1; i < data.length; i++) {
        int[] result = findRepetitive(i, data);
        resultJ = result[0];
        resultJSeq = result[1];

        if (resultJSeq - resultJ > 3) {
          if(resultJ != -1) {
            i = i + (resultJSeq - resultJ) ;
          }
          if (seqBufferIndex > 0) {
            outputStream.writeShort(seqBufferIndex | 0x8000);
            outputStream.write(seqBuffer, 0, seqBufferIndex);
          }
          seqBuffer = new byte[65_536];
          seqBufferIndex = 0;
          short back = (short) (i - resultJ - (resultJSeq - resultJ));
          byte[] backwardsRef = {
              (byte) (((resultJSeq - resultJ) & 0xFF00) >> 8),
              (byte) ((resultJSeq - resultJ) & 0x00FF),
              (byte) ((back & 0xFF00) >> 8),
              (byte) (back & 0x00FF)};
          outputStream.write(backwardsRef);
        } else {
          if (resultJ != -1) {
            seqBuffer[seqBufferIndex++] = data[i];
          }
        }

        if (resultJ == -1) {
          seqBuffer[seqBufferIndex++] = data[i];
          if (seqBufferIndex > seqBuffer.length - 1) {
            outputStream.writeShort(seqBufferIndex | 0x8000);
            outputStream.write(seqBuffer, 0, seqBufferIndex);
            seqBuffer = new byte[65_536];
            seqBufferIndex = 0;
          }
        }
      }
      if (seqBufferIndex > 0) {
        outputStream.writeShort(seqBufferIndex | 0x8000);
        outputStream.write(seqBuffer, 0, seqBufferIndex);
      }
    }
  }

  static byte[] packData(byte[] input) {
    int FREQUENCYLENGTH = 256 * Integer.BYTES;

    int[] frekvensListe = generateFrequencyList(input);

    HuffmanNode rootNode = generateHuffmanTree(frekvensListe);

    Map<Byte, BitSetCollection> codeMap = generateCodes(rootNode);

    BitSet innpakketKode = new BitSet();

    // Konverterer Frekvenstabellen til bytes
    byte[] frekvensTabell = new byte[FREQUENCYLENGTH];
    int byteIndex = 0;
    for (int i = 0; i < frekvensListe.length; i++) {
      byte[] frekvensByte = ByteBuffer.allocate(Integer.BYTES).putInt(frekvensListe[i]).array();
      for (int l = 0; l < frekvensByte.length; l++) {
        frekvensTabell[byteIndex] = frekvensByte[l];
        byteIndex ++;
      }
    }

    int bitIndex = 0;

    for (byte b : input) {
      BitSetCollection bitSet = codeMap.get(b);
      for (int i = 0; i < bitSet.bitLength; i++) {
        innpakketKode.set(bitIndex, bitSet.bitSet.get(i));
        bitIndex++;
      }
    }

    BitSetCollection bitSetCollection = new BitSetCollection(innpakketKode, bitIndex);

    byte[] message = convertBitsToBytes(bitSetCollection);

    byte[] output = new byte[FREQUENCYLENGTH + message.length];
    System.arraycopy(frekvensTabell, 0, output, 0, FREQUENCYLENGTH);
    System.arraycopy(message, 0, output, FREQUENCYLENGTH, message.length);

    return output;
  }

  static int[] generateFrequencyList(byte[] input) {
    int FREQUENCYLENGTH = 256 * Integer.BYTES;

    int[] frekvensListe = new int[FREQUENCYLENGTH / Integer.BYTES];
    Arrays.setAll(frekvensListe, i -> 0);

    for (byte b : input) {
      frekvensListe[b & 0xFF] += 1;
    }
    return frekvensListe;
  }

  static HuffmanNode generateHuffmanTree(int[] frekvensListe) {
    PriorityQueue<HuffmanNode> nQueue = new PriorityQueue<>();

    for (int i = 0; i < frekvensListe.length; i++) {
      int element = frekvensListe[i];
      if (element > 0) {
        nQueue.add(new HuffmanNode((byte) i, element));
      }
    }

    return generateRootNodeFromMinHeap(nQueue);
  }

  static HuffmanNode generateRootNodeFromMinHeap(PriorityQueue<HuffmanNode> nQueue) {
    while (nQueue.size() > 1) {
      HuffmanNode a = nQueue.poll();
      HuffmanNode b = nQueue.poll();

      HuffmanNode combo = new HuffmanNode(a, b);
      nQueue.add(combo);
    }
    return nQueue.poll();
  }

  static Map<Byte, BitSetCollection> generateCodes(HuffmanNode rootNode) {
    if (rootNode == null || rootNode.getClass() != HuffmanNode.class) {
      throw new IllegalArgumentException(rootNode + " er enten null eller ikke en HuffmanNode");
    }
    Map<Byte, BitSetCollection> codeMap = new HashMap<>();

    exploreHuffmanNode(rootNode, new BitSet(), 0, codeMap);

    return codeMap;
  }

  static void exploreHuffmanNode(HuffmanNode node, BitSet bits, int bitIndex,
                                         Map<Byte, BitSetCollection> codeMap) {
    if (node.vNode == null & node.hNode == null) {
      codeMap.put(node.tegn, new BitSetCollection((BitSet) bits.clone(), bitIndex));
      return;
    }

    bits.clear(bitIndex);
    exploreHuffmanNode(node.vNode, bits, bitIndex + 1, codeMap);

    bits.set(bitIndex);
    exploreHuffmanNode(node.hNode, bits, bitIndex + 1, codeMap);
  }

  static byte[] convertBitsToBytes(BitSetCollection bits) {
    byte[] byteListe = bits.bitSet.toByteArray();
    byte[] numberOfBits = ByteBuffer.allocate(Long.BYTES).putLong(bits.bitLength).array();

    byte[] result = new byte[(int) (bits.bitLength + 7) / 8 + Long.BYTES];
    System.arraycopy(byteListe, 0, result, 0, byteListe.length);
    System.arraycopy(numberOfBits, 0, result, byteListe.length, Long.BYTES);
    return result;
  }

  static void huffmann() throws IOException {
    try (DataInputStream inputFile = new DataInputStream(new BufferedInputStream(new FileInputStream("lzcompressed")));
         DataOutputStream outputStream = new DataOutputStream(
             new BufferedOutputStream(new FileOutputStream("compressed.txt")))) {
      byte[] pakketInn = packData(inputFile.readAllBytes());
      outputStream.write(pakketInn);
    }
  }
}

class BitSetCollection {
  public BitSet bitSet;
  public long bitLength;

  public BitSetCollection(BitSet bitSet, long bitLength) {
    this.bitSet = bitSet;
    this.bitLength = bitLength;
  }
}

class HuffmanNode implements Comparable<HuffmanNode> {
  final Byte tegn;
  int frekvens;
  HuffmanNode vNode;
  HuffmanNode hNode;

  public HuffmanNode(Byte tegn, int frekvens) {
    this.tegn = tegn;
    this.frekvens = frekvens;
    this.vNode = null;
    this.hNode = null;
  }

  public HuffmanNode(HuffmanNode vNode, HuffmanNode hNode) {
    this.tegn = null;
    this.frekvens = vNode.frekvens + hNode.frekvens;
    this.vNode = vNode;
    this.hNode = hNode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    HuffmanNode node = (HuffmanNode) obj;
    return frekvens == node.frekvens;
  }

  @Override
  public int compareTo(HuffmanNode other) {
    return Integer.compare(this.frekvens, other.frekvens);
  }
}
