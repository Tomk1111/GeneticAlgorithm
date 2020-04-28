/*
 * Created by
 * Bartlomiej Mlynarkiewicz - 17241782
 * Paul Kinsella - 17244412
 * Norbert Cholewka - 16110366
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class is17241782 {

    public static void main(String[] args) {

        /*

        ///Users/bartek/IdeaProjects/Genetics/src/edges.txt
        try{
            Gens gens = new Gens();
            gens.readFile(args[0]);
        }catch (IndexOutOfBoundsException e){
            System.out.println("File not provided !!!\n Exiting");
            System.exit(1);
        }

        */


        UserInput up = new UserInput();
        up.showOptionPane();

        final int pop = up.getPopulation();
        final int gen = up.getGenerations();
        final int crs = up.getCrossOver();
        final int mut = up.getMutation();


            Gens gens = new Gens(true,  200);
            System.out.println("First Drawing Algorithm");

            /*
                Change file path
            */
            gens.readFile("edges.txt");
            gens.setMode(1);
            gens.setData(pop, gen, crs, mut);
            gens.init();



            Gens gensTwo = new Gens(true, 200);
            System.out.println("TimGA Drawing Algorithm");
            /*
                Change file path
            */
            gensTwo.readFile("edges.txt");
            gensTwo.setMode(2);
            gensTwo.setData(pop, gen, crs, mut);
            gensTwo.init();

    }
}

class Gens{

    private boolean showBest;
    private int stepSpeed;
    private int mode;
    private int tPop;
    private int tGen;
    private int tCross;
    private int tMut;

    public void setData(int tPop, int tGen, int tCross, int tMut) {
        this.tPop = tPop;
        this.tGen = tGen;
        this.tCross = tCross;
        this.tMut = tMut;
    }

    public Gens() {
        stepSpeed = 250;
    }

    public Gens(boolean showBest) {
        this(showBest, 1);
    }

    public Gens(boolean showBest, int stepSpeed) {
        this.showBest = showBest;
        this.stepSpeed = stepSpeed;
    }


    public void setMode(int mode) {
        this.mode = mode;
    }
    private Node[][] currentPopulation;
    private JFrame j;
    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Node> tmpNodes = new ArrayList<>();
    private int[][] adj;


    private int getMax(ArrayList<Edge> edges){
        int maxNodeOne = 0, maxNodeTwo = 0;

        try {
            maxNodeOne = edges.stream().mapToInt(x -> x.getNodeOne().getNodeNumber())
                    .max().orElseThrow(NoSuchElementException::new);

            maxNodeTwo = edges.stream().mapToInt(x -> x.getNodeTwo().getNodeNumber())
                    .max().orElseThrow(NotActiveException::new);
        }catch (NotActiveException e){
            System.out.println(e.getMessage());
        }

        return  maxNodeOne > maxNodeTwo ? maxNodeOne : maxNodeTwo;
    }

    public void init(){
        int popSize = tPop;
        int crossover = tCross;
        int mutation = tMut;
        int generations = tGen;


        int max = (getMax(edges)+1);
        adj = new int[max][max];

        System.out.println("\n********************* Edge List ***************************\n");
        for(int i = 0; i < edges.size(); i++){
            System.out.println("Edge " + (i+1) + " Node One " + (edges.get(i).getNodeOne().getNodeNumber()) + " Node Two " + (edges.get(i).getNodeTwo().getNodeNumber())) ;
            adj[edges.get(i).getNodeOne().getNodeNumber()][edges.get(i).getNodeTwo().getNodeNumber()] = 1;
        }

        if(!checkMatrix(adj)){
            System.out.println("Invalid adj matrix, leading diagonal is not all 0, exiting");
            System.exit(1);
        }

        System.out.println("\n********************* Adjacency Matrix ***************************\n");
        printMatrix(adj);
        System.out.println("\n********************* Adjacency Matrix ***************************\n");


        currentPopulation = new Node[popSize][max];

        for(int i = 0; i < max; i++){
            tmpNodes.add(addNode(i));
        }

        ArrayList<Node[]> orderings = initPops(max, popSize);
        System.out.println("\n********************* Orderings ***************************\n");
        for(int i = 0; i < orderings.size(); i++) {
            Node[] tmp = orderings.get(i);
            System.out.print("Ordering " + (i+1) + "\t\t");
            for(int j = 0; j < tmp.length; j++){
                currentPopulation[i][j] = tmp[j];
                System.out.print(tmp[j].getNodeNumber() + " ");
            }
            System.out.println();
        }
        System.out.println("\n********************* Orderings ***************************\n");

        Node[][] tmpNodes = currentPopulation.clone();


        switch(mode) {
            case 1:
                OrderingClassTwo orderingClass = new OrderingClassTwo(tmpNodes, crossover, mutation, generations);
                orderingClass.calculate(edges);
                break;
            case 2:
                OrderingClassTwo orderingClassTwo = new OrderingClassTwo(currentPopulation, crossover, mutation, generations);
                orderingClassTwo.timgaA(edges);
                break;
        }






    }

    private void printMatrix(int[][] adjMatrix){
        for (int[] adjMatrix1 : adjMatrix) {
            for (int i : adjMatrix1) {
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    private ArrayList<Node []> initPops(int max, int popSize){
        ArrayList<Node []> orderings = new ArrayList<>();
        boolean equals = false;
        while (orderings.size() != popSize) {
            Node[] res = creataOrdering(max);
            for(Node[] nodes : orderings){
                if(!Arrays.equals(res, nodes)){
                    equals = true;
                }
            }

            if (equals)orderings.add(res);

            if(orderings.size() == 0) {
                orderings.add(res);
            }

        }
		System.out.println("Orderings size:"+orderings.size());
        return orderings;
    }

    private Node[] creataOrdering(int n){
        ArrayList<Node> integers = new ArrayList<>();
		int counter=0;
        while (integers.size() != n) {
            int x = counter;
            if(!contains(integers, x)){
                Node node = getNode(x);
				System.out.println("X:"+x);
				System.out.println("Node"+node.toString());
                if(node != null){
                    integers.add(node);
					counter++;
                }
            }
        }

        return toArray(integers);
    }

    private Node[] toArray(ArrayList<Node> node){
        Node[] nodes = new Node[node.size()];
        for(int i = 0; i < node.size(); i++){
            nodes[i] = node.get(i);
        }
        return nodes;
    }

    private Node getNode(int x){
        for(Node node : tmpNodes){
            if(node.getNodeNumber() == x){
                return node;
            }
        }
        return null;
    }

    private boolean contains(ArrayList<Node> nodes, int x){
        for(Node node : nodes){
            if(node.getNodeNumber() == x){
                return true;
            }
        }
        return false;
    }

    public void readFile(String filePath){
        List<Edge> tEdges = new ArrayList<>();
        Set<Node> tNodes = new TreeSet<>();
        File file = new File(filePath);
        if(!file.exists()){
            System.out.println("File doesn't exist");
            System.exit(1);
        }

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            ArrayList<String> arrayList = new ArrayList<>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                arrayList.add(line);
            }

            for(String pts : arrayList){
                String[] split = pts.split(" ");
                int numOne = Integer.parseInt(split[0]);
                int numTwo = Integer.parseInt(split[1]);
                Node oneNode = new Node(numOne);
                Node twoNode = new Node(numTwo);
                tNodes.add(oneNode);
                tNodes.add(twoNode);
                tEdges.add(new Edge(oneNode, twoNode));
                // addEdge(numOne,numTwo);
            }

            nodes.clear();
            edges.clear();
            nodes.addAll(tNodes);
            edges.addAll(tEdges);

            //showOptionPane();

        }catch (IOException e){
            System.out.println("Failed to read file, exiting");
            System.exit(1);
        }


    }

    private Node addNode(int nodeNumber){
        List<Node> nodesCollect  = nodes.stream().filter(x -> x.getNodeNumber() == nodeNumber).collect(Collectors.toList());
        return nodesCollect.size() == 1 ? nodesCollect.get(0) : new Node(nodeNumber);
    }

    private boolean checkMatrix(int[][] adj){
        int count = 0;
        for(int i = 0; i < adj.length; i++){
            if(adj[i][i] != 0)count++;
        }
        return count == 0;
    }


    class Ordering{

        private Node[] ordering;
        private double totalDistance;
        private int index;

        public Ordering(Node[] ordering, double totalDistance, int index){
            this.ordering = ordering;
            this.totalDistance = totalDistance;
            this.index  = index;
        }

        public void setTotalDistance(double totalDistance) {
            this.totalDistance = totalDistance;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public int getIndex() {
            return index;
        }

        public Node[] getOrdering() {
            return ordering;
        }

        public String orderingToString(){
            StringBuilder stringBuilder = new StringBuilder();
            Arrays.stream(this.ordering).forEach(x -> {
                stringBuilder.append(x.getNodeNumber()).append(" ");
            });
            return stringBuilder.toString();
        }
    }

    class OrderingClassTwo {

        private Node[][] orderings;
        private int crossOver;
        private int mutation;
        private int generations;


        public OrderingClassTwo(Node[][] orderings, int crossOver, int mutation, int generations) {
            this.orderings = orderings;
            this.mutation = mutation;
            this.crossOver = crossOver;
            this.generations = generations;
        }

        public int getCrossOver() {
            return crossOver;
        }

        public int getMutation() {
            return mutation;
        }


        private void timgaA(ArrayList<Edge> edges) {
            ArrayList<Ordering> orderings = new ArrayList<>();
            long totalTime = 0, bestTime = 1000000;
            int runs = 0;
            double fitnessScore = 0;
            double best = Integer.MAX_VALUE;
            int bestGen = 0;//what gen was this reach
            int[][] bestAd = null;
            Node[] bestOrder = null;

            GraphVisualisation graph = new GraphVisualisation("Timga-A");
            graph.setWindowLoc(450, 0);
            while (runs != generations) {


                for (int j = 0; j < this.orderings.length; j++) {
                    long startTime = System.nanoTime();
                    double totalDist = 0, minDist = 0, eval = 0;
                    Node[] nodes = this.orderings[j];
                    double chunk = ((2 * Math.PI) / nodes.length);

                    for (int i = 0; i < nodes.length; i++) {
                        nodes[i].setX(Math.cos(i * chunk));
                        nodes[i].setY(Math.sin(i * chunk));
                    }


                    for (Edge edge : edges) {
                        for (Node node : nodes) {
                            if (edge.getNodeOne().equals(node)) {
                                edge.getNodeOne().setY(node.getY());
                                edge.getNodeOne().setX(node.getX());
                            }
                            if(edge.getNodeTwo().equals(node)){
                                edge.getNodeTwo().setY(node.getY());
                                edge.getNodeTwo().setX(node.getX());
                            }
                        }
                        edge.calculateDiastance();
                    }

                    double distance = 1, tmpDist = 0, minDisance = 1, minDistanceEdge = 1;
                    for(int i = 0; i < nodes.length; i++){
                        for(int k = 0; k < nodes.length; k++){
                            if(!nodes[i].equals(nodes[k])){
                                tmpDist = Point2D.distance(nodes[i].getX(),nodes[i].getY(), nodes[k].getX(), nodes[k].getY());
                                distance = Math.min(distance, tmpDist);
                                minDisance = Math.min(minDisance, tmpDist);
                            }
                        }
                        totalDist+= distance;
                        distance = 1;
                    }


                    minDist = (nodes.length * Math.pow(minDisance, 2));

                    double std = 0;

                    for(Edge edge : edges){
                        minDistanceEdge = Math.min(minDistanceEdge, edge.getDistance());
                    }



                    for (Edge edge : edges){
                        std += Math.pow((edge.getDistance() - minDistanceEdge), 2);
                    }

                    std = Math.sqrt((std/(nodes.length-1)));



                    double edgeCrossings = ((nodes.length/2) * ((nodes.length-1)/2) * (edges.size()/2) * ((edges.size()-1)/2));


					System.out.println("minDistSum:"+ totalDist);
					System.out.println("deviation:"+ std);
					System.out.println("minDist:"+ minDist);
					System.out.println("totalEdgeCrossings:"+ edgeCrossings);
                    eval =  Math.abs((2 * totalDist) - (2 * (std)) - (2.5 * (std/minDist)) + (.25 * (nodes.length * (Math.pow(minDist, 2)))) - (((edgeCrossings))));


                    long endTime = System.nanoTime() - startTime;

                    orderings.add(new Ordering(nodes, eval, j));
                    bestTime = Math.min(bestTime, endTime);

                    totalTime += endTime;
                    fitnessScore = eval;
					System.out.println("fitnessScore"+fitnessScore);
					System.out.println();
                }


                orderings.sort(Comparator.comparingDouble(Ordering::getTotalDistance));

                for(int i = 0; i < orderings.size(); i++){
                    this.orderings[i] = orderings.get(i).getOrdering();
                }

                try {
                    Thread.sleep(stepSpeed);
                    // new GraphVisualisation(adj, this.orderings[0], this.orderings[0].length, "First Drawing Algorithm");
                    boolean isFitter = fitnessScore < best;
                    if(isFitter) {
                        bestAd = adj.clone();
                        bestOrder = this.orderings[0].clone();
                        best = fitnessScore;
                        bestGen = runs;
                    }

                    if(showBest && isFitter) {
                        graph.updateFunc(adj, this.orderings[0], this.orderings[0].length);
                    } else {
                        graph.updateFunc(adj, this.orderings[0], this.orderings[0].length);
                    }



                    graph.setText(String.format("Generation: %d/%d Fitness: %.2f (Best: %.2f Gen: %d)", runs, generations, fitnessScore, best, bestGen));
                    graph.repaint();
                }catch (InterruptedException e){
                    System.out.println(e);
                }

                replace(orderings, this.orderings);

                this.orderings = crossOver(this.orderings);

                orderings.clear();


                runs++;
            }

            totalTime = (totalTime/generations);
            System.out.println("Average Time For Fitness Function " + totalTime);
            graph.setText(String.format("Average Time For Fitness Function: %d ms (Best: %.2f Gen: %d)", TimeUnit.MILLISECONDS.toSeconds(totalTime), best,bestGen));

            if(bestAd != null && bestOrder != null) {
                graph.updateFunc(bestAd, bestOrder, bestOrder.length);
            }
            graph.repaint();

        }

        private void calculate(ArrayList<Edge> edges) {
            ArrayList<Ordering> orderings = new ArrayList<>();
            long totalTime = 0;
            long bestTime = 10000000;


            int runs = 0;
            double dist = 0;
            GraphVisualisation graph = new GraphVisualisation("First Drawing Algorithm");
            graph.setWindowLoc(0, 0);
            double fitnessScore = 0;
            double best = Integer.MAX_VALUE;
            int bestGen = 0;//what gen was this reach
            int[][] bestAd = null;
            Node[] bestOrder = null;
            while(runs != generations) {

                for (int j = 0; j < this.orderings.length; j++) {
                    long startTime = System.nanoTime();
                    Node[] nodes = this.orderings[j];
                    double chunk = ((2 * Math.PI) / nodes.length);

                    for (int i = 0; i < nodes.length; i++) {
                        nodes[i].setX(Math.cos(i * chunk));
                        nodes[i].setY(Math.sin(i * chunk));
                    }

                    for (Edge edge : edges) {
                        for (Node node : nodes) {
                            if (edge.getNodeOne().equals(node)) {
                                edge.getNodeOne().setY(node.getY());
                                edge.getNodeOne().setX(node.getX());
                            }
                            if(edge.getNodeTwo().equals(node)){
                                edge.getNodeTwo().setY(node.getY());
                                edge.getNodeTwo().setX(node.getX());
                            }
                        }
                    }

                    for (Edge edge : edges) {
                        edge.calculateDiastance();
                        dist += edge.getDistance();
                    }

                    long endTime = System.nanoTime() - startTime;
                    orderings.add(new Ordering(nodes, dist, j));

                    bestTime = Math.min(bestTime, endTime);

                    totalTime += endTime;
                    fitnessScore = dist;
                    dist = 0;
                }

                orderings.sort(Comparator.comparingDouble(Ordering::getTotalDistance));

                for(int i = 0; i < orderings.size(); i++){
                    this.orderings[i] = orderings.get(i).getOrdering();
                }

                try {
                    Thread.sleep(stepSpeed);
                    // new GraphVisualisation(adj, this.orderings[0], this.orderings[0].length, "First Drawing Algorithm");
                    boolean isFitter = fitnessScore < best;
                    if(isFitter) {
                        bestAd = adj.clone();
                        bestOrder = this.orderings[0].clone();
                        best = fitnessScore;
                        bestGen = runs;
                    }

                    if(showBest && isFitter) {
                        graph.updateFunc(adj, this.orderings[0], this.orderings[0].length);
                    } else {
                        graph.updateFunc(adj, this.orderings[0], this.orderings[0].length);
                    }

                    graph.setText(String.format("Generation: %d/%d Fitness: %.2f (Best: %.2f Gen: %d)", runs, generations, fitnessScore, best, bestGen));
                    graph.repaint();
                }catch (InterruptedException e){
                    System.out.println(e);
                }

                replace(orderings, this.orderings);

                this.orderings = crossOver(this.orderings);

                orderings.clear();


                runs++;
            }

            totalTime = (totalTime/generations);
            System.out.println("Average Time For Fitness Function " + totalTime);
            graph.setText(String.format("Average Time For Fitness Function: %d ms (Best: %.2f Gen: %d)", TimeUnit.MILLISECONDS.toSeconds(totalTime), best,bestGen));

            if(bestAd != null && bestOrder != null) {
                graph.updateFunc(bestAd, bestOrder, bestOrder.length);
            }
            graph.repaint();

        }

        private Node[][] crossOver(Node[][] nodes){
            ArrayList<Node[]> tmp = copy(nodes, nodes.length);
            ArrayList<Node[]> arrays = new ArrayList<>();

            int counter = 0;


            while (counter < orderings.length) {
                int rand = new Random().nextInt(100);


                if(getCrossOver() >= rand && tmp.size() != 1){
                    int[] rans = ThreadLocalRandom.current().ints(0,(tmp.size())).distinct().limit(2).toArray();

                    Node[] nodeOne = tmp.get(rans[0]);
                    Node[] nodeTwo = tmp.get(rans[1]);
                    arrays.addAll(swap(nodeOne, nodeTwo));

                    tmp.remove(nodeOne);
                    tmp.remove(nodeTwo);

                    counter = counter + 2;

                }else if((getCrossOver() <= rand) && rand <= (getCrossOver() + getMutation())){
                    int ran = new Random().nextInt(tmp.size());
                    arrays.add(mutation(tmp.get(ran)));
                    tmp.remove(ran);
                    counter++;
                }else if((getCrossOver() == getMutation()) && (getCrossOver() <= rand)){
                    int ran = new Random().nextInt(tmp.size());
                    arrays.add(tmp.get(ran));
                    tmp.remove(ran);
                    counter++;

                }


            }


            for(int i = 0; i < currentPopulation.length; i++){
                currentPopulation[i] = arrays.get(i);
            }


            return currentPopulation;
        }

        private ArrayList<Node[]> swap(Node[] nodeOne, Node[] nodeTwo) {

            int cutPoint = new Random().nextInt((nodeOne.length - 2) - 1) + 1;

            Node[] tmpOne = Arrays.copyOf(nodeOne, nodeOne.length);
            Node[] tmpTwo = Arrays.copyOf(nodeTwo, nodeTwo.length);
            Node[] nodesToReturn = new Node[nodeOne.length];

            int i = 0;
            while (i < cutPoint){
                nodesToReturn[i] = tmpTwo[i];
                tmpTwo[i] = tmpOne[i];
                tmpOne[i] = nodesToReturn[i];
                i++;
            }

            return new ArrayList<Node[]>(){{
                add(removeDups(tmpOne));
                add(removeDups(tmpTwo));
            }};
        }

        private Node[] removeDups(Node[] node){
            ArrayList<Node> missingNodes = new ArrayList<>();
            ArrayList<Node> tmpNode = copy(node);
            Map<Node,ArrayList<Integer>> mis = new HashMap<>();
            int counter = 0;


            for(int i = 0; i < tmpNode.size(); i++){
                if(!tmpNode.contains(nodes.get(i))){
                    missingNodes.add(nodes.get(i));
                }

                mis.put(tmpNode.get(i), new ArrayList<>());
                for(int j = 0; j < tmpNode.size(); j++){
                    if(tmpNode.get(i).equals(tmpNode.get(j))){
                        mis.get(tmpNode.get(i)).add(j);
                    }
                }
            }

            for(Node nod : mis.keySet()){
                if(mis.get(nod).size() == 2){
                    int index = mis.get(nod).get(0);
                    if(tmpNode.remove(index) != null){
                        tmpNode.add(index, missingNodes.get(counter));
                        counter++;
                    }
                }
            }

            return toArray(tmpNode);
        }

        private ArrayList<Node> copy(Node[] nodes){
            return new ArrayList<>(Arrays.asList(nodes));
        }

        private Node[] mutation(Node [] node){
            int[] rans = ThreadLocalRandom.current().ints(0,(node.length)).distinct().limit(2).toArray();
            Node tmpNode = node[rans[0]];
            node[rans[0]] = node[rans[1]];
            node[rans[1]] = tmpNode;
            return node;
        }

        private ArrayList<Node[]> copy(Node[][] nodes, int length){
            ArrayList<Node[]> nodes1 = new ArrayList<>();
            for(int i = 0; i < length; i++ ){
                nodes1.add(nodes[i]);
            }
            return nodes1;
        }

        private Node[][] replace(ArrayList<Ordering> orderings, Node[][] nodes){

            int[] ints = orderings.stream().mapToInt(Ordering::getIndex).toArray();
            int lenght = ints.length/3;

            int nd = nodes.length - 1;
            int mid = (nd - lenght + 1);
            for(int i = 0; i < lenght; i++){
                nodes[mid+i] = nodes[i];
            }

            return nodes;
        }


    }

}

class HintTextField extends JTextField implements FocusListener {

    private final String hint;
    private boolean showingHint;

    HintTextField(final String hint) {
        super(hint);
        this.hint = hint;
        this.showingHint = true;
        super.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText("");
            showingHint = false;
        }
    }

    public void setShowingHint(boolean showing){
        this.showingHint = showing;
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            super.setText(hint);
            showingHint = true;
        }
    }

    @Override
    public String getText() {
        return showingHint ? "" : super.getText();
    }
}

class UserInput {
    private JFrame j;
    private ArrayList<String> tmp = new ArrayList<>();
    private int population;
    private int generations;
    private int crossover;
    private int mutation;

    public int getCrossOver() { return crossover;}
    public int getGenerations() { return generations;}
    public int getPopulation() { return population;}
    public int getMutation() { return mutation;}

    public void showOptionPane(){

        JPanel jPanel = getJPanel(4,1);
        JPanel jPanel2 = getJPanel(4,1);
        JPanel jPanel3 = getJPanel(1,2);
        String[] textFields = {"Population Size (Positive Value)", "Number Of Generations (Positive Value)", "Cross Over Rate [0-100]", "Mutation Rate [0-100]"};
        HintTextField[] jTextFields = new HintTextField[textFields.length];
        for(int i = 0; i < jTextFields.length; i++){
            jPanel.add(new JLabel(textFields[i]));
            jTextFields[i] = new HintTextField(textFields[i]);
            jTextFields[i].setToolTipText(textFields[i]);
            jPanel2.add(jTextFields[i]);
        }

        if(!tmp.isEmpty())fillTextFields(jTextFields);
        jPanel3.add(jPanel);
        jPanel3.add(jPanel2);
        j = new JFrame();

        int result = JOptionPane.showConfirmDialog(null, jPanel3,
                "Enter values", JOptionPane.DEFAULT_OPTION);


        if(result == JOptionPane.YES_OPTION){
            fillArray(jTextFields);
            if(!empty(jTextFields)) {
                try {
                    if (!validate(jTextFields)) {
                        showOptionPane();
                    }else{
                        population = Integer.parseInt(jTextFields[0].getText());
                        generations = Integer.parseInt(jTextFields[1].getText());
                        crossover = Integer.parseInt(jTextFields[2].getText());
                        mutation = Integer.parseInt(jTextFields[3].getText());

                        // initPopulation(Integer.parseInt(jTextFields[0].getText()), Integer.parseInt(jTextFields[2].getText())
                        //         , Integer.parseInt(jTextFields[3].getText()), Integer.parseInt(jTextFields[1].getText()));
                    }
                }catch (NumberFormatException e){
                    JOptionPane.showMessageDialog(j,"Please enter digits only","Alert",JOptionPane.WARNING_MESSAGE);
                    showOptionPane();
                }
            }else{
                JOptionPane.showMessageDialog(j,"Please enter all fields","Alert",JOptionPane.WARNING_MESSAGE);
                showOptionPane();
            }
        }



    }

    public void fillArray(HintTextField[] jTextFields){
        if(!tmp.isEmpty())tmp.clear();
        for(JTextField jTextField : jTextFields){
            tmp.add(jTextField.getText());
        }
    }

    public void fillTextFields(HintTextField[] jTextFields){
        for(int i = 0; i < jTextFields.length; i++){
            jTextFields[i].setText(tmp.get(i));
            jTextFields[i].setShowingHint(false);
        }
    }

    public boolean validate(HintTextField[] jTextFields)throws NumberFormatException{

        StringBuilder res = new StringBuilder();

        if(Integer.parseInt(jTextFields[0].getText()) < 0){
            res.append("1. Population Size cannot be negative\n");
        }
        if(Integer.parseInt(jTextFields[1].getText()) < 0){
            res.append("2. Number of Generations cannot be negative\n");
        }
        if(Integer.parseInt(jTextFields[2].getText()) < 0 || Integer.parseInt(jTextFields[2].getText()) > 100){
            res.append("3. Cross over rate need to be within the range 0 - 100\n");
        }
        if(Integer.parseInt(jTextFields[3].getText()) < 0 || Integer.parseInt(jTextFields[3].getText()) > 100) {
            res.append("4. Mutation Rate Needs to be within the range 0 - 100");
        }
        if((Integer.parseInt(jTextFields[3].getText()) + Integer.parseInt(jTextFields[2].getText())) > 100){
            res.append("5. The Total Sum Cross Over Rate (CR) and Mutation Rate (Mu) cannot be greater than 100");
        }



        if(!res.toString().isEmpty()){
            JOptionPane.showMessageDialog(j,res.toString(),"Alert",JOptionPane.WARNING_MESSAGE);
            return false;
        }

        /*
        else if(Integer.parseInt(jTextFields[0].getText()) > getMax(edges)){
            JOptionPane.showMessageDialog(j,"Population size cannot be bigger than the biggest node " + max,"Alert",JOptionPane.ERROR_MESSAGE);
            return false;
        }
         */
        return true;
    }

    public boolean empty(JTextField[] jTextFields){
        return jTextFields[0].getText().isEmpty() || jTextFields[1].getText().isEmpty() || jTextFields[2].getText().isEmpty() || jTextFields[3].getText().isEmpty();
    }

    public JPanel getJPanel(int row, int col){
        GridLayout gridLayout = new GridLayout(row,col);
        gridLayout.setVgap(5);
        return new JPanel(gridLayout);
    }

}

class GraphVisualisation extends JFrame{
    private String TITLE;
    private final int HEIGHT = 400;
    private final int WIDTH = 500;
    private int[][] adjacencyMatrix;
    private int numberOfVerticies;
    private Node[] ordering;
    private double chunk;
    private String text;

    public GraphVisualisation(String title){
        this.TITLE = title;
        setTitle(title);
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        text = "";
    }

    public void setWindowLoc(int xOffsett, int yOffset) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x + xOffsett, y + yOffset);
    }
    public void updateFunc(int[][] adjacencyMatrix, Node[] ordering, int numberOfVerticies) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.ordering = ordering;
        this.numberOfVerticies = numberOfVerticies;
        this.chunk = (Math.PI * 2)/((double) numberOfVerticies);

    }
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void paint(Graphics g){
        super.paintComponents(g);
        int radius = 100;
        int mov = 200;
        g.drawString(text, 10, 350);

        for(int i = 0; i < numberOfVerticies; i++){
            int xx = (int) (Math.cos(i*chunk)*radius) + mov;
            int yy = (int) (Math.sin(i*chunk)*radius) + mov;
            g.drawOval(xx,yy, 5, 5);
            g.drawString("" + ordering[i].getNodeNumber(), xx, yy);

            for(int j = 0; j < numberOfVerticies; j++){
                if(adjacencyMatrix[ordering[i].getNodeNumber()][ordering[j].getNodeNumber()] == 1){
                    g.drawLine(
                            (int) (Math.cos(i*chunk)*radius) + mov,
                            (int) (Math.sin(i*chunk)*radius) + mov,
                            (int) (Math.cos(j*chunk)*radius) + mov,
                            (int) (Math.sin(j *chunk)*radius) +mov
                    );
                }

            }

        }
    }
}

class Edge{

    private Node nodeOne;
    private Node nodeTwo;
    private double distance;

    public Edge(Node nodeOne, Node nodeTwo){
        this.nodeOne = nodeOne;
        this.nodeTwo = nodeTwo;
    }

    public Node getNodeOne() {
        return nodeOne;
    }

    public Node getNodeTwo() {
        return nodeTwo;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public void calculateDiastance(){
        setDistance(Point2D.distance(getNodeOne().getX(),getNodeOne().getY(),getNodeTwo().getX(),getNodeTwo().getY()));
    }

    public String toString(){
        return "Node one " + getNodeOne().getNodeNumber() + " x " + getNodeOne().getX() + " y " + getNodeOne().getY() + " Node two " + getNodeTwo().getNodeNumber() + " x " + getNodeTwo().getX() + " y " + getNodeTwo().getY() + " Distance " + getDistance();
    }

}

class Node implements Comparable <Node> {
    private double x;
    private double y;
    private int nodeNumber;

    public Node(int nodeNumber){
        this.nodeNumber = nodeNumber;
    }

    public Node(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public String toString() {
        return String.format("x:%.2f y:%.2f n:%d%n", x, y, nodeNumber);
    }

    public int compareTo(Node that) {
        return this.nodeNumber - that.nodeNumber;
    }

    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj instanceof Node) {
            return this.nodeNumber == ((Node)obj).nodeNumber;
        }

        return false;
    }
}
