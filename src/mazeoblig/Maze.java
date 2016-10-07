package mazeoblig;

import simulator.VirtualUser;

import java.applet.Applet;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Title: Maze</p>
 *
 * <p>Description: A simple applet that shows the randomized maze and its clients</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 */
/**
 * Draws a maze in the applet, based on the definition it gets from the {@link RMIServer}
 * On the other side {@link RMIServer} aquires the size of the maze from the definition in this Class
 *
 */
@SuppressWarnings("serial")
public class Maze extends Applet {

	private ServerInterface serverInterface;
	private Box[][] maze;
	private VirtualUser virtualUser;
	private int numberOfClients = 500;
	public static int DIM = 40;
	private int dim = DIM;

	private String server_hostname;
	private int server_portnumber;


	/**
	 * Gets the maze from {@link RMIServer}
	 * and initi
	 */
	public void init() {
		GroupOfPlayers players = new GroupOfPlayers(numberOfClients, this);
		players.setDaemon(true);
		players.start();
	}

	/**
	 * From the start code
	 *
	 * connects to the {@link RMIServer}, given that these run on the same computer
	 * otherwise it has to be rewritten.
	 * @return
	 */
		public ServerInterface connectToServer(){
		/*
		 ** Kobler opp mot RMIServer, under forutsetning av at disse
		 ** kjører på samme maskin. Hvis ikke må oppkoblingen
		 ** skrives om slik at dette passer med virkeligheten.
		 */
			if (server_hostname == null)
				server_hostname = RMIServer.getHostName();
			if (server_portnumber == 0)
				server_portnumber = RMIServer.getRMIPort();
			try {
				Registry r = LocateRegistry.getRegistry(server_hostname, server_portnumber);

				/*
				 ** Henter inn referansen til Labyrinten (ROR)
				 */
				serverInterface = (ServerInterface) r.lookup(RMIServer.MazeName);
				maze = serverInterface.getMaze();

				return serverInterface;
			}
			catch (RemoteException e) {
				System.err.println("Remote Exception: " + e.getMessage());
				System.exit(0);
			}
			catch (NotBoundException f) {
				/*
				 ** En exception her er en indikasjon på at man ved oppslag (lookup())
				 ** ikke finner det objektet som man søker.
				 ** Årsaken til at dette skjer kan være mange, men vær oppmerksom på
				 ** at hvis hostname ikke er OK (RMIServer gir da feilmelding under
				 ** oppstart) kan være en årsak.
				 */
				System.err.println("Not Bound Exception: " + f.getMessage());
				System.exit(0);
			}
			return null;
		}

	//Get Applet information
	//@Override	
	public String getAppletInfo() {
		return "Applet Information";
	}

	//Get parameter info
	//@Override
	public String[][] getParameterInfo() {
		java.lang.String[][] pinfo = { {"Size", "int", ""}, };
		return pinfo;
	}

	/**
	 * Method to check if the given id belongs to the user.
	 * @param id
	 * @return
	 */
	public boolean belongsToUser(Integer id) {
		boolean itBelongsToUser = virtualUser.getID().equals(id);
		return itBelongsToUser;
	}

	/**
	 * Viser labyrinten / tegner den i applet
	 * draws the maze in applet
	 * @param g Graphics
	 */
	//@Override
	public void paint (Graphics g) {
		int x;
		int y;

		// Tegner baser på box-definisjonene ....
		if(maze != null) {
			for (x = 1; x < (dim - 1); ++x)
				for (y = 1; y < (dim - 1); ++y) {
					if (maze[x][y].getUp() == null) {
						g.drawLine(x * 10, y * 10, x * 10 + 10, y * 10);
					}
					if (maze[x][y].getDown() == null) {
						g.drawLine(x * 10, y * 10 + 10, x * 10 + 10, y * 10 + 10);
					}
					if (maze[x][y].getLeft() == null) {
						g.drawLine(x * 10, y * 10, x * 10, y * 10 + 10);
					}
					if (maze[x][y].getRight() == null) {
						g.drawLine(x * 10 + 10, y * 10, x * 10 + 10, y * 10 + 10);
					}
				}
		}
			// If virtualUser exists, drawGraphicsOnMaze
		if (virtualUser != null) {
			drawGraphicsOnMaze(g);
		}
	}

	/**
	 * Draw the positions of all {@link VirtualUser} positions known to {@link #virtualUser}.
	 * 
	 * @param graphicsObject
	 */
	private void drawGraphicsOnMaze(Graphics graphicsObject) {
		HashMap<String, Color> map = virtualUser.getMap();

		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();

			String[] pos = key.split(",");
			int x = new Integer(pos[0]);
			int y = new Integer(pos[1]);

			graphicsObject.setColor(map.get(key));

			graphicsObject.fillOval((x * 10) + 2, (y * 10) + 2, 7, 7);
		}
		graphicsObject.setColor(Color.black);
	}

	/**
	 * Thread class used to create a number of new clients.
	 */
	private class GroupOfPlayers extends Thread {

		private int numberOfClients;
		private Maze maze;

		GroupOfPlayers(int c, Maze m) {
			numberOfClients = c;
			maze = m;
		}
		@Override
		public void run() {
			for(int i = numberOfClients; i != 0; i--) {
				try{
					Worker worker = new Worker(maze);
					worker.setDaemon(true);
					worker.start();
					sleep(100);
				} catch(InterruptedException e) {
					System.out.println("Interrupted.");
				}
			}
		}
	}

	/**
	 * Thread class that starts a new {@link VirtualUser}, and connects it to the server and maze.
	 */
	private class Worker extends Thread {
		private Maze maze;
		public Worker(Maze m) {
			maze = m;
		}
		@Override
		public void run() {
			try {

				VirtualUser vu;
				if (virtualUser == null) {
					vu = new VirtualUser(connectToServer(), maze, (virtualUser == null) ? Color.black : null);
				}else {
					vu = new VirtualUser(connectToServer(), null, (virtualUser == null) ? Color.black : null);
				}
				if(virtualUser == null){
					virtualUser = vu;
				}

				while (true) {
					sleep(100);
					vu.move();
				}
			} catch (InterruptedException ex) {
				System.out.println("Interrupted.");
			} catch (RemoteException e) {
				System.out.println("Connection to server failed. Try to restart the Maze applet.");
			}
		}
	}
}