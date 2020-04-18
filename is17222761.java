import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/*
 * Thomas Kiely - 17185203
 * Sean Morrissey - 17222761
 * Paul Murphy - 17198046
 * Art Maguire - 16150201
 */

public class is17222761 extends JFrame {

    private static final String TITLE = "Graph Visualisation";
    private static final int WIDTH = 960;
    private static final int HEIGHT = 960;
    private static List<List<Integer>> edgeValues;
    private static int[][] currentPopulation;
    private static int[][] selectionPopulation;
    private static int[][] nextPopulation;
    private static int populationSize;
    private static int numberOfGenerations;
    private static int crossoverRate;
    private static int mutationRate;
    private static int n = 0;
    private static double chunk;
    private static List<double[]> circleCoordinates;
    private int[][] adjacencyMatrix;
    private int numberOfVertices;
    private int[] ordering;

    public is17222761(int[][] adjacencyMatrix, int[] ordering, int numberOfVertices) {
        this.adjacencyMatrix = adjacencyMatrix;
        this.ordering = ordering;
        this.numberOfVertices = numberOfVertices;
        this.chunk = (Math.PI * 2) / ((double) numberOfVertices);
        setTitle(TITLE);
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    //returns a positive integer
    public static int getPositiveInput(String message, String errorMessage, int greaterThan, int lessThan) {
        int input;
        while (true) {
            String code = JOptionPane.showInputDialog(null, message);
            try {
                input = Integer.parseInt(code);
                if (input > greaterThan && input < lessThan)
                    return input;
                message = errorMessage + "\n" + message;
            } catch (Exception e) {
                message = errorMessage + "\n" + message;
            }
        }
    }

    public static int[][] parseInputFile() {
        int i = 0;
        edgeValues = new ArrayList<>(2);
        edgeValues.add(new ArrayList<>());
        edgeValues.add(new ArrayList<>());
        String[] rowValuesString;

        try {
            File myObj = new File("input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                rowValuesString = data.split(" ");
                edgeValues.get(0).add(Integer.parseInt(rowValuesString[0]));
                edgeValues.get(1).add(Integer.parseInt(rowValuesString[1]));
                if (edgeValues.get(0).get(i) > n)
                    n = edgeValues.get(0).get(i);
                if (edgeValues.get(1).get(i) > n)
                    n = edgeValues.get(1).get(i);
                i++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        n++;
        int[][] adjacencyMatrix = new int[n][n];
        for (int j = 0; j < edgeValues.get(0).size(); j++) {
            adjacencyMatrix[edgeValues.get(0).get(j)][edgeValues.get(1).get(j)] = 1;
            adjacencyMatrix[edgeValues.get(1).get(j)][edgeValues.get(0).get(j)] = 1;
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

    public static void printOrderings(int[][] adjacencyMatrix) {
        System.out.print("\n");
        for (int i = 0; i < adjacencyMatrix.length; i++) {
            System.out.print("Ordering " + (i + 1) + ": ");
            for (int j = 0; j < adjacencyMatrix[0].length; j++) {
                System.out.print(adjacencyMatrix[i][j] + "\t");
            }
            System.out.print("\n");
        }
        System.out.println("\n");
    }

    public static void generateFirstGen() {
        ArrayList<Integer> ordering = new ArrayList<>();
        for (int i = 0; i < currentPopulation[0].length; i++) {
            ordering.add(i);
        }
        for (int i = 0; i < currentPopulation.length; i++) {
            Collections.shuffle(ordering);
            for (int j = 0; j < currentPopulation[i].length; j++) {
                currentPopulation[i][j] = ordering.get(j);
            }
        }
    }

    // Splits Orderings into 3 Selections, s1, s2, s3. Replaces s3 with s1
    private static void selection() {
        orderByCost();
        int sectionSize = (int) Math.floor(populationSize / (double) 3);

        // Replace 'worst' cost section with 'best' cost section
        for (int i = 0; i < sectionSize; i++) {
            int[] ordering = Arrays.copyOf(selectionPopulation[i], n);
            selectionPopulation[populationSize - sectionSize + i] = ordering;
        }
    }

    // Orders currentPopulation by cost by using the fitness function
    private static void orderByCost() {
        List<OrderingCost> orderingCosts = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            int[] ordering = currentPopulation[i];
            double cost = fitnessCost(ordering);
            orderingCosts.add(new OrderingCost(ordering, cost));
        }

        Collections.sort(orderingCosts);

        for (int i = 0; i < populationSize; i++)
            selectionPopulation[i] = orderingCosts.get(i).ordering;
    }

    // The fitness function as described by the pythagoras theorem for cartesian coordinates on a circle
    private static double fitnessCost(int[] ordering) {
        double cost = 0;
        for (int i = 0; i < edgeValues.get(0).size(); i++) {
            int edge1 = edgeValues.get(0).get(i);
            int edge2 = edgeValues.get(1).get(i);

            double[] edge1Coords = getCoordinates(edge1, ordering);
            double[] edge2Coords = getCoordinates(edge2, ordering);

            // Pythagoras Theorem
            // c = sqrt(a^2 + b^2);
            // a => edge1.x - edge2.x, b => edge1.y - edge2.y
            double distance = Math.sqrt(Math.pow(edge1Coords[0] - edge2Coords[0], 2) + Math.pow(edge1Coords[1] - edge2Coords[1], 2));

            cost += distance;
        }

        return cost;
    }

    // Finds the coordinated for a specific ordering node
    private static double[] getCoordinates(int e, int[] ordering) {
        int index = -1;
        for (int i = 0; i < ordering.length; i++) {
            if (ordering[i] == e) {
                index = i;
                break;
            }
        }

        return circleCoordinates.get(index);
    }

    // Gets the cartesian coordinates on a circle once
    private static void generateCircleCoordinates() {
        for (int i = 0; i < n; i++) {
            double x = Math.cos((double) i * chunk);
            double y = Math.sin((double) i * chunk);

            circleCoordinates.add(new double[]{x, y});
        }
    }

    // Shuffles currentPopulation
    private static void populateNewGeneration() {
        // Shuffle selection array so we can just pop orderings as random
        List<int[]> temp = Arrays.asList(selectionPopulation);
        Collections.shuffle(temp);
        temp.toArray(selectionPopulation);

        // The number of orderings that have been moved from selectionPopulation to the nextPopulation
        int count = 0;

        while (count < populationSize) {
            // Random number between [1, 100] (Math.random generates double between [0,1), so need to multiply by 101 and floor)
            int random = (int) Math.floor(Math.random() * 101);

            // Randomly select technique as shown in 5.b
            if (random <= crossoverRate && count < populationSize - 1) {
                crossover(count, count + 1);
                count += 2;
            } else if (random <= (crossoverRate + mutationRate)) {
                mutation(count);
                count++;
            } else {
                reproduction(count);
                count++;
            }
        }
    }

    // Crossover function, splits orderings on random number between [1, n - 1] and replaces duplicates with missing numbers
    private static void crossover(int pos1, int pos2) {
        // Random number between [1, n - 1]
        int randPos = (int) Math.floor((Math.random() * (n - 2)) + 1);

        int[] ordering1Selection1 = Arrays.copyOfRange(selectionPopulation[pos1], 0, randPos);
        int[] ordering1Selection2 = Arrays.copyOfRange(selectionPopulation[pos1], randPos, n);

        int[] ordering2Selection1 = Arrays.copyOfRange(selectionPopulation[pos2], 0, randPos);
        int[] ordering2Selection2 = Arrays.copyOfRange(selectionPopulation[pos2], randPos, n);

        int[] newOrdering1 = new int[n];
        System.arraycopy(ordering1Selection1, 0, newOrdering1, 0, randPos);
        System.arraycopy(ordering2Selection2, 0, newOrdering1, randPos, n - randPos);

        int[] newOrdering2 = new int[n];
        System.arraycopy(ordering2Selection1, 0, newOrdering2, 0, randPos);
        System.arraycopy(ordering1Selection2, 0, newOrdering2, randPos, n - randPos);

        nextPopulation[pos1] = removeDuplicatesCrossover(newOrdering1);
        nextPopulation[pos2] = removeDuplicatesCrossover(newOrdering2);
    }

    // Replaces duplicates with missing numbers
    private static int[] removeDuplicatesCrossover(int[] ordering) {
        // Gets index of duplicates
        List<Integer> toBeReplaced = new LinkedList<>();
        Map<Integer, Boolean> alreadyExists = new HashMap<>();

        for (int i = 0; i < ordering.length; i++) {
            if (!alreadyExists.containsKey(ordering[i]))
                alreadyExists.put(ordering[i], true);
            else
                toBeReplaced.add(i);
        }

        // Some randomness
        Collections.shuffle(toBeReplaced);

        for (int i = 0; i < n; i++) {
            if (!alreadyExists.containsKey(i)) {
                // Pop last
                int index = toBeReplaced.remove(toBeReplaced.size() - 1);
                ordering[index] = i;

                if (toBeReplaced.isEmpty())
                    break;
            }
        }

        return ordering;
    }

    // Mutation function, selects two random nodes and swaps them
    private static void mutation(int pos) {
        // Two unique random indices between [0, n - 1]
        int randPos1 = (int) Math.floor(Math.random() * n);
        int randPos2;

        do {
            randPos2 = (int) Math.floor(Math.random() * n);
        } while (randPos2 == randPos1);

        int rand1 = selectionPopulation[pos][randPos1];
        int rand2 = selectionPopulation[pos][randPos2];

        selectionPopulation[pos][randPos1] = rand2;
        selectionPopulation[pos][randPos2] = rand1;

        nextPopulation[pos] = Arrays.copyOf(selectionPopulation[pos], n);
    }

    // Reproduction function, copies random node in currentPopulation and places it into nextPopulation
    private static void reproduction(int pos) {
        nextPopulation[pos] = Arrays.copyOf(selectionPopulation[pos], n);
    }

    public static void main(String[] args) {
        String message = "Mutation rate: Please enter a positive integer in the range [0,100]";
        int[][] adjacencyMatrix = parseInputFile();
        printAdjacency(adjacencyMatrix);
        populationSize = getPositiveInput("Population size: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
        numberOfGenerations = getPositiveInput("Number of generations: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
        crossoverRate = getPositiveInput("Crossover rate: Please enter a positive integer in the range [0,100]", "Error: input must be a positive integer between [0,100]", -1, 101);
        while (true) {
            mutationRate = getPositiveInput(message, "Error: input must be a positive integer between [0,100]", -1, 101);
            if ((crossoverRate + mutationRate) < 101)
                break;
            else
                message = "The sum of crossover rate and mutation rate cannot exceed 100, please try again";
        }

        currentPopulation = new int[populationSize][n];
        nextPopulation = new int[populationSize][n];
        selectionPopulation = new int[populationSize][n];
        circleCoordinates = new ArrayList<>(n);

        generateFirstGen();
        System.out.println("Gen 0 Pop:");
        printOrderings(currentPopulation);

        chunk = (Math.PI * 2) / ((double) n);

        generateCircleCoordinates();

        System.out.println("Completed Generations:");

        for (int i = 0; i < numberOfGenerations; i++) {
            selection();
            populateNewGeneration();
            System.out.print(i + 1 + " / " + numberOfGenerations + "\r");
        }

        orderByCost();
        int[] bestOrdering = selectionPopulation[0];
        printOrderings(new int[][]{bestOrdering});
        double bestCost = fitnessCost(bestOrdering);
        System.out.println("Best Cost: " + bestCost);

        new is17222761(adjacencyMatrix, bestOrdering, n);
    }

    @Override
    public void paint(Graphics g) {
        int radius = 100;
        int mov = 200;

        for (int i = 0; i < numberOfVertices; i++) {
            int x1 = (int) (Math.cos(i * chunk) * radius) + mov;
            int y1 = (int) (Math.sin(i * chunk) * radius) + mov;

            g.drawString(String.valueOf(ordering[i]), x1, y1);

            for (int j = i + 1; j < numberOfVertices; j++) {
                int x2 = (int) (Math.cos(j * chunk) * radius) + mov;
                int y2 = (int) (Math.sin(j * chunk) * radius) + mov;

                if (adjacencyMatrix[ordering[i]][ordering[j]] == 1) {
                    g.drawLine(x1, y1, x2, y2);
                }
            }
        }
    }

    // Inner class for comparing costs and ordering by cost
    private static class OrderingCost implements Comparable<OrderingCost> {
        int[] ordering;
        double cost;

        public OrderingCost(int[] ordering, double cost) {
            this.ordering = ordering;
            this.cost = cost;
        }

        @Override
        public int compareTo(OrderingCost oc) {
            return Double.compare(this.cost, oc.cost);
        }

        @Override
        public String toString() {
            String out = "\nCost: " + cost + "\n";
            for (int i : ordering)
                out += i + ", ";
            return out;
        }
    }
}