import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class oving5 {
    public static void main(String[] args) {
        Run runInstance = new Run();
        runInstance.run();
      }
}

class Run {
    public void run() {
        String file = "ø5g2.txt"; // Skriv inn filnavnet her
        Graf graf = readFile(file);

        DFS dfs = new DFS(graf);
        

        dfs.sok(graf.getStack());

        Graf omvendtGraf = graf.snuGraf();

        DFS omvendtDfs = new DFS(omvendtGraf);

        omvendtDfs.sok(dfs.finishedStack);

        List<List<Integer>> sskList = new ArrayList<>();
        List<Integer> ssk = new ArrayList<>();
        for (Node node : omvendtDfs.besokteNoder) {
            if (node.tid_funnet > 0) {
                ssk.add(node.nr);
            } else {
                if (ssk.isEmpty()) {
                    ssk.add(node.nr);
                } else {
                    Collections.sort(ssk);
                    sskList.add(ssk);
                    ssk = new ArrayList<>();
                    ssk.add(node.nr);
                }
            }
        }
        Collections.sort(ssk);
        sskList.add(ssk);

        StringBuilder sb = new StringBuilder();
        sb.append(file + " har " + sskList.size() 
        + " sterkt sammenhengende komponenter\n");
        sb.append("Komponent\tNoder i komponenten\n");
        int komponentNr = 1;
        for (List<Integer> list : sskList) {
            sb.append(komponentNr).append("\t\t");

            for (Integer nodeNr : list) {
                sb.append(nodeNr). append("  ");
            }
            sb.append("\n");
            komponentNr++;
        }

        System.out.println(sb.toString());
    }

    public Graf readFile(String filename) {
        Graf graf = new Graf();
        int antallNoder = 0;
        int antallKanter = 0;
        try (Scanner scanner = new Scanner(new File(filename))) {
            Boolean firstLine = true;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] noder = line.split("\\s+");

                Node node1 = new Node(Integer.parseInt(noder[0]));
                Node node2 = new Node(Integer.parseInt(noder[1]));
                if (firstLine) {
                    antallKanter = Integer.parseInt(noder[1]);
                    antallNoder = Integer.parseInt(noder[0]);
                    firstLine = false;

                    for (int i = 0; i < antallNoder; i++) {
                        graf.addNode(new Node(i));
                    }
                } else {
                    graf.addKant(new Kant(node1, node2));
                }

            }

            if (antallNoder != graf.noder.size() || antallKanter != graf.kanter.size()) {
                throw new IOException("Antall kanter eller noder stemmer ikke");
            }
            
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
        return graf;
    }
}

class Node {
    public int nr;
    public int tid_funnet;

    public Node(int nr) {
        this.nr = nr;
        this.tid_funnet = -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return nr == node.nr;
    }
}

class Kant {
    public Node fra;
    public Node til;

    public Kant(Node fra, Node til) {
        this.fra = fra;
        this.til = til;
    }
}

class Graf {
    public Set<Node> noder;
    public Set<Kant> kanter;

    public Graf() {
        this.noder = new HashSet<>();
        this.kanter = new HashSet<>();
    }

    public Graf(Set<Node> noder, Set<Kant> kanter) {
        this.noder = noder;
        this.kanter = kanter;
    }

    public Stack<Node> getStack() {
        Stack<Node> nodeStack = new Stack<>();
        for (Node node : noder) {
            nodeStack.add(node);
        }
        return nodeStack;
    }

    public void addKant(Kant kant) {
        kanter.add(kant);

        if (!nodeEksisterer(kant.fra)) {
            noder.add(kant.fra);
        }
        if (!nodeEksisterer(kant.til)) {
            noder.add(kant.til);
        }
    }

    public boolean nodeEksisterer(Node node) {
        for (Node n : noder) {
            if (n.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public void addNode(Node node) {
        noder.add(node);
    }

    public Graf snuGraf() {
        Set<Kant> omvendtKanter = new HashSet<>();

        for (Kant kant : kanter) {
            omvendtKanter.add(new Kant(kant.til, kant.fra));
        }
        return new Graf(noder, omvendtKanter);
    }

    public void print() {
        System.out.println("Noder");
        for (Node node : noder) {
            System.out.print(node.nr + " ");
        }
        System.out.println("\nKanter");

        for (Kant kant : kanter) {
            System.out.println("Fra: " + kant.fra.nr 
            + ", Til: " + kant.til.nr);
        }
    }
}

class DFS {
    ArrayList<Node> besokteNoder;
    Graf graf;
    Stack<Node> finishedStack = new Stack<>();

    public DFS(Graf graf) {
        this.besokteNoder = new ArrayList<>();
        this.graf = graf;
    }

    public void sok(Stack<Node> nodeStack) {
        while (!nodeStack.empty()) {
            utforskNode(nodeStack.pop(), 0);
        }
    }

    public void utforskNode(Node node, int tid) {
        if (!graf.nodeEksisterer(node)) {
            throw new IllegalArgumentException("Node finnes ikke i grafen");
        }
        if (besokteNoder.contains(node)) {
            return;
        }
        besokteNoder.add(node);
        node.tid_funnet = tid;

        for (Kant kant : graf.kanter) {
            if (kant.fra.equals(node) && !besokteNoder.contains(kant.til)) {
                int nyTid = tid + 1;
                utforskNode(kant.til, nyTid);
            }
        }
        this.finishedStack.add(node);
    }
}
