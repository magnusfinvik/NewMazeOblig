package simulator;

import mazeoblig.Box;
import mazeoblig.ServerInterface;
import mazeoblig.Maze;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

/**
 * Instansen av denne klassen tilbyr i praksis tre metoder til programmereren. Disse er:
 * <p>
 * a. Konstrukt?ren (som tar imot en Maze som parameter<br>
 * b. getFirstIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 * ut av Maze og reposisjonerer "spilleren" ved starten av Maze basert p? en tilfeldig <br>
 * posisjonering av spilleren i Maze<br>
 * c. getIterationLoop() som returnerer en rekke med posisjoner i Maze som finner veien<br>
 * ut av Maze (fra inngangen) og reposisjonerer "spileren" ved starten av Maze p? nytt<br>
 * <p>
 * Ideen er at programmereren skal kunne benytte disse ferdig definerte posisjonene til ? simulere
 * hvordan en bruker forflytter seg i en labyrint.
 *
 * @author asd
 */

public class VirtualUser extends UnicastRemoteObject implements CallbackInterface {

	private Box[][] maze;
	private int dim;

	static int xp;
	static int yp;
	static boolean found = false;

	
	private Stack <PositionInMaze> myWay = new Stack<PositionInMaze>();
	private PositionInMaze [] FirstIteration; 
	private PositionInMaze [] NextIteration;

	private Color color;
	private Maze client;
	private HashMap<String, Color> usersMap = new HashMap();
	private Integer id;

	private ServerInterface server;

	private PositionInMaze[] moves;
	private int position;

	private boolean turn = true;

	/**
	 * Constructor for the {@link CallbackInterface} implementation.
	 * @param serverInterface Server connection
	 * @param mz client to be connected
	 * @param c color to use
	 * @throws RemoteException
	 */
	public VirtualUser(ServerInterface serverInterface, Maze mz, Color c) throws RemoteException {
		setColor(c);
		server = serverInterface;
		client = mz;
		this.maze = server.getMaze();
		dim = maze[0].length;
		init();
	}
	/**
	 * Initialise a random position in the maze
	 */
	private void init() {
		/*
		 * Setter en tifeldig posisjon i maze (xp og yp)
		 */
		Random rand = new Random();
		xp = rand.nextInt(dim - 2) + 1;
		yp = rand.nextInt(dim - 2) + 1;

		//Solves the way out of the maze based on a random entrance ...
		makeFirstIteration();
		// and then the maze is solved based on the entrance from the start
		makeNextIteration();
		setMoves();
		try{
			id = server.setID(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Solves maze from a random position in maze
	 */
	private void solveMaze() {
		found = false;
		// Siden posisjonen er tilfeldig valgt risikerer man at man kjører i en brønn
		// Av denne grunn .... det er noe galt med kallet under
		myWay.push(new PositionInMaze(xp, yp, color));
		backtrack(maze[xp][yp], maze[1][0]);
	}

	/**
	 * The backtracking-algorithm used to find the solution
	 * @param b Box
	 * @param from Box
	 */
	private void backtrack(Box b, Box from) {
		// Aller først - basistilfellet, slik at vi kan returnere
		// Under returen skrives det med Rødt
		if ((xp == dim - 2) && (yp == dim - 2)) {
			found = true;
			// Siden vi tegner den "riktige" veien under returen opp gjennom
			// Java's runtime-stack, så legger vi utgang inn sist ...
			return;
		}
		// Henter boksene som det finnes veier til fra den boksen jeg står i
		Box[] adj = b.getAdjecent();
		// Og sjekker om jeg kan gå de veiene
		for (int i = 0; i < adj.length; i++) {
			// Hvis boksen har en utganger som ikke er lik den jeg kom fra ...
			if (!(adj[i].equals(from))) {
				adjustXYBeforeBacktrack(b, adj[i]);
				myWay.push(new PositionInMaze(xp, yp, color));
				backtrack(adj[i], b);
				// Hvis algoritmen har funnet veien ut av labyrinten, så inneholder stacken (myWay) 
				// veien fra det tilfeldige startpunktet og ut av labyrinten
				if (!found) myWay.pop();
				adjustXYAfterBacktrack(b, adj[i]);
			}
			// Hvis veien er funnet, er det ingen grunn til å fortsette
			if (found) {
				break;
			}
		}
	}

	/**
	 * Update the x and y in the maze before the backtracking is called.
	 * @param from Box
	 * @param to Box
	 */
	private void adjustXYBeforeBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp()))) yp--;
		if ((from.getDown() != null) && (to.equals(from.getDown()))) yp++;
		if ((from.getLeft() != null) && (to.equals(from.getLeft()))) xp--;
		if ((from.getRight() != null) && (to.equals(from.getRight()))) xp++;
	}

	/**
	 * Update the x and y in the maze after the backtracking is called.
	 * @param from Box
	 * @param to Box
	 */
	private void adjustXYAfterBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp()))) yp++;
		if ((from.getDown() != null) && (to.equals(from.getDown()))) yp--;
		if ((from.getLeft() != null) && (to.equals(from.getLeft()))) xp++;
		if ((from.getRight() != null) && (to.equals(from.getRight()))) xp--;
	}

	/**
	 * Returns the route, from a random startup-point and out of the Maze as an array.
	 * @return [] PositionInMaze 
	 */
	private PositionInMaze [] solve() {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Returns the positions which gives a way around maze, randomly chosen, either right og left.
	 * @return [] PositionInMaze;
	 */
	private PositionInMaze [] roundAbout() {
		PositionInMaze [] pos = new PositionInMaze[dim * 2];
		int j = 0;
		pos[j++] = new PositionInMaze(dim - 2, dim - 1, color);
		// Vi skal enten gå veien rundt mot høyre ( % 2 == 0)
		// eller mot venstre
		if (System.currentTimeMillis() % 2 == 0) { 
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(dim - 1, i, color);
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, 0, color);
		}
		else {
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, dim - 1, color);
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(0, i, color);
		}
		// Uansett, så returneres resultatet
		return pos;
	}

	/**
	 * Solves the maze, from start-position.
	 * @return
	 */
	@SuppressWarnings("unused")
	private PositionInMaze [] solveFull() {
		solveMaze();
		PositionInMaze [] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Generates the way out of the maze from a random position, as well as
	 * around and to the entrance.
	 */
	private void makeFirstIteration() {
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		FirstIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Generates the way out of the maze from entrance-position in the maze, as well
	 * as the around and to the entrance of the maze again.
	 */
	private void makeNextIteration() {
		// Tvinger posisjonen til å være ved inngang av Maze
		xp = 1;
		yp = 1;
		myWay = new Stack<>();
		PositionInMaze [] outOfMaze = solve();
		PositionInMaze [] backToStart = roundAbout();
		NextIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Generic method that combines two arrays of the same type.
	 * @param <T>
	 * @param first
	 * @param second
	 * @return
	 */
	private static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Returns a {@link PositionInMaze}[] which contains x- and y- positions that
	 * a {@link VirtualUser} uses to find the way out of the maze from the
	 * entrace of the maze.
	 * @return nextIteration
	 */
	public PositionInMaze [] getIterationLoop() {
		return NextIteration;
	}

	/**
	 * Returns a {@link PositionInMaze}[] which contains x- and y- positions that
	 * a {@link VirtualUser} uses to find the way out of the maze from a
	 * randomly generated starting position.
	 * @return firstIteration
	 */
	public PositionInMaze [] getFirstIterationLoop() {
		return FirstIteration;
	}


	/**
	 * Recieves and updates the mazemap of all other client's positions
	 * @param mazeMap
	 * @throws RemoteException
	 */
	@Override
	public void updateMazeMap(HashMap<String, Color> mazeMap) throws RemoteException {
		usersMap = mazeMap;
		if (client != null){
			if (client.belongsToUser(id)) {
				client.repaint();
			}
		}

	}

	/**
	 * Returns a map of all known user's positions
	 * 
	 * @return HashMap of strings and colors
	 */
	public HashMap<String, Color> getMap() {
		return usersMap;
	}

	/**
	 * Performs a move and updatePosition {@link #server} about position
	 */
	public void move() {
		if (position < (moves.length - 1)) {
			position++;
		}else {setMoves();}

		try {
			server.updatePosition(id, moves[position]);
		} catch (RemoteException e) {
			System.out.println("Couldn't connect to Server to server. Exiting.");
			System.exit(0);
		}
	}

	/**
	 * Sets the next {@link #moves}.
	 */
	private void setMoves() {
		position = 0;
		turn = !turn;
		if (turn){ moves = getIterationLoop();}
		else { moves = getFirstIterationLoop();}
	}

	/**
	 * Return {@link CallbackInterface} Id.
	 */
	@Override
	public Integer getID() {
		return id;
	}
	
	/**
	 * Set Color for this {@link VirtualUser}
	 * @param given color given in to the method
	 */
	public void setColor (Color given) {
		if (given != null) {
			color = given;
			return;
		}
		Random generator = new Random();
		Color[] array = {Color.black, Color.white, Color.red, Color.green, Color.blue, Color.gray, Color.magenta, Color.pink};
	    int rnd = generator.nextInt(array.length);
	    color = array[rnd];
    }
}