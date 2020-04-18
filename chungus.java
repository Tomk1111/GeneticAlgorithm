import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/* 
 * Thomas Kiely - 17185203
 * Sean Morrissey - 17222761
 * Paul Murphy - 17198046
 * Art Maguire: 16150201 
*/

public class chungus extends JFrame {
	
	private static final String TITLE = "Graph Visualisation";
	private static final int WIDTH = 960;
	private static final int HEIGHT = 960;
	private static int[][] adjacencyMatrix;
	private int[] ordering;
	private static double chunk;
	private static int[][] currentPopulation;
	private static int[][] nextPopulation;
	private static int populationSize;
	private static int numberOfGeneration;
	private static int crossoverRate;
	private static int mutationRate;
	private static int n = 0;
	private static int currentGeneration=0;
	private static GUI gui;
	private static ArrayList<Vertice> verts = new ArrayList<Vertice>();
	private static ArrayList<OrderingCost> genOrders = new ArrayList<OrderingCost>();
	private static ArrayList<OrderingCost> nextGenOrders = new ArrayList<OrderingCost>();
	
	public static void main (String [] args) {
		String message = "Mutation rate: Please enter a positive integer in the range [0,100]";
		adjacencyMatrix=parseInputFile();
		chunk = (2 * Math.PI) / n;
		printAdjacency(adjacencyMatrix);
		populationSize=getPositiveInput("Population size: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
		numberOfGeneration=getPositiveInput("Number of generations: Please enter a positive integer", "Error: input must be a positive integer", 0, Integer.MAX_VALUE);
		crossoverRate=getPositiveInput("Crossover rate: Please enter a positive integer in the range [0,100]", "Error: input must be a positive integer between [0,100]", -1, 101);
		while(true) {
			mutationRate=getPositiveInput(message, "Error: input must be a positive integer between [0,100]", -1, 101);
			if((crossoverRate + mutationRate) < 101)
				break;
			else
				message="The sum of crossover rate and mutation rate cannot exceed 100, please try again";
		}
		fillVerts();
		generateFirstGen();
		printOrderings(genOrders);
		//generateNextGen();
		gui = new GUI(genOrders.get(0),currentGeneration,chunk,new ButtonListener());
	}
	
	static class ButtonListener implements ActionListener {
        public ButtonListener(){};

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Next Generation")) {
                generateNextGen();
				//printOrderings(genOrders);
                OrderingCost o = genOrders.get(0);
                gui.update(o, o.getCost(), ++currentGeneration);
            } else if(e.getActionCommand().equals("Last Generation")) {
                for(;currentGeneration < numberOfGeneration; ++currentGeneration) {
                    generateNextGen();
					//printOrderings(genOrders);
                }
                OrderingCost o = genOrders.get(0);
                gui.update(o, o.getCost(), currentGeneration);
            }
        }
    }
	
	//returns a positive integer
	public static int getPositiveInput(String message, String errorMessage, int greaterThan, int lessThan) {
		int input;
		while(true) {
			String code = JOptionPane.showInputDialog(null, message);
			try {
				input = Integer.parseInt(code);
				if(input > greaterThan && input < lessThan)
					return input;
				message = errorMessage + "\n" + message;
			}
			catch(Exception e) {
				message = errorMessage + "\n" + message;
			}
		}
	}
	
	public static int[][] parseInputFile()
	{
		int i=0;
		ArrayList<ArrayList<Integer>> rowValues = new ArrayList<ArrayList<Integer>>();
		rowValues.add(new ArrayList<Integer>());
		rowValues.add(new ArrayList<Integer>());
		String[] rowValuesString = new String [2];
		try {
			File myObj = new File("input.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				rowValuesString = data.split(" ");
				rowValues.get(0).add(Integer.parseInt(rowValuesString[0]));
				rowValues.get(1).add(Integer.parseInt(rowValuesString[1]));
				if(rowValues.get(0).get(i) > n)
					n=rowValues.get(0).get(i);
				if(rowValues.get(1).get(i) > n)
					n=rowValues.get(1).get(i);
				i++;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		n++;
		int[][] adjacencyMatrix = new int[n][n];
		for(int j=0;j < rowValues.get(0).size(); j++) {
			adjacencyMatrix[rowValues.get(0).get(j)][rowValues.get(1).get(j)]=1;
			adjacencyMatrix[rowValues.get(1).get(j)][rowValues.get(0).get(j)]=1;
		}
		return adjacencyMatrix;
	}
	
	public static void printAdjacency(int[][]adjacencyMatrix)
	{
		for(int i=0; i < adjacencyMatrix.length; i++)
			System.out.print("\t"+i);
		System.out.print("\n");
		for(int i=0; i < adjacencyMatrix.length; i++) {
			System.out.print(i+"\t");
			for(int j=0; j < adjacencyMatrix[0].length; j++) {
				System.out.print(adjacencyMatrix[i][j]+"\t");
			}
			System.out.print("\n");
		}
	}
	
	public static void printOrderings(ArrayList<OrderingCost> o)
	{		
		for(int i=0; i < o.size(); i++)
		{
			for(int j=0; j<o.get(i).getOrdering().size();j++)
			{
				System.out.print(o.get(i).getOrdering().get(j).getNumber()+" ");
			}
			System.out.print("Cost:"+o.get(i).getCost());
			System.out.print("\n");
		}
		System.out.print("\n");
	}
	
	public static void generateFirstGen() {
		for(int i=0; i <populationSize;i++)
		{
			Collections.shuffle(verts);
			OrderingCost ordering = new OrderingCost((ArrayList)verts.clone(),0);
			ordering.setCost(fitnessFunk(ordering));
			genOrders.add(ordering);
		}
		//orderOrderings();
		//removeBottomThird();
	}
	
	public static double fitnessFunk(OrderingCost ordering) {
		double cost = 0;
		Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);
		for(int i=0; i<ordering.getOrdering().size();i++)
		{
			Vertice vertOne = ordering.getOrdering().get(i);
			ArrayList<Integer> connections = vertOne.getConnections();
			for(Integer vertNumber : connections)
			{
				if(vertOne.getNumber() < vertNumber)
				{
					Vertice v = ordering.getOrdering().get(vertNumber);
					double[] vertX = coordinates.get(vertOne.getNumber());
					double[] vertY = coordinates.get(v.getNumber());
					double distance = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
					cost+=distance;
				}
			}
		}
		return cost;
	}
	
	public static void orderOrderings() {
		Collections.sort(genOrders);
	}
	
	public static void removeBottomThird() {
		int third = ((int)(Math.ceil(populationSize/3.0)));
		for(int i=0;i<third;i++) {
			//genOrders.get(genOrders.size()-(i+1))=genOrders.get(i);
			genOrders.remove(genOrders.size()-(i+1));
			genOrders.add(genOrders.get(i));
		}
	}
	
	public static double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1,2) + Math.pow(y2 - y1,2));
	}
	
	public static void fillVerts () {
		ArrayList<Integer>connections=new ArrayList<Integer>();
		for(int i=0;i<n;i++) {
			for(int j=0;j<n;j++)
			{
				if(adjacencyMatrix[j][i] == 1)
					connections.add(j);
			}
			verts.add(new Vertice(i,(ArrayList)connections.clone()));
			connections.clear();
		}
	}
	
	public static void generateNextGen() {
		Collections.shuffle(genOrders);
		int pr=0;
		pr=(int)Math.random()*101;
		while(genOrders.size() > 0) {
			pr=(int)(Math.random()*101);
			if(crossoverRate >= pr && genOrders.size() > 1)
			{
				//do crossover
				crossover();
			}
			else if(pr<=(crossoverRate+mutationRate))
			{
				//do mutation
				mutation();
			}
			//else if((crossoverRate+mutationRate)<=pr)
			//{
				//do reproduction
			//	reproduction();
			//}
		}
		//currentGen is empty, set currentGen=nextGen and clear nextGen
		genOrders=(ArrayList)nextGenOrders.clone();
		nextGenOrders.clear();
		//order the orderings s1,s2,s3 and replace s3 with s1
		orderOrderings();
		removeBottomThird();
		//printOrderings(genOrders);
	}
	
	public static void reproduction(){
		int randomIndex =(int)(Math.random()*genOrders.size());
		nextGenOrders.add(genOrders.get(randomIndex));
		genOrders.remove(randomIndex);
	}
	
	public static void crossover(){
		int index1=0;
		int index2=0;
		ArrayList<Vertice> cross1 = new ArrayList<Vertice>();
		ArrayList<Vertice> cross2 = new ArrayList<Vertice>();
		while(index1==index2) {
			index1=(int)(Math.random()*genOrders.size());
			index2=(int)(Math.random()*genOrders.size());
		}
		int cp=(int)(Math.random()*(genOrders.get(index1).getOrdering().size()-2));
		cp++; // [1,N-2]
		for(int i=0;i<genOrders.get(index1).getOrdering().size();i++) {
			if(i<cp) {
				cross1.add(genOrders.get(index2).getOrdering().get(i));
				cross2.add(genOrders.get(index1).getOrdering().get(i));
			} else {
				cross1.add(genOrders.get(index1).getOrdering().get(i));
				cross2.add(genOrders.get(index2).getOrdering().get(i));
			}
		}
		ArrayList<Integer> dupes = new ArrayList<Integer>();
		ArrayList<Vertice> notInListVertice = (ArrayList)genOrders.get(index1).getOrdering().clone();
		for(int i=0;i<cross1.size();i++){
			for(int j=i+1;j<cross1.size();j++) {
				if(cross1.get(i).getNumber()==cross1.get(j).getNumber())
					dupes.add(j); //index of duplicate
			}
			for(int p=0;p<notInListVertice.size();p++)
			{
				if(cross1.get(i).getNumber()==notInListVertice.get(p).getNumber())
					notInListVertice.remove(p);
			}
		}
		Collections.shuffle(notInListVertice);
		for(int i=0;i<dupes.size();i++)
			cross1.set(dupes.get(i),notInListVertice.get(i));
		dupes.clear();
		notInListVertice = (ArrayList)genOrders.get(index2).getOrdering().clone();
		for(int i=0;i<cross2.size();i++){
			for(int j=i+1;j<cross2.size();j++) {
				if(cross2.get(i).getNumber()==cross2.get(j).getNumber())
					dupes.add(j); //index of duplicate
			}
			for(int p=0;p<notInListVertice.size();p++)
			{
				if(cross2.get(i).getNumber()==notInListVertice.get(p).getNumber())
					notInListVertice.remove(p);
			}
		}
		Collections.shuffle(notInListVertice);
		for(int i=0;i<dupes.size();i++)
			cross2.set(dupes.get(i),notInListVertice.get(i));
		OrderingCost temp1 = new OrderingCost(cross1,0);
		temp1.setCost(fitnessFunk(temp1));
		nextGenOrders.add(temp1);
		OrderingCost temp2 = new OrderingCost(cross2,0);
		temp2.setCost(fitnessFunk(temp2));
		nextGenOrders.add(temp2);
		if(index2>index1) {
			genOrders.remove(index2);
			genOrders.remove(index1);
		} else {
			genOrders.remove(index1);
			genOrders.remove(index2);
		}
	}
	
	public static void mutation(){
		int randomIndex =(int)(Math.random()*genOrders.size());
		int index1=0;
		int index2=0;
		while(index1==index2) {
			index1=(int)(Math.random()*(genOrders.get(randomIndex).getOrdering().size()));
			index2=(int)(Math.random()*(genOrders.get(randomIndex).getOrdering().size()));
		}
		ArrayList<Vertice> mutated = new ArrayList<Vertice>();
		for(int i =0;i<genOrders.get(randomIndex).getOrdering().size();i++)
		{
			if(i==index1)
				mutated.add(genOrders.get(randomIndex).getOrdering().get(index2));
			else if(i==index2)
				mutated.add(genOrders.get(randomIndex).getOrdering().get(index1));
			else
				mutated.add(genOrders.get(randomIndex).getOrdering().get(i));
		}
		OrderingCost temp = new OrderingCost(mutated,0);
		temp.setCost(fitnessFunk(temp));
		nextGenOrders.add(temp);
		genOrders.remove(randomIndex);
	}
	
	static class OrderingCost implements Comparable<OrderingCost> {
		ArrayList<Vertice> ordering = new ArrayList<Vertice>();
        double cost;

        public OrderingCost(ArrayList<Vertice> ordering, double cost) {
            this.ordering = ordering;
            this.cost = cost;
        }

        @Override
        public int compareTo(OrderingCost oc) {
            return Double.compare(this.cost, oc.cost);
        }
		
		public void setCost(double cost) {
			this.cost=cost;
		}
		
		public ArrayList<Vertice> getOrdering() {
			return ordering;
		}
		
		public double getCost() {
			return cost;
		}

        @Override
        public String toString() {
            String out = "\nCost: " + cost + "\n";
            for (Vertice i : ordering)
                out += i.getNumber() + ", ";
            return out;
        }
		
		
		public Map<Integer, double[]> generateCoordinates(double chunk) {
			Map<Integer, double[]> coordinates = new HashMap<Integer, double[]>();
			for(int i = 0; i < ordering.size(); i++) {
				double x = Math.cos(i * chunk);
				double y = Math.sin(i * chunk);
				coordinates.put(ordering.get(i).getNumber(), new double[]{x, y});
			}
			return coordinates;
		}
    }
	
	static class Vertice {
		int number;
		ArrayList<Integer> connections = new ArrayList<Integer>();
		
		public Vertice(int number, ArrayList<Integer> connections)
		{
			this.number=number;
			this.connections=connections;
		}
		
		public int getNumber() {
			return number;
		}
		
		public ArrayList<Integer> getConnections() {
			return connections;
		}
	}

	static class GUI extends JFrame {
		private static final int WIDTH = 760;
		private static final int HEIGHT = 760;
		private static final String TITLE = "Graph Visualisation";

		private double chunk;
		private OrderingCost bestOrdering;
		private int generations;

		private JPanel mainPanel;
		private JPanel bottomPanel;
		private Painter graphPainter;
		private JLabel orderingLabel;
		private JLabel currentGenerationLabel;
		private JLabel bestOrderingFitnessCost;
		private JButton generateNextPopulationButton;
		private JButton generateFinalPopulationButton;

		public GUI(OrderingCost bestOrdering,
				int generations,double chunk, ActionListener listener) {
			super(TITLE);
			this.bestOrdering=bestOrdering;
			this.chunk=chunk;
			this.generations=generations;
			setSize(WIDTH, HEIGHT);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			initMainPanel(listener);
			setVisible(true);
		}

		public void update(OrderingCost ordering, double fitness, int generation) {
			orderingLabel.setText("Current best ordering  " + ordering);
			currentGenerationLabel.setText("Current generation: " + generation);
			bestOrderingFitnessCost.setText("Ordering Fitness: " + fitness);
			graphPainter.setOrdering(ordering);
			repaint();
		}

		private void initMainPanel(ActionListener listener) {
			mainPanel = new JPanel();
			mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			orderingLabel = new JLabel("Current best ordering  "+
					bestOrdering.toString());
			currentGenerationLabel = new JLabel("Current generation: 0");
			bestOrderingFitnessCost = new JLabel("Ordering Fitness: " + bestOrdering.getCost());
			generateNextPopulationButton = new JButton("Next Generation");
			generateFinalPopulationButton = new JButton("Last Generation");

			graphPainter = new Painter(bestOrdering,chunk);

			bottomPanel = new JPanel();
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
			bottomPanel.setPreferredSize(new Dimension(WIDTH, 200));
			bottomPanel.add(orderingLabel);
			bottomPanel.add(Box.createVerticalGlue());
			bottomPanel.add(currentGenerationLabel);
			bottomPanel.add(Box.createVerticalGlue());
			bottomPanel.add(bestOrderingFitnessCost);
			bottomPanel.add(Box.createVerticalGlue());
			bottomPanel.add(generateNextPopulationButton);
			bottomPanel.add(Box.createVerticalGlue());
			bottomPanel.add(generateFinalPopulationButton);
			bottomPanel.add(Box.createVerticalGlue());
			mainPanel.add(bottomPanel);
			mainPanel.add(graphPainter);
			add(mainPanel);

			generateNextPopulationButton.addActionListener(listener);
			generateFinalPopulationButton.addActionListener(listener);
		}
	}

	static class Painter extends JPanel {
		private static final int WIDTH = 700;
		private static final int HEIGHT = 500;
		private static final int RADIUS = 150;
		private static final int SHIFTX = WIDTH/2;
		private static final int SHIFTY = HEIGHT/2;

		private OrderingCost ordering;
		private double chunk;
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
			Graphics2D g2 = (Graphics2D)g;

			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.red);
			g2.drawOval((WIDTH/2)-RADIUS, (HEIGHT/2)-RADIUS, RADIUS*2, RADIUS*2);
			g2.setColor(Color.black);

			for(int i = 0; i < ordering.getOrdering().size(); i++) {
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
			for(Integer nodeValue : connections) {
				double[] connectionPoint = coordinates.get(nodeValue);
				g.setColor(Color.black);
				g.setStroke(new BasicStroke(3));
				g.drawLine(
						(int)(nodePoint[0] * RADIUS) + SHIFTX,
						(int)(nodePoint[1] * RADIUS) + SHIFTY,
						(int)(connectionPoint[0] * RADIUS) + SHIFTX,
						(int)(connectionPoint[1] * RADIUS) + SHIFTY);
			}

		}

		private void drawOrderingValue(double[] nodePoint, int index, int label,
				Graphics2D g) {
			g.setStroke(new BasicStroke(4));
			if (index < ordering.getOrdering().size() / 2) {
				g.drawString(
						Integer.toString(label),
						(int)(nodePoint[0] * RADIUS) + SHIFTX + 7,
						(int)(nodePoint[1] * RADIUS) + SHIFTY + 7);
			} else {
				g.drawString(
						Integer.toString(label),
						(int)(nodePoint[0] * RADIUS) + SHIFTX - 14,
						(int)(nodePoint[1] * RADIUS) + SHIFTY - 14);
			}
		}

		private void drawNodePoint(double[] nodePoint, Graphics2D g) {
			g.setColor(Color.blue);
			g.fillOval(
					(int)(nodePoint[0] * RADIUS) + SHIFTX - 7,
					(int)(nodePoint[1] * RADIUS) + SHIFTY - 7,
					14,
					14);
		}
	}
}
