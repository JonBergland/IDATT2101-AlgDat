import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class oving4 {
  public static void main(String[] args) {
    Run runInstance = new Run();
    runInstance.run();
  }
}

class Run {
  public void run() {
    HashtabellWithStrings hashTabell = new HashtabellWithStrings();

    try (Scanner scanner = new Scanner(new File("navn.txt"))) {
    while (scanner.hasNextLine()) {
    String nextLine = scanner.nextLine();
    hashTabell.addValue(nextLine);
    }
    System.out.println("Hent mitt navn: " + hashTabell.getValue("Jon Bergland"));
    System.out.println("Antall elementer: " + hashTabell.antElements);
    System.out.println("Lastfaktor: " + hashTabell.getLoadFactor());
    System.out.println("Antall kollisjoner: " + hashTabell.collisions);
    System.out.println("Kollisjoner per person: " + (double)hashTabell.collisions
    / hashTabell.antElements);

    } catch (IOException e) {
    throw new RuntimeException(e);
    }

    System.out.println("\nHashtabell med heltall:");

    HashtabellWithInt hashTabellInt = new HashtabellWithInt();
    List<Integer> randomNumbers = generateRandomNumbers(10_000_000, 1_000_000_000);

    long beforeTime = System.currentTimeMillis();
    for (int i = 0; i < randomNumbers.size(); i++) {
      hashTabellInt.addValue(randomNumbers.get(i));
    }
    long afterTime = System.currentTimeMillis();

    System.out.println("Tidsforbruk på 10_000_000 tall: " + (afterTime - beforeTime) + "ms");
    System.out.println("Antall elementer i hashtabell: " + hashTabellInt.antElements);
    System.out.println("Lastefaktor: " + hashTabellInt.getLoadFactor());
    System.out.println("Kollisjoner: " + hashTabellInt.collisions);
    System.out.println("Kollisjoner per element: " + (double) hashTabellInt.collisions / hashTabellInt.antElements);

    HashMap<Integer, Integer> hashMap = new HashMap<>();
    beforeTime = System.currentTimeMillis();
    for (int i = 0; i < randomNumbers.size(); i++) {
      hashMap.put(randomNumbers.get(i), randomNumbers.get(i));
    }
    afterTime = System.currentTimeMillis();
    System.out.println("Java.Hashmap");
    System.out.println("Tidsforbruk for java.Hashmap på 10_000_000 tall: " + (afterTime - beforeTime) + "ms");
    System.out.println("Antall elementer i hashmap: " + hashMap.size());

  }

  public List<Integer> generateRandomNumbers(int size, int max_size) {
    List<Integer> randomNumbers = new ArrayList<>();
    Random random = new Random();
    for (int i = 0; i < size; i++) {
      randomNumbers.add(random.nextInt(max_size));
    }
    return randomNumbers;
  }
}

class HashtabellWithStrings {
  int pow = 7;
  int size = (int) Math.pow(2, pow);
  int antElements = 0;
  int collisions = 0;
  List<LenketListe> hashTabell = new ArrayList<>();

  HashtabellWithStrings() {
    for (int i = 0; i < size; i++) {
      hashTabell.add(new LenketListe());
    }
  }

  public double getLoadFactor() {
    return (double) this.antElements / this.size;
  }

  public List<LenketListe> getHashTabell() {
    return this.hashTabell;
  }

  public String getValue(String value) {
    int hashValue = multHash(weighString(value), pow);
    LenketListe listAtIndex = hashTabell.get(hashValue);
    Node node = listAtIndex.getHead();

    while (node != null) {
      if (node.getElement().equals(value)) {
        return "Found: " + value;
      }
      node = node.getNext();
    }
    return "Not found: " + value;
  }

  public void addValue(String string) {
    int hashValue = multHash(weighString(string), pow);
    LenketListe listAtIndex = hashTabell.get(hashValue);

    if (listAtIndex.getHead() != null) {
      System.out.println("Collision between: " + string + " and " + hashTabell.get(hashValue).getHead().getElement());
      listAtIndex.insertAtFront(string);
      collisions++;
    } else {
      listAtIndex.insertAtFront(string);
    }
    antElements++;
  }

  /**
   * Hashfunksjon basert på heltallsmultiplikasjon.
   * <p>
   * Denne funksjonen:
   * <ol>
   * <li>Multipliserer k med en konstant A</li>
   * <li>Reduserer mengden bits i produktet ved hjelp
   * av høyreskift</li>
   * <li>Benytte bitvis AND operasjon ved hjelp av 0x7f for å bare sikre at tallet
   * havner i området 0 og 127</li>
   * </ol>
   * </p>
   */
  public static int multHash(int k, int x) {
    final int A = 1327217885;
    int mask = (1 << x) - 1;
    return ((k * A >>> (32 - x)) & mask);
  }

  public int weighString(String string) {
    int sum = 0;

    for (int i = 0; i < string.length(); i++) {
      int value = Character.getNumericValue(string.charAt(i));
      sum += value * (i + 1);
    }
    return sum;
  }
}

class HashtabellWithInt {
  int size = 12_000_017;
  // int size = 12_999_997;
  int antElements = 0;
  int collisions = 0;
  List<Integer> hashTabell = new ArrayList<>();

  public HashtabellWithInt() {
    for (int i = 0; i < size; i++) {
      hashTabell.add(null);
    }
  }

  public double getLoadFactor() {
    return (double) this.antElements / this.size;
  }

  public int getValue(int value) {
    int hashValue = restHash1(value, size);
    Integer element = hashTabell.get(hashValue);
    if (element == null) {
      return -1;
    } else {
      if (element == value) {
        return value;
      } else {
        do {
          hashValue += restHash2(hashValue, size);
          element = hashTabell.get(hashValue);
        } while (element != null && element != value);
        if (element == value) {
          return value;
        } else {
          return -1;
        }
      }
    }
  }

  public void addValue(int value) {
    int hashValue = restHash1(value, size);
    Integer element = hashTabell.get(hashValue);
    if (element == null) {
      hashTabell.set(hashValue, value);
      antElements++;
      return;
    } else if (element != value) {
      int kollisjonsflytt = restHash2(value, size);
      do {
        hashValue = (hashValue + kollisjonsflytt) % size;
        element = hashTabell.get(hashValue);
        collisions++;
      } while (element != null && element != value);
      if (element == null) {
        hashTabell.set(hashValue, value);
        antElements++;
      }
    }
  }

  public int restHash1(int k, int m) {
    return k % m;
  }

  public int restHash2(int k, int m) {
    return (k % (m - 1)) + 1;
  }
}

/**
 * Node klasse brukt i lenket lister
 */
class Node {
  String element;
  Node next;

  public Node(String element, Node nesteNode) {
    this.element = element;
    this.next = nesteNode;
  }

  public String getElement() {
    return this.element;
  }

  public Node getNext() {
    return this.next;
  }
}

/**
 * En enkeltlenket liste klasse med streng verdier
 */
class LenketListe {
  private Node head = null;
  private int antElements = 0;

  public Node getHead() {
    return this.head;
  }

  public int getAntElements() {
    return this.antElements;
  }

  public void insertAtFront(String value) {
    this.head = new Node(value, this.head);
    this.antElements++;
  }

  public void insertAtBack(String value) {
    if (this.head != null) {
      Node node = this.head;
      while (node.next != null)
        node = node.next;
      node.next = new Node(value, null);
    } else {
      this.head = new Node(value, null);
    }
    this.antElements++;
  }

  public Node remove(Node n) {
    Node previous = null;
    Node current = this.head;
    while (current != null && current != n) {
      previous = current;
      current = current.next;
    }

    if (current != null) {
      if (previous != null) {
        previous.next = current.next;
      } else {
        this.head = current.next;
      }
      current.next = null;
      this.antElements--;
      return current;
    } else
      return null;
  }

  public Node findNr(int nr) {
    Node current = this.head;

    if (nr < this.antElements) {
      for (int i = 0; i < nr; i++) {
        current = current.next;
      }
      return current;
    } else
      return null;
  }

  public void deleteAll() {
    this.head = null;
    this.antElements = 0;
  }
}
