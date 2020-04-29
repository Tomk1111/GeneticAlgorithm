import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.border.Border;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/* 
 * Thomas Kiely - 17185203
 * Sean Morrissey - 17222761
 * Paul Murphy - 17198046
 * Art Maguire: 16150201 
*/

public class is17222761 extends JFrame {
	
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
	private static ArrayList<OrderingCost> genOrders = new ArrayList<OrderingCost>(); //used for fitness function 1
	private static ArrayList<OrderingCost> nextGenOrders = new ArrayList<OrderingCost>(); //used for fitnes function 1
	private static ArrayList<OrderingCost> genOrders2 = new ArrayList<OrderingCost>(); //used for fitness function 2
	private static ArrayList<OrderingCost> nextGenOrders2 = new ArrayList<OrderingCost>(); //used for fitnes function 2

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
		gui = new GUI(genOrders.get(0),genOrders2.get(0),currentGeneration,chunk,new ButtonListener());
	}
	
	static class ButtonListener implements ActionListener {
        public ButtonListener(){};

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals("Next Generation")) {
				generateNextGen();
				//printOrderings(genOrders);
                OrderingCost o = genOrders.get(0);
                OrderingCost o2 = genOrders2.get(0);
                gui.update(o,o2,o.getCost(), o2.getCost(), ++currentGeneration, o.getTime(), o2.getTime());
            } else if(e.getActionCommand().equals("Last Generation")) {
				long total1=0;
				long total2=0;
                for(;currentGeneration < numberOfGeneration; ++currentGeneration) {
                    generateNextGen();
					OrderingCost o = genOrders.get(0);
					OrderingCost o2 = genOrders2.get(0);
					total1+=o.getTime();
					total2+=o2.getTime();
					//printOrderings(genOrders);
                }
                OrderingCost o = genOrders.get(0);
                OrderingCost o2 = genOrders2.get(0);
                gui.update(o,o2,o.getCost(), o2.getCost(),currentGeneration,total1, total2);
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
		ArrayList<Vertice> testList = new ArrayList<>();
		testList.add(verts.get(0));
		testList.add(verts.get(1));
		testList.add(verts.get(2));
		testList.add(verts.get(3));
		testList.add(verts.get(4));
		testList.add(verts.get(5));
		testList.add(verts.get(6));
		testList.add(verts.get(7));
		testList.add(verts.get(8));
		testList.add(verts.get(9));
		testList.add(verts.get(10));
		testList.add(verts.get(11));
		testList.add(verts.get(12));
		testList.add(verts.get(13));
		testList.add(verts.get(14));
		testList.add(verts.get(15));
		testList.add(verts.get(16));
		testList.add(verts.get(17));
		for(int i=0; i <populationSize;i++)
		{
			Collections.shuffle(verts);
			OrderingCost ordering = new OrderingCost((ArrayList)verts.clone(),0);
			ordering.setCost(fitnessFunk(ordering));
			genOrders.add(ordering);
			OrderingCost ordering2 = new OrderingCost((ArrayList)verts.clone(),0);
			ordering2.setCost(timGAFunk(ordering2));
			genOrders2.add(ordering2);
		}
		orderOrderings();
		removeBottomThird();
	}



	//https://www.emis.de/journals/DM/v92/art5.pdf
	public static double timGAFunk(OrderingCost ordering) {
		long startTime = System.nanoTime();
		double cost = 0;
		double tmpDist = 0;
		double minDist = 1;
		double minDistSum = 0;
		double minNodeDist = 1;
		double minEdgeLen = 0;
		int totalEdgeCrossings=0;
		double minNodeDistSum=0;
		Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);
		for(int i=0; i<ordering.getOrdering().size();i++)
		{
			Vertice vertOne = ordering.getOrdering().get(i);
			for(int j=0;j<ordering.getOrdering().size();j++)
			{
				Vertice v=ordering.getOrdering().get(j);
				if(v.getNumber()!=vertOne.getNumber())
				{
					double[] vertX = coordinates.get(vertOne.getNumber());
					double[] vertY = coordinates.get(v.getNumber());
					tmpDist = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
					minDist = Math.min(tmpDist, minDist);
					minNodeDist = Math.min(minNodeDist, tmpDist);
				}
			}
			minDistSum += minDist;
			minDist=1;
		}

		minNodeDistSum = (ordering.getOrdering().size() * Math.pow(minNodeDist, 2));
		double deviation = 0;

		for(int i=0; i<ordering.getOrdering().size();i++)
		{
			Vertice vertOne = ordering.getOrdering().get(i);
			ArrayList<Integer> connections = vertOne.getConnections();
			for(Integer vertNumber : connections)
			{
				if(vertOne.getNumber() < vertNumber)
				{

					Vertice v=null;
					for(int loop=0;loop<ordering.getOrdering().size();loop++) {
						if(vertNumber == ordering.getOrdering().get(loop).getNumber())
							v=ordering.getOrdering().get(loop);
					}
					double[] vertX = coordinates.get(vertOne.getNumber());
					double[] vertY = coordinates.get(v.getNumber());
					tmpDist = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
					deviation += Math.pow((tmpDist - minDist), 2);
				}
			}
		}

		deviation = Math.sqrt((deviation/populationSize));
		totalEdgeCrossings = getEdgeCrossings(ordering);

		double fitness = Math.abs((2 * minDistSum) - (2 * deviation) - (2.5 * (deviation /  minNodeDistSum)) 
				+ (0.25 * (populationSize * (Math.pow(minNodeDistSum, 2)))) + (1 * totalEdgeCrossings));

		cost = fitness;
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		duration=duration/1000000; //Milliseconds
		ordering.setTime(duration);
		return cost;
	}

	public static int getEdgeCrossings(OrderingCost ordering) {
		int crossings = 0;
		for (int i = 0;i < ordering.getOrdering().size();i++) {
			Vertice vertice = ordering.getOrdering().get(i);

			for (int tail : vertice.getConnections()) {
				if (tail < vertice.getNumber()) continue;
				crossings += countIntersections(vertice.getNumber(), tail, ordering);
			}
		}
		return  crossings/2;
	}

	public static int countIntersections(int start, int end, OrderingCost ordering) {
		int crossings = 0;
		Map<Integer, double[]> coordinates = ordering.generateCoordinates(chunk);
		double[] startPoint = coordinates.get(start);
		double[] endPoint = coordinates.get(end);
		for (int i = 0;i < ordering.getOrdering().size();i++) {
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
		for(int i=0; i<ordering.getOrdering().size();i++)
		{
			Vertice vertOne = ordering.getOrdering().get(i);
			ArrayList<Integer> connections = vertOne.getConnections();
			for(Integer vertNumber : connections)
			{
				if(vertOne.getNumber() < vertNumber)
				{
					
					Vertice v=null;
					for(int loop=0;loop<ordering.getOrdering().size();loop++) {
						if(vertNumber == ordering.getOrdering().get(loop).getNumber())
							v=ordering.getOrdering().get(loop);
					}
					double[] vertX = coordinates.get(vertOne.getNumber());
					double[] vertY = coordinates.get(v.getNumber()); //THESE VALUES ARE WRONG
					double distance = calculateDistance(vertX[0], vertX[1], vertY[0], vertY[1]);
					cost+=distance;
				}
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		duration=duration/1000000; //Milliseconds
		ordering.setTime(duration);
		return cost;
	}
	
	public static void orderOrderings() {
		Collections.sort(genOrders);
		Collections.sort(genOrders2);
	}
	
	public static void removeBottomThird() {
		int third = ((int)(Math.ceil(populationSize/3.0)));
		for(int i=0;i<third;i++) {
			genOrders.remove(genOrders.size()-(i+1));
			genOrders.add(genOrders.get(i));
			genOrders2.remove(genOrders2.size()-(i+1));
			genOrders2.add(genOrders2.get(i));
			//genOrders2.set(i,genOrders2.get(genOrders2.size()-(i+1)));
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
		Collections.shuffle(genOrders2);
		int pr=0;
		pr=(int)Math.random()*101;
		while(genOrders.size() > 0) {
			pr=(int)(Math.random()*101);
			if(crossoverRate >= pr && genOrders.size() > 1)
			{
				//do crossover
				crossover();
			}
			else if(crossoverRate<=pr && pr<=(crossoverRate+mutationRate))
			{
				//do mutation
				mutation();
			}
			else if((crossoverRate==mutationRate) && crossoverRate<=pr)
			{
				//do reproduction
				reproduction();
			}
		}
		//currentGen is empty, set currentGen=nextGen and clear nextGen
		genOrders=(ArrayList)nextGenOrders.clone();
		nextGenOrders.clear();
		genOrders2=(ArrayList)nextGenOrders2.clone();
		nextGenOrders2.clear();

		//order the orderings s1,s2,s3 and replace s3 with s1
		orderOrderings();
		removeBottomThird();
		//printOrderings(genOrders);




	}
	
	public static void reproduction(){
		int randomIndex =(int)(Math.random()*genOrders.size());
		nextGenOrders.add(genOrders.get(randomIndex));
		genOrders.remove(randomIndex);

		randomIndex =(int)(Math.random()*genOrders2.size());
		nextGenOrders2.add(genOrders2.get(randomIndex));
		genOrders2.remove(randomIndex);
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


//		PART 2

		index1=0;
		index2=0;
		cross1 = new ArrayList<Vertice>();
		cross2 = new ArrayList<Vertice>();
		while(index1==index2) {
			index1=(int)(Math.random()*genOrders2.size());
			index2=(int)(Math.random()*genOrders2.size());
		}
		cp=(int)(Math.random()*(genOrders2.get(index1).getOrdering().size()-2));
		cp++; // [1,N-2]
		for(int i=0;i<genOrders2.get(index1).getOrdering().size();i++) {
			if(i<cp) {
				cross1.add(genOrders2.get(index2).getOrdering().get(i));
				cross2.add(genOrders2.get(index1).getOrdering().get(i));
			} else {
				cross1.add(genOrders2.get(index1).getOrdering().get(i));
				cross2.add(genOrders2.get(index2).getOrdering().get(i));
			}
		}
		dupes = new ArrayList<Integer>();
		notInListVertice = (ArrayList)genOrders2.get(index1).getOrdering().clone();
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
		notInListVertice = (ArrayList)genOrders2.get(index2).getOrdering().clone();
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
		temp1 = new OrderingCost(cross1,0);
		temp1.setCost(timGAFunk(temp1));
		nextGenOrders2.add(temp1);
		temp2 = new OrderingCost(cross2,0);
		temp2.setCost(timGAFunk(temp2));
		nextGenOrders2.add(temp2);
		if(index2>index1) {
			genOrders2.remove(index2);
			genOrders2.remove(index1);
		} else {
			genOrders2.remove(index1);
			genOrders2.remove(index2);
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


		//PART 2

		randomIndex =(int)(Math.random()*genOrders2.size());
		index1=0;
		index2=0;
		while(index1==index2) {
			index1=(int)(Math.random()*(genOrders2.get(randomIndex).getOrdering().size()));
			index2=(int)(Math.random()*(genOrders2.get(randomIndex).getOrdering().size()));
		}
		mutated = new ArrayList<Vertice>();
		for(int i =0;i<genOrders2.get(randomIndex).getOrdering().size();i++)
		{
			if(i==index1)
				mutated.add(genOrders2.get(randomIndex).getOrdering().get(index2));
			else if(i==index2)
				mutated.add(genOrders2.get(randomIndex).getOrdering().get(index1));
			else
				mutated.add(genOrders2.get(randomIndex).getOrdering().get(i));
		}
		temp = new OrderingCost(mutated,0);
		temp.setCost(timGAFunk(temp));
		nextGenOrders2.add(temp);
		genOrders2.remove(randomIndex);
	}
	
	static class OrderingCost implements Comparable<OrderingCost> {
		ArrayList<Vertice> ordering = new ArrayList<Vertice>();
        double cost;
		long time;

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
		
		public void setTime(long time) {
			this.time=time;
		}
		
		public long getTime() {
			return this.time;
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
		
        public String toString2() {
            String out = "";
            for (Vertice i : ordering)
                out += i.getNumber() + " ";
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
		private static final int WIDTH = 1000;
		private static final int HEIGHT = 600;
		private static final String TITLE = "Graph Visualisation";

		private double chunk;
		private OrderingCost bestOrdering;
		private OrderingCost bestOrdering2;
		private int generations;

		private JPanel mainPanel;
		private JPanel topPanelGraphOne;
		private JPanel topPanelGraphTwo;
		private JPanel centerPanelButtons;
		
		private Painter graphPainter;
		private JLabel orderingLabel;
		private JLabel fitnessNameLabel;
		private JLabel currentGenerationLabel;
		private JLabel bestOrderingFitnessCost;
		private JLabel timeToCalculate;
		
		private Painter graphPainter2;
		private JLabel orderingLabel2;
		private JLabel fitnessNameLabel2;
		private JLabel currentGenerationLabel2;
		private JLabel bestOrderingFitnessCost2;
		private JLabel timeToCalculate2;
		
		private JButton generateNextPopulationButton;
		private JButton generateFinalPopulationButton;

		public GUI(OrderingCost bestOrdering, OrderingCost bestOrdering2,
				int generations,double chunk, ActionListener listener) {
			super(TITLE);
			this.bestOrdering=bestOrdering;
			this.bestOrdering2=bestOrdering2;
			this.chunk=chunk;
			this.generations=generations;
			setSize(WIDTH, HEIGHT);
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			initMainPanel(listener);
			setVisible(true);
		}

		public void update(OrderingCost ordering,OrderingCost ordering2, double fitness,double fitness2, int generation
						,long time1, long time2) {
			orderingLabel.setText("Current best ordering: " + ordering.toString2());
			currentGenerationLabel.setText("Current generation: " + generation);
			bestOrderingFitnessCost.setText("Ordering Fitness: " + fitness);
			graphPainter.setOrdering(ordering);
			orderingLabel2.setText("Current best ordering: " + ordering2.toString2());
			currentGenerationLabel2.setText("Current generation: " + generation);
			bestOrderingFitnessCost2.setText("Ordering Fitness: " + fitness2);
			timeToCalculate.setText("Time: "+time1+" milliseconds");
			timeToCalculate2.setText("Time: "+time2+" milliseconds");
			graphPainter2.setOrdering(ordering2);
			repaint();
		}

		private void initMainPanel(ActionListener listener) {
			mainPanel = new JPanel();
			mainPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
			mainPanel.setLayout(new BorderLayout());

			orderingLabel = new JLabel("Current best ordering: "+
					bestOrdering.toString2());
			currentGenerationLabel = new JLabel("Current generation: 0");
			bestOrderingFitnessCost = new JLabel("Ordering Fitness: " + bestOrdering.getCost());
			generateNextPopulationButton = new JButton("Next Generation");
			generateFinalPopulationButton = new JButton("Last Generation");
			fitnessNameLabel = new JLabel("Fitness Function: Sum of Edge Lenghts");
			timeToCalculate = new JLabel("Time: 0 milliseconds");
			
			orderingLabel2 = new JLabel("Current best ordering: "+
					bestOrdering2.toString2());
			currentGenerationLabel2 = new JLabel("Current generation: 0");
			bestOrderingFitnessCost2 = new JLabel("Ordering Fitness: " + bestOrdering2.getCost());
			fitnessNameLabel2 = new JLabel("Fitness Function: OTHER FITNESS FUNCTION");
			timeToCalculate2 = new JLabel("Time: 0 milliseconds");

			graphPainter = new Painter(bestOrdering,chunk);
			graphPainter2 = new Painter(bestOrdering2,chunk);
			
			Border blackline = BorderFactory.createLineBorder(Color.black);
			topPanelGraphOne = new JPanel();
			topPanelGraphTwo = new JPanel();
			
			centerPanelButtons = new JPanel();
			centerPanelButtons.setBorder(blackline);
			centerPanelButtons.setLayout(new BoxLayout(centerPanelButtons, BoxLayout.Y_AXIS));
			centerPanelButtons.setPreferredSize(new Dimension(WIDTH, 300));
			centerPanelButtons.setPreferredSize(new Dimension(HEIGHT, 200));
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
