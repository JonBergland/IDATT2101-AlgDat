package oving7;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

public class oving7 {
    public static void main(String[] args) {
        Run runInstance = new Run();
        runInstance.run();
    }
}

class Run {
    public void run() {
        Graf graf = new Graf();
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node5 = new Node(5);
        Node node6 = new Node(6);
        Node node7 = new Node(7);

        graf.addKant(new Kant(node7, node3, 6));
        graf.addKant(new Kant(node1, node2, 4));
        graf.addKant(new Kant(node1, node4, 2));
        graf.addKant(new Kant(node2, node3, 2));
        graf.addKant(new Kant(node3, node5, 10));
        graf.addKant(new Kant(node3, node7, 8));
        graf.addKant(new Kant(node4, node2, 1));
        graf.addKant(new Kant(node4, node5, 8));
        graf.addKant(new Kant(node5, node2, 6));
        graf.addKant(new Kant(node6, node1, 5));
        graf.addKant(new Kant(node6, node2, 10));
        graf.addKant(new Kant(node6, node3, 9));
        graf.addKant(new Kant(node6, node7, 15));

        Djikstra djikstra = new Djikstra();
        Graf nyGraf = djikstra.kjorDjikstra(graf, graf.getStack().pop());

        nyGraf.print();
    }
}

class Djikstra {
    public Graf kjorDjikstra(Graf graf, Node fNode) {
        Stack<Node> grStack = graf.getStack();
        if (!grStack.contains(fNode)) {
            throw new IllegalArgumentException("Noden er ikke i Grafen");
        }

        // Setter avstand til den første noden til 0
        fNode.setAvstand(0);

        PriorityQueue<Node> nQueue = new PriorityQueue<>();

        // Bruker kantene ut fra første node til å sette avstanden i nabonodene
        for (Kant k : fNode.kantListe) {
            Node til = k.til;
            til.avstand = k.vekt;
            nQueue.add(til);
        }

        // Gjentar dette så lenge det finnes nye nabonoder å gå til
        // Vil stoppe når grafen ikke kan gå lengre. Kan føre til ufulstendig kartlegging
        while (nQueue.size() > 0) {
            Node neste = nQueue.poll();
            for (Kant k : neste.kantListe) {
                Node til = k.til;
                int nyAvstand = k.vekt + neste.avstand;
                if (til.avstand == Integer.MAX_VALUE) {
                    til.avstand = nyAvstand;
                    til.setKortesteKant(k);
                    nQueue.add(til);
                } else if (nyAvstand < til.avstand) {
                    til.avstand = nyAvstand;
                    til.setKortesteKant(k);
                }
            }
        }
        return graf;
    }

    public Graf kortesteVei(Graf graf, Node startNode, Node maalNode) {
        // Snur grafen for å ta utgangspunkt i målnoden
        Graf omvGraf = graf.snuGraf();
        Stack<Node> grStack = omvGraf.getStack();
        if (!grStack.contains(startNode) || !grStack.contains(maalNode)) {
            throw new IllegalArgumentException("Noden er ikke i Grafen");
        }

        maalNode.setAvstand(0);

        PriorityQueue<Node> nQueue = new PriorityQueue<>();

        // Bruker kantene ut fra mål noden til å sette avstanden i nabonodene
        for (Kant k : maalNode.kantListe) {
            Node til = k.til;
            til.avstand = k.vekt;
            nQueue.add(til);
        }

        // Gjentar dette så lenge det finnes nye nabonoder å gå til eller 
        // Nabo noden blir tatt ut av 
        while (nQueue.size() > 0) {
            Node neste = nQueue.poll();
            if (neste.equals(startNode)) {
                return omvGraf;
            }
            for (Kant k : neste.kantListe) {
                Node til = k.til;
                int nyAvstand = k.vekt + neste.avstand;
                if (til.avstand == Integer.MAX_VALUE) {
                    til.avstand = nyAvstand;
                    nQueue.add(til);
                } else if (nyAvstand < til.avstand) {
                    til.avstand = nyAvstand;
                }
            }
        }

        return omvGraf;
    }
}

class Node implements Comparable<Node> {
    public int nr;
    public int avstand;
    public List<Kant> kantListe = new ArrayList<>();
    public Kant kortesteKant;

    public Node(int nr) {
        this.nr = nr;
        this.avstand = Integer.MAX_VALUE;
        this.kortesteKant = null;
    }

    public void setAvstand(int nyAvstand) {
        this.avstand = nyAvstand;
    }

    public void setKortesteKant(Kant kant) {
        if (kant == null || kant.getClass() != Kant.class) {
            throw new IllegalArgumentException("Kanten er ikke riktig objekt eller null");
        }
        this.kortesteKant = kant;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Node node = (Node) obj;
        return nr == node.nr;
    }

    @Override
    public int compareTo(Node o) {
        return Integer.compare(this.avstand, o.avstand);
    }
}

class Kant {
    public Node fra;
    public Node til;
    public int vekt;

    public Kant(Node fra, Node til, int vekt) {
        this.fra = fra;
        this.til = til;
        this.vekt = vekt;
        this.fra.kantListe.add(this);
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
            omvendtKanter.add(new Kant(kant.til, kant.fra, kant.vekt));
        }
        return new Graf(noder, omvendtKanter);
    }

    public void resetNoder() {
        for (Node n : noder) {
            n.avstand = Integer.MAX_VALUE;
        }
    }

    public void print() {
        System.out.println("Noder");
        for (Node node : noder) {
            System.out.println(node.nr + " " + node.avstand);
        }
        System.out.println("\nKanter");

        for (Kant kant : kanter) {
            System.out.println("Fra: " + kant.fra.nr
                    + ", Til: " + kant.til.nr);
        }
    }
}
