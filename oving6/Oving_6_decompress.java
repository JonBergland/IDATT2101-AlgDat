import java.io.*;
import java.util.Date;
import java.nio.ByteBuffer;
import java.util.PriorityQueue;

public class Oving_6_decompress {
  public static void main(String[] args) throws IOException {
    Date start = new Date();

    if (args.length == 1) {
      File compressedFile = new File(args[0]);
      File decompressedFile = new File("decompressed.txt");

      decompressedFile.delete();

      huffmann(compressedFile);
      lempelZiv(decompressedFile);
    } else {
      System.out.println("The program must be run like this: java Oving_6_decompress [filename] ");
    }


    new File("huffmann").delete();

    Date end = new Date();
    System.out.println("Elapsed time:  " + (end.getTime() - start.getTime()) + " ms");
    System.out.println("Original file size: " + new File(args[0]).length() + " bytes");
    System.out.println("New file size: " + new File("decompressed.txt").length() + " bytes");
  }

  static int forRef(DataInputStream inputStream, char currentByte, File decompressedFile)
      throws IOException {
    int writeCount = 0;
    try (DataOutputStream outputStream = new DataOutputStream(
        new BufferedOutputStream(new FileOutputStream(decompressedFile, true)))) {

      int forwardSteps = ((currentByte & 0x7F) << 8) | inputStream.read();
      for (int i = 0; i < forwardSteps; i++) {
        outputStream.write(inputStream.read());
        writeCount++;
      }
    }
    return writeCount;
  }

  static int backRef(
      DataInputStream inputStream, char currentByte, File decompressedFile, int writeCount)
      throws IOException {
    try (DataInputStream inputStreamFromOutput = new DataInputStream(
        new BufferedInputStream(new FileInputStream(decompressedFile)))) {

      int forwardSteps = (((currentByte & 0x7F) << 8) | inputStream.read()) + 1;
      int backwardSteps = (inputStream.read() << 8 | inputStream.read());
      inputStreamFromOutput.skipBytes(writeCount - backwardSteps);
      try (DataOutputStream outputStream = new DataOutputStream(
          new BufferedOutputStream(new FileOutputStream(decompressedFile, true)))) {

        for (int i = 0; i < forwardSteps; i++) {
          outputStream.write(inputStreamFromOutput.read());
          outputStream.flush();
          writeCount++;
        }
      }
    }
    return writeCount;
  }

  static void lempelZiv(File decompressedFile) throws IOException {
    int writeCount = 0;
    try (DataInputStream inputStream = new DataInputStream(
        new BufferedInputStream(new FileInputStream("huffmann")))) {

      int byteRead;
      while ((byteRead = inputStream.read()) != -1) {
        char currentByte = (char) byteRead;
        int firstBit = (currentByte & 0x80) >>> 7;
        switch (firstBit) {
          case 0:
            writeCount = backRef(inputStream, currentByte, decompressedFile, writeCount);
            break;
          case 1:
            writeCount += forRef(inputStream, currentByte, decompressedFile);
            break;
        }
      }
    }
  }

  static byte[] unpackData(byte[] input) {
    int FREQUENCYLENGTH = 256 * Integer.BYTES;

    // Hent ut frekvenstabellen
    byte[] frekvensTabell = new byte[FREQUENCYLENGTH];
    if (input.length < frekvensTabell.length) {
      throw new IllegalArgumentException("Input er mangler frekvenstabell");
    }
    System.arraycopy(input, 0, frekvensTabell, 0, FREQUENCYLENGTH);
    int[] frekvensListe = new int[FREQUENCYLENGTH / Integer.BYTES];

    int byteIndex = 0;
    for (int i = 0; i < frekvensListe.length; i += 1) {
      byte[] gydligeBitsArray = new byte[Integer.BYTES];
      System.arraycopy(frekvensTabell, byteIndex, gydligeBitsArray, 0, Integer.BYTES);
      frekvensListe[i] = ByteBuffer.wrap(gydligeBitsArray).getInt();
      byteIndex += Integer.BYTES;
    }

    HuffmanNode rootNode = generateHuffmanTree(frekvensListe);

    byte[] data = new byte[input.length - FREQUENCYLENGTH - Long.BYTES];
    System.arraycopy(input, FREQUENCYLENGTH, data, 0, data.length);

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

  static void huffmann(File file) throws IOException {
    try (DataInputStream inputFile = new DataInputStream(
        new BufferedInputStream(new FileInputStream(file)));
         DataOutputStream outputStream = new DataOutputStream(
             new BufferedOutputStream(new FileOutputStream("huffmann")))) {
      byte[] pakketUt = unpackData(inputFile.readAllBytes());
      outputStream.write(pakketUt);
    }
  }
}
