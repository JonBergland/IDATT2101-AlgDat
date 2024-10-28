import java.util.*;

public class oving3 {
  final static Random random = new Random();

  public static void main(String[] args) {
    long randomTime, duplicateTime, alreadySortedTime, reverseTime;

    // Quicksort med en delingsverdi
    System.out.println("Quicksort with dual pivot");
    lineSeperator();

    // Sortering av tabell med tilfeldige tall
    System.out.println("List with random numbers");
    int[] randomList = randomNumberGenerator(50_000_000, 0, 100_000);
    randomTime = sortTest(randomList);
    lineSeperator();

    // Sortering av tabell med bare to forskjellige tall
    System.out.println("List with only two unique numbers");
    int[] duplicateList = duplicateListGenerator(50_000_000);
    duplicateTime = sortTest(duplicateList);
    lineSeperator();

    // Sortering av tabell som allerede er sortert
    System.out.println("List that already is sorted");
    int[] sortedList = randomNumberGenerator(50_000_000, 0 ,100_000);
    quickSort(sortedList, 0, sortedList.length - 1);

    alreadySortedTime = sortTest(sortedList);
    lineSeperator();

    // Sortering av tabell som er baklengs sortert
    System.out.println("List that already is reverse sorted");
    int[] reverseSorted = randomNumberGenerator(50_000_000, 0 ,100_000);
    quickSort(sortedList, 0, sortedList.length - 1);
    sortedList = reverseList(reverseSorted);

    reverseTime = sortTest(sortedList);
    lineSeperator();

    // Time table
    System.out.println(
        "List          |  Sorting time\n" +
        "------------------------\n" +
        "Tilfeldige tall | " + randomTime + "ms\n" +
        "Duplikater      | " + duplicateTime + "ms\n" +
        "Sortert         | " + alreadySortedTime + "ms\n"+
        "Revers sortert  | " + reverseTime + "ms\n"
    );


  }

  private static long sortTest(int[] list) {
    int beforeSum;
    long beforeTime;
    long afterTime;
    long time;
    int aftersum;
    beforeSum = Arrays.stream(list).sum();

    beforeTime = System.currentTimeMillis();
    quickSortDualPivots(list, 0, list.length - 1);
    afterTime = System.currentTimeMillis();
    time = afterTime - beforeTime;
    aftersum = Arrays.stream(list).sum();

    sumtest(beforeSum, aftersum);

    System.out.println("The list is ordered: " + orderTest(list));
    return time;
  }

  public static void quickSort(int[] t, int v, int h) {
    if (h - v > 2) {
      int delepos = splitt(t, v, h);
      quickSort(t,v, delepos - 1);
      quickSort(t, delepos  + 1, h);
    } else median3sort(t, v, h);
  }

  public static int splitt(int[] t, int v, int h) {
    int iv, ih;
    int m = median3sort(t, v, h);
    int dv = t[m];
    bytt(t, m, h - 1);
    for (iv = v, ih = h - 1;;) {
      while (t[++iv] < dv);
      while (t[--ih] > dv);
      if (iv >= ih) break;
      bytt(t, iv, ih);
    }
    bytt(t, iv, h-1);
    return iv;
  }

  public static void quickSortDualPivots(int[] t, int v, int h) {
    if (v < h) {
      int[] delepos = dualSplitt(t, v, h);
      quickSortDualPivots(t,v, delepos[0]);
      if (delepos[0] != delepos[1]) {
        quickSortDualPivots(t,delepos[0] + 1, delepos[1] - 1);
      }
      quickSortDualPivots(t,delepos[1], h);
    }
  }

  public static int[] dualSplitt(int[] t, int v, int h) {
    bytt(t, v, v + (h-v)/3);
    bytt(t, h, h - (h-v)/3);

    if (t[v] > t[h]) bytt(t, v, h);

    int j = v + 1;
    int g = h - 1, k = v + 1, p = t[v], q = t[h];

    while(k <= g) {
      if (t[k] < p) {
        bytt(t, k, j);
        j++;
      } else if (t[k] >= q) {
        while (t[g] > q && k < g) g--;

        bytt(t, k, g);
        g--;

        if (t[k] < p) {
          bytt(t, k, j);
          j++;
        }
      }
      k++;
    }
    j--;
    g++;
    bytt(t, v, j);
    bytt(t, h, g);

    return new int[] {j, g};
  }

  public static int median3sort(int[] t, int v, int h) {
    int m = (v + h)/2;
    if (t[m] < t[v]) bytt(t, v, m);
    if (t[m] > t[h]) {
      bytt(t, m, h);
      if (t[m] < t[v]) bytt(t, v, m);
    }
    return m;
  }

  public static void bytt(int[] t, int a, int b) {
    int placeholder = t[a];
    t[a] = t[b];
    t[b] = placeholder;
  }

  public static boolean orderTest(int[] t) {
    for (int i = 0; i < t.length - 2; i++) {
      if (t[i] > t[i + 1]) {
        return false;
      }
    }
    return true;
  }

  public static int[] randomNumberGenerator(int numberOfNumbers, int minBound, int maxBound) {
    int[] t = new int[numberOfNumbers];
    for (int i = 0; i < numberOfNumbers; i++){
      t[i] = random.nextInt(minBound, maxBound);
    }
    return t;
  }

  public static int[] duplicateListGenerator(int numberOfNumbers) {
    int a = random.nextInt(0, 4);
    int b = random.nextInt(5, 10);
    int[] t = new int[numberOfNumbers];
    for (int i = 0; i < numberOfNumbers; i++) {
      if (i % 2 == 0) {
        t[i] = a;
      } else {
        t[i] = b;
      }
    }
    return t;
  }

  public static int[] reverseList(int[] t) {
    for (int i = 0; i < t.length/2 ; i++) {
      bytt(t, i, t.length - 1 - i);
    }

    return t;
  }

  public static void sumtest(int sum1, int sum2) {
    if (sum1 == sum2) {
      System.out.println("The sum is the same");
    } else {
      System.out.println("The sum is different: " + sum1 + " (before) , " + sum2 + " (after)");
    }
  }

  public static void lineSeperator() {
    System.out.println("-----------------------");
  }
}
