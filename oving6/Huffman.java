import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class innpakking {
    public static void main(String[] args) {
        Run runInstance = new Run();
        runInstance.run();
    }
}

class Run {
    public void run() {
        String streng = "Dette er en streng +++00dsjdsjafhadjasfljadbadn" 
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n"
        + "Dette er en reppererende streng\n";
        System.out.println(streng);
        Huffman huffman = new Huffman();

        byte[] pakketInn = huffman.packData(huffman.encodeStringToBytes(streng));

        System.out.println("Lengde på komprimert fil: " + pakketInn.length);

        byte[] pakketUt = huffman.unpackData(pakketInn);

        String result = huffman.decodeBytesToString(pakketUt);

        System.out.println(result);

        System.out.println(streng.equals(result));

    }
}

class Huffman {
    private final int FREQUENCYLENGTH = 256 * Integer.BYTES;

    public Huffman() {
    }

    public byte[] packData(byte[] input) {
        int[] frekvensListe = generateFrequencyList(input);
        HuffmanNode rootNode = generateHuffmanTree(frekvensListe);
        Map<Byte, BitCollection> codeMap = generateCodes(rootNode);

        int antallBits = 0;
        for (byte b : input) {
            antallBits += codeMap.get(b).bitLength;
        }

        int antallBytes = (antallBits + 7) / 8;
        byte[] pakketData = new byte[antallBytes];


        int bitIndex = 0;

        for (byte b : input) {
            BitCollection bits = codeMap.get(b);
            for (int i = 0; i < bits.bitLength; i++) {
                boolean bit = bits.getBit(i);
                BitUtils.setBit(pakketData, bitIndex++, bit);
            }
        }

        byte[] numberOfBits = ByteBuffer.allocate(Long.BYTES).putLong(bitIndex).array();

        byte[] result = new byte[(int) (bitIndex + 7) / 8 + Long.BYTES];
        System.arraycopy(pakketData, 0, result, 0, pakketData.length);
        System.arraycopy(numberOfBits, 0, result, result.length - Long.BYTES, Long.BYTES);


        // Konverterer Frekvenstabellen til bytes
        byte[] frekvensTabell = new byte[this.FREQUENCYLENGTH];
        int byteIndex = 0;
        for (int i = 0; i < frekvensListe.length; i++) {
            byte[] frekvensByte = ByteBuffer.allocate(Integer.BYTES).putInt(frekvensListe[i]).array();
            for (int l = 0; l < frekvensByte.length; l++) {
                frekvensTabell[byteIndex] = frekvensByte[l];
                byteIndex ++;
            }
        }
        byte[] output = new byte[this.FREQUENCYLENGTH + result.length];
        System.arraycopy(frekvensTabell, 0, output, 0, this.FREQUENCYLENGTH);
        System.arraycopy(result, 0, output, this.FREQUENCYLENGTH, result.length);

        return output;
    }

    public byte[] unpackData(byte[] input) {
        // Hent ut frekvenstabellen
        byte[] frekvensTabell = new byte[this.FREQUENCYLENGTH];
        if (input.length < frekvensTabell.length) {
            throw new IllegalArgumentException("Input er mangler frekvenstabell");
        }
        System.arraycopy(input, 0, frekvensTabell, 0, this.FREQUENCYLENGTH);
        int[] frekvensListe = new int[this.FREQUENCYLENGTH / Integer.BYTES];

        int byteIndex = 0;
        for (int i = 0; i < frekvensListe.length; i += 1) {
            byte[] gydligeBitsArray = new byte[Integer.BYTES];
            System.arraycopy(frekvensTabell, byteIndex, gydligeBitsArray, 0, Integer.BYTES);
            frekvensListe[i] = ByteBuffer.wrap(gydligeBitsArray).getInt();
            byteIndex += Integer.BYTES;
        }

        HuffmanNode rootNode = generateHuffmanTree(frekvensListe);

        byte[] data = new byte[input.length - this.FREQUENCYLENGTH - Long.BYTES];
        System.arraycopy(input, this.FREQUENCYLENGTH, data, 0, data.length);

        byte[] gydligeBitsArray = new byte[Long.BYTES];
        System.arraycopy(input, input.length - Long.BYTES, gydligeBitsArray, 0, Long.BYTES);
        long gyldigeBits = ByteBuffer.wrap(gydligeBitsArray).getLong();

        //BitSetCollection bits = convertBytesToBits(data);
        HuffmanNode node = rootNode;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < gyldigeBits; i++) {
                boolean bit = BitUtils.getBit(data, i);
                node = bit ? node.hNode : node.vNode;

                if (node.hNode == null && node.vNode == null) {
                    byteArrayOutputStream.write(node.tegn);
                    node = rootNode;
                }
            }

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error while unpacking bytes", e);
        }
    }

    public int[] generateFrequencyList(byte[] input) {
        int[] frekvensListe = new int[this.FREQUENCYLENGTH / Integer.BYTES];
        Arrays.setAll(frekvensListe, i -> 0);

        for (byte b : input) {
            frekvensListe[b & 0xFF] += 1;
        }
        return frekvensListe;
    }

    public HuffmanNode generateHuffmanTree(int[] frekvensListe) {
        PriorityQueue<HuffmanNode> nQueue = new PriorityQueue<>();

        for (int i = 0; i < frekvensListe.length; i++) {
            int element = frekvensListe[i];
            if (element > 0) {
                nQueue.add(new HuffmanNode((byte) i, element));
            }
        }

        return generateRootNodeFromMinHeap(nQueue);
    }

    public HuffmanNode generateRootNodeFromMinHeap(PriorityQueue<HuffmanNode> nQueue) {
        while (nQueue.size() > 1) {
            HuffmanNode a = nQueue.poll();
            HuffmanNode b = nQueue.poll();

            HuffmanNode combo = new HuffmanNode(a, b);
            nQueue.add(combo);
        }
        return nQueue.poll();
    }

    public Map<Byte, BitCollection> generateCodes(HuffmanNode rootNode) {
        if (rootNode == null || rootNode.getClass() != HuffmanNode.class) {
            throw new IllegalArgumentException(rootNode + " er enten null eller ikke en HuffmanNode");
        }

        Map<Byte, BitCollection> codeMap = new HashMap<>();

        exploreHuffmanNode(rootNode, 0L, 0, codeMap);

        return codeMap;
    }

    private void exploreHuffmanNode(HuffmanNode node, long bits, int bitLength, Map<Byte, BitCollection> codeMap) {
        if (node.vNode == null & node.hNode == null) {
            codeMap.put(node.tegn, new BitCollection(bits, bitLength));
            return;
        }

        exploreHuffmanNode(node.vNode, bits << 1, bitLength + 1, codeMap);

        exploreHuffmanNode(node.hNode, (bits << 1) | 1, bitLength + 1, codeMap);
    }

    public byte[] encodeStringToBytes(String input) {
        try {
            return input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }
    }

    public String decodeBytesToString(byte[] input) {
        try {
            return new String(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding is not supported", e);
        }
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

class BitSetCollection {
    public BitSet bitSet;
    public long bitLength;

    public BitSetCollection(BitSet bitSet, long bitLength) {
        this.bitSet = bitSet;
        this.bitLength = bitLength;
    }
}

/*
 * En utils-klasse for manipulering av bits
 */
class BitUtils {
    public static boolean getBit(byte[] byteArray, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);
        return ((byteArray[byteIndex] >> bitPosition) & 1) == 1;
    }

    public static void setBit(byte[] byteArray, int bitIndex, boolean value) {
        int byteIndex = bitIndex / 8;
        int bitPosition = 7 - (bitIndex % 8);

        if (value) {
            byteArray[byteIndex] |= (1 << bitPosition);
        } else {
            byteArray[byteIndex] &= ~(1 << bitPosition);
        }
    }
}

/*
 * En data klasse for bit
 */
class BitCollection {
    private long bit;
    public int bitLength;

    public BitCollection(long bit, int bitLength) {
        this.bit = bit;
        this.bitLength = bitLength;
    }

    public boolean getBit(int index) {
        return ((bit >> (bitLength - index - 1)) & 1) == 1;
    }
}
