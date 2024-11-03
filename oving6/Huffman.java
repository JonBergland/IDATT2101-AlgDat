import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        String streng = "åei ieiei eiiei ieh iehei ";
        System.out.println(streng);
        Huffman huffman = new Huffman();

        byte[] bytes = huffman.encodeStringToBytes(streng);

        int[] frekvensListe = huffman.generateFrequencyList(bytes);

        HuffmanNode rootNode = huffman.generateHuffmanTree(frekvensListe);

        Map<Byte, List<Boolean>> codeMap = huffman.generateCodes(rootNode);

        byte[] pakketInn = huffman.packData(codeMap, streng.getBytes(), frekvensListe);

        System.out.println("Lengde på komprimert fil: " + pakketInn.length);

        byte[] pakketUt = huffman.unpackData(pakketInn);

        String result = huffman.decodeBytesToString(pakketUt);

        System.out.println(result);

    }
}

class Huffman {
    private final int FREQUENCYLENGTH = 256;
    
        public Huffman() {
        }
    
        public HuffmanNode generateHuffmanTree(int[] frekvensListe) {
            PriorityQueue<HuffmanNode> nQueue = new PriorityQueue<>();
    
            for (int i = 0; i < frekvensListe.length; i++) {
                int element = frekvensListe[i];
                if (element > 0) {
                    nQueue.add(new HuffmanNode((byte) i,element));
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
    
        public Map<Byte, List<Boolean>> generateCodes(HuffmanNode rootNode) {
            if (rootNode == null || rootNode.getClass() != HuffmanNode.class) {
                throw new IllegalArgumentException(rootNode + " er enten null eller ikke en HuffmanNode");
            }
            Map<Byte, List<Boolean>> codeMap = new HashMap<>();
    
            exploreHuffmanNode(rootNode, new ArrayList<>(), codeMap);
    
            return codeMap;
        }
    
        private void exploreHuffmanNode(HuffmanNode node, List<Boolean> bits, Map<Byte, List<Boolean>> codeMap) {
            if (node.vNode == null & node.hNode == null) {
                codeMap.put(node.tegn, new ArrayList<>(bits));
                return;
            }
    
            bits.add(false);
            exploreHuffmanNode(node.vNode, bits, codeMap);
            bits.remove(bits.size() -1);
    
            bits.add(true);
            exploreHuffmanNode(node.hNode, bits, codeMap);
            bits.remove(bits.size() -1);
        }
    
        public byte[] packData(Map<Byte, List<Boolean>> codeMap, byte[] input, int[] frekvensListe) {
            List<Boolean> innpakketKode = new ArrayList<>();
    
            // Konverterer Frekvenstabellen til bytes
            byte[] frekvensTabell = new byte[this.FREQUENCYLENGTH];
            for (int i = 0; i < frekvensListe.length; i++) {
                frekvensTabell[i] = (byte) frekvensListe[i];
            }
    
            for (byte b : input) {
                innpakketKode.addAll(codeMap.get(b));
            }
    
            byte[] message = convertBitsToBytes(innpakketKode);
    
            byte[] output = new byte[this.FREQUENCYLENGTH + message.length];
            System.arraycopy(frekvensTabell, 0, output, 0, this.FREQUENCYLENGTH);
            System.arraycopy(message, 0, output, this.FREQUENCYLENGTH, message.length);
    
    
            return output;
        }
    
        public byte[] unpackData(byte[] input) {
        // Hent ut frekvens tabellen
        byte[] frekvensTabell = new byte[this.FREQUENCYLENGTH];
        if (input.length < frekvensTabell.length) {
            throw new IllegalArgumentException("Input er mangler frekvenstabell");
        }
        System.arraycopy(input, 0, frekvensTabell, 0, this.FREQUENCYLENGTH);
        int[] frekvensListe = new int[this.FREQUENCYLENGTH];
        Arrays.setAll(frekvensListe, i -> 0);

        for (int i = 0; i < this.FREQUENCYLENGTH; i ++) {
            if (frekvensTabell[i] > 0) {
                frekvensListe[i] = (int) input[i];
            }
        }

        HuffmanNode rootNode = generateHuffmanTree(frekvensListe);
        
        byte[] fil = new byte[input.length - this.FREQUENCYLENGTH];
        System.arraycopy(input, this.FREQUENCYLENGTH, fil, 0, fil.length);

        List<Boolean> bits = convertBytesToBits(fil);
        HuffmanNode node = rootNode;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            for(Boolean bool : bits) {
                if (bool) {
                    node = node.hNode;
                } else {
                    node = node.vNode;
                }
    
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

    private byte[] convertBitsToBytes(List<Boolean> bits) {
        int byteLengde = (bits.size() + 7) / 8;
        byte[] byteListe = new byte[byteLengde + 1];

        for (int i = 0; i < bits.size(); i++) {
            if(bits.get(i)) {
                byteListe[i/8] |= (1 << (7 - (i % 8)));
            }
        }

        //Legger på lengden på de gyldige bitsene i byteListe 
        byteListe[byteLengde] = (byte) bits.size();
        return byteListe;
    }
    
    private List<Boolean> convertBytesToBits(byte[] byteListe) {
        int gydligeBits = (int) byteListe[byteListe.length - 1];
        List<Boolean> innpakketKode = new ArrayList<>();

        for (byte b : byteListe) {
            for (int i = 7; i >= 0; i--) {
                boolean bit = (b & (1 << i)) != 0;
                innpakketKode.add(bit);
            }
        }

        return new ArrayList<Boolean>(innpakketKode.subList(0, gydligeBits));
    }

    public int[] generateFrequencyList(byte[] input) {
        int[] frekvensListe = new int[this.FREQUENCYLENGTH];
        Arrays.setAll(frekvensListe, i -> 0);

        for (byte b : input) {
            frekvensListe[b & 0xFF] += 1;
        }
        return frekvensListe;
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

    public HuffmanNode (HuffmanNode vNode, HuffmanNode hNode) {
        this.tegn = null;
        this.frekvens = vNode.frekvens + hNode.frekvens;
        this.vNode = vNode;
        this.hNode = hNode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HuffmanNode node = (HuffmanNode) obj;
        return frekvens == node.frekvens;
   }

   @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frekvens, other.frekvens);
    }
}
