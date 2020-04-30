import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/*
 * Thomas Kiely - 17185203
 * Sean Morrissey - 17222761
 * Paul Murphy - 17198046
 * Art Maguire - 16150201
 */

public class is17222761 extends JFrame {

    private static final ArrayList<Vertice> verts = new ArrayList<>();
    private static final ArrayList<OrderingCost> nextGenOrders = new ArrayList<>(); //used for fitnes function 1
    private static final ArrayList<OrderingCost> nextGenOrders2 = new ArrayList<>(); //used for fitnes function 2
    private static int bestGeneration1 = 0;
    private static int bestGeneration2 = 0;
    private static double bestScore1;
    private static double bestScore2;
    private static double total1 = 0;
    private static double total2 = 0;
    private static final double milliseconds = 1000000.0;
    private static int[][] adjacencyMatrix;
    private static double chunk;
    private static int populationSize;
    private static int numberOfGeneration;
    private static int crossoverRate;
    private static int mutationRate;
    private static int n = 0;
    private static int currentGeneration = 0;
    private static GUI gui;
    private static ArrayList<OrderingCost> genOrders = new ArrayList<>(); //used for fitness function 1
    private static ArrayList<OrderingCost> genOrders2 = new ArrayList<>(); //used for fitness function 2

    public static void main(String[] args) {
        String message = "Mutation rate: Please enter a positive integer in the range [0,100]";
        adjacencyMatrix = parseInputFile();
        chunk = (2 * Math.PI) / n;
        printAdjacency(adjacencyMatrix);
        populationSize = getPositiveInput("Population size: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
        numberOfGeneration = getPositiveInput("Number of generations: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
        crossoverRate = getPositiveInput("Crossover rate: Please enter a positive integer in the range [0,100]", "Error: input must be a positive integer between [0,100]", -1, 101);

        while (true) {
            mutationRate = getPositiveInput(message, "Error: input must be a positive integer between [0,100]", -1, 101);
            if ((crossoverRate + mutationRate) < 101)
                break;
            else
                message = "The sum of crossover rate and mutation rate cannot exceed 100, please try again";
        }

        fillVerts();
        generateFirstGen();
        //printOrderings(genOrders);

        gui = new GUI(genOrders.get(0), genOrders2.get(0), currentGeneration, chunk, new ButtonListener());
    }

    public static int getPositiveInput(String message, String errorMessage, int greaterThan, int lessThan) {
        String tmp = message;
        int input;

        while (true) {
            String code = JOptionPane.showInputDialog(null, tmp);
            try {
                input = Integer.parseInt(code);
                if (input > greaterThan && input < lessThan)
                    return input;
                tmp = errorMessage + "\n" + message;
            } catch (Exception e) {
                tmp = errorMessage + "\n" + message;
            }
        }
    }

    public static int[][] parseInputFile() {
        int i = 0;
        ArrayList<ArrayList<Integer>> rowValues = new ArrayList<>();
        rowValues.add(new ArrayList<>());
        rowValues.add(new ArrayList<>());
        String[] rowValuesString;

        try {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                rowValuesString = data.split(" ");
                rowValues.get(0).add(Integer.parseInt(rowValuesString[0]));
                rowValues.get(1).add(Integer.parseInt(rowValuesString[1]));
                if (rowValues.get(0).get(i) > n)
                    n = rowValues.get(0).get(i);
                if (rowValues.get(1).get(i) > n)
                    n = rowValues.get(1).get(i);
                i++;
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        n++;
        int[][] adjacencyMatrix = new int[n][n];

        for (int j = 0; j < rowValues.get(0).size(); j++) {
            adjacencyMatrix[rowValues.get(0).get(j)][rowValues.get(1).get(j)] = 1;
            adjacencyMatrix[rowValues.get(1).get(j)][rowValues.get(0).get(j)] = 1;
        }

        return adjacencyMatrix;
    }

    public static void printAdjacency(int[][] adjacencyMatrix) {
        for (int i = 0; i < adjacencyMatrix.length; i++)
            System.out.print("\t" + i);
        System.out.print("\n");

        for (int i = 0; i < adjacencyMatrix.length; i++) {
            System.out.print(i + "\t");
            for (int j = 0; j < adjacencyMatrix[0].length; j++) {
                System.out.print(adjacencyMatrix[i][j] + "\t");
            }

            System.out.print("\n");
        }
    }

    public static void printOrderings(ArrayList<OrderingCost> o) {
        for (OrderingCost orderingCost : o) {
            for (int j = 0; j < orderingCost.getOrdering().size(); j++) {
                System.out.print(orderingCost.getOrdering().get(j).getNumber() + " ");
            }

            System.out.print("Cost:" + orderingCost.getCost());
            System.out.print("\n");
        }

        System.out.print("\n");
    }

    public static void generateFirstGen() {
        for (int i = 0; i < populationSize; i++) {
            Collections.shuffle(verts);
            OrderingCost ordering = new OrderingCost((ArrayList) verts.clone(), 0);
            ordering.setCost(fitnessFunk(ordering));
            genOrders.add(ordering);
            OrderingCost ordering2 = new OrderingCost((ArrayList) verts.clone(), 0);
            ordering2.setCost(timGAFunk(ordering2));
            genOrders2.add(ordering2);
        }

        orderOrderings();
        removeBottomThird();
    }

    //https://www.emis.de/journals/DM/v92/art5.pdf
    public static double timGAFunk(OrderingCost ordering) {
        long startTime = System.nanoTime();
        double cost;
        double tmpDist;
        double minDist = 1;
        double minDistSum = 0;
        double minNodeDist = 1;
        double totalEdgeCrossings = 0;
        double minNodeDistSum;
        Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);

        for (int i = 0; i < ordering.getOrdering().size(); i++) {
            Vertice vertOne = ordering.getOrdering().get(i);
            for (int j = 0; j < ordering.getOrdering().size(); j++) {
                Vertice v = ordering.getOrdering().get(j);
                if (v.getNumber() != vertOne.getNumber()) {
                    double[] vertX = coordinates.get(vertOne.getNumber());
                    double[] vertY = coordinates.get(v.getNumber());
                    tmpDist = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
                    minDist = Math.min(tmpDist, minDist);
                    minNodeDist = Math.min(minNodeDist, tmpDist);
                }
            }

            minDistSum += minDist;
            minDist = 1;
        }

        minNodeDistSum = (ordering.getOrdering().size() * Math.pow(minNodeDist, 2));
        double deviation = 0;
		int counter = 0;
        for (int i = 0; i < ordering.getOrdering().size(); i++) {
            Vertice vertOne = ordering.getOrdering().get(i);
            ArrayList<Integer> connections = vertOne.getConnections();
            for (Integer vertNumber : connections) {
                if (vertOne.getNumber() < vertNumber) {
                    Vertice v = null;
                    for (int loop = 0; loop < ordering.getOrdering().size(); loop++) {
                        if (vertNumber == ordering.getOrdering().get(loop).getNumber())
                            v = ordering.getOrdering().get(loop);
                    }
                    double[] vertX = coordinates.get(vertOne.getNumber());
                    double[] vertY = coordinates.get(v.getNumber());
                    tmpDist = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
                    deviation += Math.pow((tmpDist - minNodeDist), 2);
					counter++;
                }
            }
        }

        deviation = Math.sqrt((deviation / (ordering.getOrdering().size() - 1)));
		//https://en.wikipedia.org/wiki/Crossing_number_(graph_theory)
        totalEdgeCrossings = ((ordering.getOrdering().size()/2) * ((ordering.getOrdering().size()-1)/2) * ((counter/2)) * ((--counter)/2));

        double fitness = Math.abs((2 * minDistSum) - (2 * deviation) - (2.5 * (deviation / minNodeDistSum))
                + (0.25 * (ordering.getOrdering().size() * (Math.pow(minNodeDistSum, 2)))) - (1 * totalEdgeCrossings));
        
        cost = fitness;
        long endTime = System.nanoTime();
        long duration2 = (endTime - startTime);
        total2 += (double) duration2 / milliseconds;
        return cost;
    }

    public static int getEdgeCrossings(OrderingCost ordering) {
        int crossings = 0;

        for (int i = 0; i < ordering.getOrdering().size(); i++) {
            Vertice vertice = ordering.getOrdering().get(i);

            for (int tail : vertice.getConnections()) {
                if (tail < vertice.getNumber()) continue;
                crossings += countIntersections(vertice.getNumber(), tail, ordering);
            }
        }

        return crossings / 2;
    }

    public static int countIntersections(int start, int end, OrderingCost ordering) {
        int crossings = 0;
        Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);
        double[] startPoint = coordinates.get(start);
        double[] endPoint = coordinates.get(end);

        for (int i = 0; i < ordering.getOrdering().size(); i++) {
            if (i == start || i == end) continue;

            Vertice vertice = ordering.getOrdering().get(i);
            for (int num : vertice.getConnections()) {
                if (num < vertice.getNumber()) continue;
                if (num == start || num == end) continue;

                double[] compareStart = coordinates.get(vertice.getNumber());
                double[] compareEnd = coordinates.get(num);
                if (Line2D.linesIntersect(startPoint[0], startPoint[1], endPoint[0], endPoint[1], compareStart[0],
                        compareStart[1], compareEnd[0], compareEnd[1])) {
                    crossings++;
                }
            }
        }

        return crossings;
    }

    public static double fitnessFunk(OrderingCost ordering) {
        long startTime = System.nanoTime();
        double cost = 0;
        Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);

        for (int i = 0; i < ordering.getOrdering().size(); i++) {
            Vertice vertOne = ordering.getOrdering().get(i);
            ArrayList<Integer> connections = vertOne.getConnections();
            for (Integer vertNumber : connections) {
                if (vertOne.getNumber() < vertNumber) {
                    Vertice v = null;

                    for (int loop = 0; loop < ordering.getOrdering().size(); loop++) {
                        if (vertNumber == ordering.getOrdering().get(loop).getNumber())
                            v = ordering.getOrdering().get(loop);
                    }

                    double[] vertX = coordinates.get(vertOne.getNumber());
                    double[] vertY = coordinates.get(v.getNumber());
                    double distance = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
                    cost += distance;
                }
            }
        }

        long endTime = System.nanoTime();
        long duration1 = (endTime - startTime);
        total1 += (double) duration1 / milliseconds;
        return cost;
    }

    public static void orderOrderings() {
        Collections.sort(genOrders);
        Collections.sort(genOrders2);
    }

    public static void removeBottomThird() {
        int third = ((int) (Math.ceil(populationSize / 3.0)));

        for (int i = 0; i < third; i++) {
            genOrders.remove(genOrders.size() - (i + 1));
            genOrders.add(genOrders.get(i));
            genOrders2.remove(genOrders2.size() - (i + 1));
            genOrders2.add(genOrders2.get(i));
        }
    }

    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static void fillVerts() {
        ArrayList<Integer> connections = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adjacencyMatrix[j][i] == 1)
                    connections.add(j);
            }

            verts.add(new Vertice(i, (ArrayList) connections.clone()));
            connections.clear();
        }
    }

    public static void generateNextGen() {
        Collections.shuffle(genOrders);
        Collections.shuffle(genOrders2);
        int pr;

        while (genOrders.size() > 0) {
            pr = (int) (Math.random() * 101);
            if (crossoverRate >= pr && genOrders.size() > 1) {
                //do crossover
                genOrders = crossover(genOrders, 1);
                genOrders2 = crossover(genOrders2, 2);
            } else if (crossoverRate <= pr && pr <= (crossoverRate + mutationRate)) {
                //do mutation
                genOrders = mutation(genOrders, 1);
                genOrders2 = mutation(genOrders2, 2);
            } else if ((crossoverRate == mutationRate) && crossoverRate <= pr) {
                //do reproduction
                reproduction();
            }
        }

        genOrders = (ArrayList) nextGenOrders.clone();
        nextGenOrders.clear();
        genOrders2 = (ArrayList) nextGenOrders2.clone();
        nextGenOrders2.clear();

        //order the orderings s1,s2,s3 and replace s3 with s1
        orderOrderings();
        removeBottomThird();
    }

    public static void reproduction() {
        int randomIndex = (int) (Math.random() * genOrders.size());
        nextGenOrders.add(genOrders.get(randomIndex));
        genOrders.remove(randomIndex);

        randomIndex = (int) (Math.random() * genOrders2.size());
        nextGenOrders2.add(genOrders2.get(randomIndex));
        genOrders2.remove(randomIndex);
    }

    public static ArrayList<OrderingCost> crossover(ArrayList<OrderingCost> list, int fitnessChoice) {
        int index1 = 0;
        int index2 = 0;
        ArrayList<Vertice> cross1 = new ArrayList<>();
        ArrayList<Vertice> cross2 = new ArrayList<>();

        while (index1 == index2) {
            index1 = (int) (Math.random() * list.size());
            index2 = (int) (Math.random() * list.size());
        }

        int cp = (int) (Math.random() * (list.get(index1).getOrdering().size() - 2));
        cp++; // [1,N-2]

        for (int i = 0; i < list.get(index1).getOrdering().size(); i++) {
            if (i < cp) {
                cross1.add(list.get(index2).getOrdering().get(i));
                cross2.add(list.get(index1).getOrdering().get(i));
            } else {
                cross1.add(list.get(index1).getOrdering().get(i));
                cross2.add(list.get(index2).getOrdering().get(i));
            }
        }

        ArrayList<Integer> dupes = new ArrayList<>();
        ArrayList<Vertice> notInListVertice = (ArrayList) list.get(index1).getOrdering().clone();

        for (int i = 0; i < cross1.size(); i++) {
            for (int j = i + 1; j < cross1.size(); j++) {
                if (cross1.get(i).getNumber() == cross1.get(j).getNumber())
                    dupes.add(j); //index of duplicate
            }

            for (int p = 0; p < notInListVertice.size(); p++) {
                if (cross1.get(i).getNumber() == notInListVertice.get(p).getNumber())
                    notInListVertice.remove(p);
            }
        }

        Collections.shuffle(notInListVertice);

        for (int i = 0; i < dupes.size(); i++)
            cross1.set(dupes.get(i), notInListVertice.get(i));

        dupes.clear();
        notInListVertice = (ArrayList) list.get(index2).getOrdering().clone();

        for (int i = 0; i < cross2.size(); i++) {
            for (int j = i + 1; j < cross2.size(); j++) {
                if (cross2.get(i).getNumber() == cross2.get(j).getNumber())
                    dupes.add(j); //index of duplicate
            }

            for (int p = 0; p < notInListVertice.size(); p++) {
                if (cross2.get(i).getNumber() == notInListVertice.get(p).getNumber())
                    notInListVertice.remove(p);
            }
        }

        Collections.shuffle(notInListVertice);

        for (int i = 0; i < dupes.size(); i++)
            cross2.set(dupes.get(i), notInListVertice.get(i));

        if (fitnessChoice == 1) {
            OrderingCost temp1 = new OrderingCost(cross1, 0);
            temp1.setCost(fitnessFunk(temp1));
            nextGenOrders.add(temp1);
            OrderingCost temp2 = new OrderingCost(cross2, 0);
            temp2.setCost(fitnessFunk(temp2));
            nextGenOrders.add(temp2);
            if (index2 > index1) {
                list.remove(index2);
                list.remove(index1);
            } else {
                list.remove(index1);
                list.remove(index2);
            }
        } else {
            OrderingCost temp1 = new OrderingCost(cross1, 0);
            temp1.setCost(timGAFunk(temp1));
            nextGenOrders2.add(temp1);
            OrderingCost temp2 = new OrderingCost(cross2, 0);
            temp2.setCost(timGAFunk(temp2));
            nextGenOrders2.add(temp2);
            if (index2 > index1) {
                list.remove(index2);
                list.remove(index1);
            } else {
                list.remove(index1);
                list.remove(index2);
            }
        }

        return list;
    }

    public static ArrayList<OrderingCost> mutation(ArrayList<OrderingCost> list, int fitnessChoice) {
        int randomIndex = (int) (Math.random() * list.size());
        int index1 = 0;
        int index2 = 0;

        while (index1 == index2) {
            index1 = (int) (Math.random() * (list.get(randomIndex).getOrdering().size()));
            index2 = (int) (Math.random() * (list.get(randomIndex).getOrdering().size()));
        }

        ArrayList<Vertice> mutated = new ArrayList<>();

        for (int i = 0; i < list.get(randomIndex).getOrdering().size(); i++) {
            if (i == index1)
                mutated.add(list.get(randomIndex).getOrdering().get(index2));
            else if (i == index2)
                mutated.add(list.get(randomIndex).getOrdering().get(index1));
            else
                mutated.add(list.get(randomIndex).getOrdering().get(i));
        }

        OrderingCost temp = new OrderingCost(mutated, 0);

        if (fitnessChoice == 1) {
            temp.setCost(fitnessFunk(temp));
            nextGenOrders.add(temp);
            list.remove(randomIndex);
        } else {
            temp.setCost(timGAFunk(temp));
            nextGenOrders2.add(temp);
            list.remove(randomIndex);
        }

        return list;
    }

    static class ButtonListener implements ActionListener {
        public ButtonListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Next Generation")) {
                if (currentGeneration < numberOfGeneration) {
                    generateNextGen();
                    OrderingCost o = genOrders.get(0);
                    OrderingCost o2 = genOrders2.get(0);
                    gui.update(o, o2, o.getCost(), o2.getCost(), ++currentGeneration, total1, total2);
                }
            } else if (e.getActionCommand().equals("Last Generation")) {
                for (; currentGeneration < numberOfGeneration; ++currentGeneration) {
                    generateNextGen();
                    OrderingCost o = genOrders.get(0);
                    OrderingCost o2 = genOrders2.get(0);
                    gui.update(o, o2, o.getCost(), o2.getCost(), ++currentGeneration, total1, total2);
                }
                OrderingCost o = genOrders.get(0);
                OrderingCost o2 = genOrders2.get(0);
                gui.update(o, o2, o.getCost(), o2.getCost(), currentGeneration, total1, total2);
            }
        }
    }

    static class OrderingCost implements Comparable<OrderingCost> {
        ArrayList<Vertice> ordering;
        double cost;

        public OrderingCost(ArrayList<Vertice> ordering, double cost) {
            this.ordering = ordering;
            this.cost = cost;
        }

        @Override
        public int compareTo(OrderingCost oc) {
            return Double.compare(this.cost, oc.cost);
        }

        public ArrayList<Vertice> getOrdering() {
            return ordering;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder("\nCost: " + cost + "\n");
            for (Vertice i : ordering)
                out.append(i.getNumber()).append(", ");
            return out.toString();
        }

        public String toString2() {
            StringBuilder out = new StringBuilder();
            for (Vertice i : ordering)
                out.append(i.getNumber()).append(" ");
            return out.toString();
        }

        public Map<Integer, double[]> generateCoordinates(double chunk) {
            Map<Integer, double[]> coordinates = new HashMap<>();

            for (int i = 0; i < ordering.size(); i++) {
                double x = Math.cos(i * chunk);
                double y = Math.sin(i * chunk);
                coordinates.put(ordering.get(i).getNumber(), new double[]{x, y});
            }

            return coordinates;
        }
    }

    static class Vertice {
        int number;
        ArrayList<Integer> connections;

        public Vertice(int number, ArrayList<Integer> connections) {
            this.number = number;
            this.connections = connections;
        }

        public int getNumber() {
            return number;
        }

        public ArrayList<Integer> getConnections() {
            return connections;
        }
    }

    static class GUI extends JFrame {
        private static final int WIDTH = 1000;
        private static final int HEIGHT = 600;
        private static final String TITLE = "Graph Visualisation";
        private final double chunk;
        private final OrderingCost bestOrdering;
        private final OrderingCost bestOrdering2;
        private Painter graphPainter;
        private JLabel orderingLabel;
        private JLabel currentGenerationLabel;
        private JLabel bestGenerationLabel1;
        private JLabel bestOrderingFitnessCost;
        private JLabel timeToCalculate;
        private Painter graphPainter2;
        private JLabel orderingLabel2;
        private JLabel currentGenerationLabel2;
        private JLabel bestOrderingFitnessCost2;
        private JLabel bestGenerationLabel2;
        private JLabel timeToCalculate2;

        public GUI(OrderingCost bestOrdering, OrderingCost bestOrdering2,
                   int generations, double chunk, ActionListener listener) {
            super(TITLE);
            this.bestOrdering = bestOrdering;
            this.bestOrdering2 = bestOrdering2;
            this.chunk = chunk;
            setSize(WIDTH, HEIGHT);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            initMainPanel(listener);
            setVisible(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            // setUndecorated(true);
        }

        public void update(OrderingCost ordering, OrderingCost ordering2, double fitness, double fitness2, int generation
                , double time1, double time2) {
            orderingLabel.setText("Current best ordering: " + ordering.toString2());
            currentGenerationLabel.setText("Current generation: " + generation);
            bestOrderingFitnessCost.setText("Ordering Fitness: " + fitness);

            if (fitness < bestScore1) {
                bestScore1 = fitness;
                bestGeneration1 = generation;
                System.out.println("1 BEST GEN: " + bestGeneration1);
            }

            bestGenerationLabel1.setText("Best Generation: " + bestGeneration1);
            graphPainter.setOrdering(ordering);
            orderingLabel2.setText("Current best ordering: " + ordering2.toString2());
            currentGenerationLabel2.setText("Current generation: " + generation);
            bestOrderingFitnessCost2.setText("Ordering Fitness: " + fitness2);

            if (fitness2 < bestScore2) {
                bestScore2 = fitness2;
                bestGeneration2 = generation;
                System.out.println("2 BEST GEN: " + bestGeneration2);
            }

            bestGenerationLabel2.setText("Best Generation: " + bestGeneration2);
            timeToCalculate.setText("Time: " + time1 + " milliseconds");
            timeToCalculate2.setText("Time: " + time2 + " milliseconds");
            graphPainter2.setOrdering(ordering2);

            repaint();
        }

        private void initMainPanel(ActionListener listener) {
            JPanel mainPanel = new JPanel();
            mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            mainPanel.setLayout(new BorderLayout());

            orderingLabel = new JLabel("Current best ordering: " +
                    bestOrdering.toString2());
            currentGenerationLabel = new JLabel("Current generation: 0");
            bestOrderingFitnessCost = new JLabel("Ordering Fitness: " + bestOrdering.getCost());
            bestScore1 = bestOrdering.getCost();
            JButton generateNextPopulationButton = new JButton("Next Generation");
            JButton generateFinalPopulationButton = new JButton("Last Generation");
            JLabel fitnessNameLabel = new JLabel("Fitness Function: Sum of Edge Lenghts");
            timeToCalculate = new JLabel("Time: 0 milliseconds");
            bestGenerationLabel1 = new JLabel("Best Generation: " + bestGeneration1);
            orderingLabel2 = new JLabel("Current best ordering: " +
                    bestOrdering2.toString2());
            currentGenerationLabel2 = new JLabel("Current generation: 0");
            bestOrderingFitnessCost2 = new JLabel("Ordering Fitness: " + bestOrdering2.getCost());
            bestScore2 = bestOrdering2.getCost();
            JLabel fitnessNameLabel2 = new JLabel("Fitness Function: TimGA");
            timeToCalculate2 = new JLabel("Time: 0 milliseconds");
            bestGenerationLabel2 = new JLabel("Best Generation: " + bestGeneration2);
            graphPainter = new Painter(bestOrdering, chunk);
            graphPainter2 = new Painter(bestOrdering2, chunk);

            Border blackline = BorderFactory.createLineBorder(Color.black);
            JPanel topPanelGraphOne = new JPanel();
            JPanel topPanelGraphTwo = new JPanel();

            JPanel centerPanelButtons = new JPanel();
            centerPanelButtons.setBorder(blackline);
            centerPanelButtons.setLayout(new BoxLayout(centerPanelButtons, BoxLayout.Y_AXIS));
            centerPanelButtons.setPreferredSize(new Dimension(WIDTH, 300));
            centerPanelButtons.setPreferredSize(new Dimension(HEIGHT, 200));
            generateNextPopulationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            generateFinalPopulationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanelButtons.add(Box.createVerticalGlue());
            centerPanelButtons.add(generateNextPopulationButton);
            centerPanelButtons.add(Box.createVerticalGlue());
            centerPanelButtons.add(generateFinalPopulationButton);
            centerPanelButtons.add(Box.createVerticalGlue());

            topPanelGraphOne.setBorder(blackline);
            topPanelGraphTwo.setBorder(blackline);

            graphPainter.setBorder(blackline);
            graphPainter2.setBorder(blackline);

            topPanelGraphOne.setLayout(new BoxLayout(topPanelGraphOne, BoxLayout.Y_AXIS));
            topPanelGraphOne.setPreferredSize(new Dimension(WIDTH, 300));
            topPanelGraphOne.setPreferredSize(new Dimension(HEIGHT, 200));
            topPanelGraphOne.add(fitnessNameLabel);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(orderingLabel);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(currentGenerationLabel);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(bestOrderingFitnessCost);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(bestGenerationLabel1);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(timeToCalculate);
            topPanelGraphOne.add(Box.createVerticalGlue());
            topPanelGraphOne.add(graphPainter);

            topPanelGraphTwo.setLayout(new BoxLayout(topPanelGraphTwo, BoxLayout.Y_AXIS));
            topPanelGraphTwo.setPreferredSize(new Dimension(WIDTH, 300));
            topPanelGraphTwo.setPreferredSize(new Dimension(HEIGHT, 200));
            topPanelGraphTwo.add(fitnessNameLabel2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(orderingLabel2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(currentGenerationLabel2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(bestOrderingFitnessCost2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(bestGenerationLabel2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(timeToCalculate2);
            topPanelGraphTwo.add(Box.createVerticalGlue());
            topPanelGraphTwo.add(graphPainter2);

            mainPanel.add(topPanelGraphOne, BorderLayout.LINE_START);
            mainPanel.add(centerPanelButtons, BorderLayout.CENTER);
            mainPanel.add(topPanelGraphTwo, BorderLayout.LINE_END);

            add(mainPanel);

            generateNextPopulationButton.addActionListener(listener);
            generateFinalPopulationButton.addActionListener(listener);
        }
    }

    static class Painter extends JPanel {
        private static final int WIDTH = 400;
        private static final int HEIGHT = 400;
        private static final int RADIUS = 150;
        private static final int SHIFTX = WIDTH / 2;
        private static final int SHIFTY = HEIGHT / 2;
        private final double chunk;
        private OrderingCost ordering;
        private Map<Integer, double[]> coordinates;

        public Painter(OrderingCost ordering, double chunk) {
            this.ordering = ordering;
            this.chunk = chunk;
            coordinates = ordering.generateCoordinates(chunk);
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
        }

        public void setOrdering(OrderingCost ordering) {
            this.ordering = ordering;
            coordinates = ordering.generateCoordinates(chunk);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.red);
            g2.drawOval((WIDTH / 2) - RADIUS, (HEIGHT / 2) - RADIUS, RADIUS * 2, RADIUS * 2);
            g2.setColor(Color.black);

            for (int i = 0; i < ordering.getOrdering().size(); i++) {
                Vertice vert = ordering.getOrdering().get(i);
                ArrayList<Integer> nodeConnections = vert.getConnections();
                double[] nodePoint = coordinates.get(vert.getNumber());

                drawOrderingValue(nodePoint, i, vert.getNumber(), g2);
                drawNodePoint(nodePoint, g2);
                drawEdges(nodePoint, nodeConnections, g2);
            }
        }

        private void drawEdges(double[] nodePoint, ArrayList<Integer> connections,
                               Graphics2D g) {
            for (Integer nodeValue : connections) {
                double[] connectionPoint = coordinates.get(nodeValue);
                g.setColor(Color.black);
                g.setStroke(new BasicStroke(3));
                g.drawLine(
                        (int) (nodePoint[0] * RADIUS) + SHIFTX,
                        (int) (nodePoint[1] * RADIUS) + SHIFTY,
                        (int) (connectionPoint[0] * RADIUS) + SHIFTX,
                        (int) (connectionPoint[1] * RADIUS) + SHIFTY);
            }

        }

        private void drawOrderingValue(double[] nodePoint, int index, int label,
                                       Graphics2D g) {
            g.setStroke(new BasicStroke(4));
            if (index < ordering.getOrdering().size() / 2) {
                g.drawString(
                        Integer.toString(label),
                        (int) (nodePoint[0] * RADIUS) + SHIFTX + 7,
                        (int) (nodePoint[1] * RADIUS) + SHIFTY + 7);
            } else {
                g.drawString(
                        Integer.toString(label),
                        (int) (nodePoint[0] * RADIUS) + SHIFTX - 14,
                        (int) (nodePoint[1] * RADIUS) + SHIFTY - 14);
            }
        }

        private void drawNodePoint(double[] nodePoint, Graphics2D g) {
            g.setColor(Color.blue);
            g.fillOval(
                    (int) (nodePoint[0] * RADIUS) + SHIFTX - 7,
                    (int) (nodePoint[1] * RADIUS) + SHIFTY - 7,
                    14,
                    14);
        }
    }
}