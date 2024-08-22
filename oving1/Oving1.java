import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Oving1 {
  final static int STARTING_SALDO = 0;
  final static Random random = new Random();
  public static void main(String[] args) {

    //Oppgave 1-1
    ArrayList<Integer> numbers = new ArrayList<>();
    Collections.addAll(numbers, 0, -1, 3, -9, 2, 2, -1, 2, -1, -5);
    ArrayList<Integer> bestResult = calculateBestCombo(numbers);

    System.out.println("Kjøpsdato: " + bestResult.get(0));
    System.out.println("Salgsdato: " + bestResult.get(1));
    System.out.println("Aksje kurs: " + bestResult.get(2));


    //Oppgave 1-3
    ArrayList<Integer> tenThousand = randomNumberGenerator(10000);
    ArrayList<Integer> twentyThousand = randomNumberGenerator(20000);
    ArrayList<Integer> thirtyThousand = randomNumberGenerator(40000);
    ArrayList<Integer> fourtyThousand = randomNumberGenerator(80000);
    ArrayList<Integer> fiftyThousand = randomNumberGenerator(160000);

    Long timeStart = System.currentTimeMillis();
    ArrayList<Integer> bestResult1 = calculateBestCombo(tenThousand);
    Long timeEnd = System.currentTimeMillis();

    System.out.println("Antall tall | Tid");
    System.out.println(tenThousand.size() + " | " + (timeEnd - timeStart) + "ms");

    timeStart = System.currentTimeMillis();
    bestResult = calculateBestCombo(twentyThousand);
    timeEnd = System.currentTimeMillis();

    System.out.println(twentyThousand.size() + " | " + (timeEnd - timeStart) + "ms");

    timeStart = System.currentTimeMillis();
    bestResult = calculateBestCombo(thirtyThousand);
    timeEnd = System.currentTimeMillis();

    System.out.println(thirtyThousand.size() + " | " + (timeEnd - timeStart) + "ms");

    timeStart = System.currentTimeMillis();
    bestResult = calculateBestCombo(fourtyThousand);
    timeEnd = System.currentTimeMillis();

    System.out.println(fourtyThousand.size() + " | " + (timeEnd - timeStart) + "ms");

    timeStart = System.currentTimeMillis();
    bestResult = calculateBestCombo(fiftyThousand);
    timeEnd = System.currentTimeMillis();

    System.out.println(fiftyThousand.size() + " | " + (timeEnd - timeStart) + "ms");

  }

  private static ArrayList<Integer> randomNumberGenerator(int numberOfNumbers) {
    ArrayList<Integer> numbers = new ArrayList<>();
    for(int i = 0; i < numberOfNumbers; i++){
      numbers.add(random.nextInt(-10, 10));
    }
    return numbers;
  }

  private static ArrayList<Integer> calculateBestCombo(ArrayList<Integer> numbers) {
    ArrayList<Integer> results = new ArrayList<Integer>();

    ArrayList<Integer> bestResult =
        new ArrayList<>(Collections.nCopies(3, 0)); // List containing buy date, sell date and profit

    for (int buyDay = 0; buyDay < numbers.size() - 1; buyDay++) {

      int saldo = STARTING_SALDO;
      for (int sellDay = buyDay + 1; sellDay < numbers.size(); sellDay++) {
        saldo += numbers.get(sellDay);
        results.add(saldo);
      }
      int maxResult = Collections.max(results);

      if (bestResult.get(2) < maxResult) { // If new result is better than last best result
        bestResult.set(0, buyDay); // Sets the buy day for the best profit combination
        bestResult.set(1, buyDay + results.indexOf(maxResult) + 1); // Sets us the sell day for the best profit combination
        bestResult.set(2, maxResult); // Sets the profit
      }
      results.clear();
    }
    return bestResult;
  }

}
