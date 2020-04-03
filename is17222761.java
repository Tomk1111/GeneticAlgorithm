import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

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
	private int[][] adjacencyMatrix;
	private int numberOfVertices;
	private int[] ordering;
	private double chunk;
	private int[][] currentPopulation;
	private int[][] nextPopulation;
	private int populationSize;
	private int numberOfGeneration;
	private int crossoverRate;
	private int MutationRate;
	
	public is17222761(int[][] adjacencyMatrix, int[] ordering, int numberOfVertices) {
		this.adjacencyMatrix = adjacencyMatrix;
		this.ordering = ordering;
		this.numberOfVertices = numberOfVertices;
		this.chunk = (Math.PI * 2) / ((double) numberOfVertices);
		setTitle(TITLE);
		setSize(WIDTH,HEIGHT);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	@Override
	public void paint(Graphics g) {
		int radius = 100;
		int mov = 200;
		
		for(int i = 0; i < numberOfVertices; i++) {
			for(int j = i + 1; j < numberOfVertices; j++) {
				if(adjacencyMatrix[ordering[i]][ordering[j]] == 1) {
					g.drawLine((int)(Math.cos(i * chunk) * radius) + mov,
							(int)(Math.sin(i * chunk) * radius) + mov,
							(int)(Math.cos(j * chunk) * radius) + mov,
							(int)(Math.sin(j * chunk) * radius) + mov);
				}
			}
		}
	}
	
	//returns a positive integer
	public static int getPositiveInput(String message) {
		int input;
		while(true) {
			String code = JOptionPane.showInputDialog(null, message);
			try {
				input = Integer.parseInt(code);
				if(input > 0)
					return input;
			}
			catch(Exception e) {
				message = "Error: input must be a positive integer\n" + message;
			}
		}
	}
	
	public static int[][] parseInputFile()
	{
		int n=0;
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
	
	public static void main (String [] args) {
		parseInputFile();
		printAdjacency(parseInputFile());
	}
}