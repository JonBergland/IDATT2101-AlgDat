package oving7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
        Node node1 = new Node(1, 0, 1);
        Node node2 = new Node(2, 1, 1);
        Node node3 = new Node(3, 2, 1);
        Node node4 = new Node(4, 1, 2);
        Node node5 = new Node(5, 2, 2);
        Node node6 = new Node(6, 1, 0);
        Node node7 = new Node(7, 2, 0);

        graf.addKant(new Kant(node7, node2, 6));
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
        //Graf nyGraf = djikstra.kjorDjikstra(graf, graf.getStack().pop());
        Graf nyGraf = djikstra.kortesteVei(graf, node6, node5);
        nyGraf.print();

        
    }
}

class A_star {
    public Graf kjorAStar(Graf graf, Node startNode, Node maalNode) {
        Stack<Node> grStack = graf.getStack();
		if (!grStack.contains(startNode) || !grStack.contains(maalNode)) {
			throw new IllegalArgumentException("Noden er ikke i Grafen");
		}

		// Setter avstand til den første noden til 0
		startNode.setAvstand(0);

		PriorityQueue<Node> nQueue = new PriorityQueue<>();

		// Bruker kantene ut fra første node til å sette avstanden og estimatet i nabonodene
		for (Kant k : startNode.kantListe) {
			Node til = k.til;
			til.avstand = k.vekt;
            til.estimat = beregnEstimat(til, maalNode); 
            til.setKortesteKant(k);
			nQueue.add(til);
		}

        while (nQueue.size() > 0) {
			Node neste = nQueue.poll();
			if (neste.equals(maalNode)) {
				return graf;
			}
			for (Kant k : neste.kantListe) {
                Node til = k.til;
                int nyAvstand = k.vekt + neste.avstand;
                if (til.avstand == Integer.MAX_VALUE) {
                    til.avstand = nyAvstand;
                    til.estimat = beregnEstimat(til, maalNode);
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

    public double beregnEstimat(Node a, Node b) {
        double estimat = Math.sqrt(Math.pow((a.breddegrad - b.breddegrad), 2) + Math.pow((a.lengdegrad - b.lengdegrad), 2));
        return estimat;
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
            til.setKortesteKant(k);
			nQueue.add(til);
		}

		// Gjentar dette så lenge det finnes nye nabonoder å gå til
		// Vil stoppe når grafen ikke kan gå lengre. Kan føre til ufulstendig kartlegging
		while (nQueue.size() > 0) {
			Node neste = nQueue.poll();
			sjekkKanter(neste, nQueue);
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

        startNode = omvGraf.noder.get(startNode.nr);
        maalNode = omvGraf.noder.get(maalNode.nr);

		maalNode.setAvstand(0);

		PriorityQueue<Node> nQueue = new PriorityQueue<>();

		// Bruker kantene ut fra mål noden til å sette avstanden i nabonodene
		for (Kant k : maalNode.kantListe) {
			Node til = k.til;
			til.avstand = k.vekt;
            til.setKortesteKant(k);
			nQueue.add(til);
		}

		// Gjentar dette så lenge det finnes nye nabonoder å gå til eller
		// Mål noden blir tatt ut av min heapen
		while (nQueue.size() > 0) {
			Node neste = nQueue.poll();
			if (neste.equals(startNode)) {
				return omvGraf;
			}
			sjekkKanter(neste, nQueue);
		}
		return omvGraf;
	}

    public static void sjekkKanter(Node node, PriorityQueue<Node> nQueue) {
		for (Kant k : node.kantListe) {
			Node til = k.til;
			int nyAvstand = k.vekt + node.avstand;
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
}

class Node implements Comparable<Node> {
    public int nr;
	public int avstand;
	public double breddegrad;
	public double lengdegrad;
	public List<Kant> kantListe = new ArrayList<>();
	public Kant kortesteKant;
    public double estimat;

	public Node(int nr, double breddegrad, double lengdegrad) {
		this.nr = nr;
		this.breddegrad = breddegrad;
		this.lengdegrad = lengdegrad;
		this.avstand = Integer.MAX_VALUE;
		this.kortesteKant = null;
        this.estimat = 0;
	}

    public void setAvstand(int nyAvstand) {
        this.avstand = nyAvstand;
    }
    
    public void setEstimat(double estimat) {
        this.estimat = estimat;
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
        return Double.compare(this.avstand + this.estimat, o.avstand + o.estimat);
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
    public Map<Integer, Node> noder;
	public Set<Kant> kanter;
	public int målnode;
	public int startnode;

	public Graf() {
		this.noder = new HashMap<>();
		this.kanter = new HashSet<>();
	}

	public Graf(Map<Integer, Node> noder, Set<Kant> kanter) {
		this.noder = noder;
		this.kanter = kanter;
	}

    public Stack<Node> getStack() {
		Stack<Node> nodeStack = new Stack<>();
		for (Node node : noder.values()) {
			nodeStack.add(node);
		}
		return nodeStack;
	}

	public void addKant(Kant kant) {
		kanter.add(kant);

		if (!nodeEksisterer(kant.fra)) {
			noder.put(kant.fra.nr, kant.fra);
		}
		if (!nodeEksisterer(kant.til)) {
			noder.put(kant.til.nr, kant.til);
		}
	}

    public boolean nodeEksisterer(Node node) {
		for (Node n : noder.values()) {
			if (n.equals(node)) {
				return true;
			}
		}
		return false;
	}

    public void addNode(Node node) {
		noder.put(node.nr, node);
	}

    public Graf snuGraf() {
        Set<Kant> omvendtKanter = new HashSet<>();
        HashMap<Integer, Node> nodeKopi = new HashMap<>();
        for (Node node : noder.values()) {
            nodeKopi.put(node.nr, new Node(node.nr, node.breddegrad, node.lengdegrad));
        }
        
        for (Kant kant : kanter) {
            omvendtKanter.add(new Kant(nodeKopi.get(kant.til.nr), nodeKopi.get(kant.fra.nr), kant.vekt));
        }
        return new Graf(nodeKopi, omvendtKanter);
    }

    public void resetNoder() {
        for (Node n : noder.values()) {
            n.avstand = Integer.MAX_VALUE;
            n.kortesteKant = null;
        }
    }

    public void print() {
        System.out.println("Noder");
        for (Node node : noder.values()) {
            System.out.println(node.nr + " " + node.avstand);
        }
        System.out.println("\nKanter");

        for (Kant kant : kanter) {
            System.out.println("Fra: " + kant.fra.nr
                    + ", Til: " + kant.til.nr);
        }
    }
}
