package mazeoblig;

/**
 * <p>Title: RMIServer</p>
 *
 * <p>Description: This is a server that connects to the rmiregistry that starts automatically
 * and runs server-objects on it.</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.net.*;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * RMIServer starts execution at the standard entry point "public static void main";
 * It creates an instance of itself and continues processing in the constructor.
 */

public class RMIServer {

  private final static int DEFAULT_PORT = 9000;
  private final static String DEFAULT_HOST = "undefined";
  public static int PORT = DEFAULT_PORT;
  private static String HOST_NAME;
  private static InetAddress myAdress = null;
  //private static RMIServer rmi;

  //private static BoxMaze maze;
  public static String MazeName = "Maze";


  public RMIServer() throws RemoteException, MalformedURLException,
                             NotBoundException, AlreadyBoundException {
    getStaticInfo();
    LocateRegistry.createRegistry(PORT);
    System.out.println( "RMIRegistry created on host computer " + HOST_NAME +
                        " on port " + Integer.toString( PORT) );

    /*
    ** Adds the maze
    */
    BoxMaze maze = new ClientInformation(Maze.DIM);
    System.out.println( "Remote implementation object created" );
    String urlString = "//" + HOST_NAME + ":" + PORT + "/" + MazeName;

    Naming.rebind( urlString, maze );
    System.out.println( "Bindings Finished, waiting for client requests." );
  }

    /**
     * METHOD THAT CAME WITH THE CODE,.
     */
  private static void getStaticInfo() {
    /**
     * Henter hostname på min datamaskin
     */
      if (HOST_NAME == null) HOST_NAME = DEFAULT_HOST;
      if (PORT == 0) PORT = DEFAULT_PORT;
      if (HOST_NAME.equals("undefined")) {
          try {
              myAdress = InetAddress.getLocalHost();
              }
          catch (java.net.UnknownHostException e) {
              System.err.println("Can't find my own networkaddress.");
              e.printStackTrace(System.err);
          }
      }
      else {
          System.out.println("A MazeServer is running already, use that!");
      }
      HOST_NAME = myAdress.getHostName();
      System.out.println("Maze server name: " + HOST_NAME);
      System.out.println("Maze server IP:   " + myAdress.getHostAddress());
  }

  public static int getRMIPort() { return PORT; }
  public static String getHostName() { return HOST_NAME; }

    /**
     * MAIN METHOD
     * Starts the {@link RMIServer}
     * @throws Exception
     */
   public static void main ( String[] args ) throws Exception {
      try {
          new RMIServer();
          Registry reg = LocateRegistry.getRegistry(HOST_NAME, PORT);
          reg.list();
          System.out.println("RMIRegistry on " + HOST_NAME + ":" + PORT + "\n----------------------------");
      }
      catch ( java.rmi.UnknownHostException uhe ) {
         System.out.println( "Maskinnavnet, " + HOST_NAME + " er ikke korrekt." );
      }
      catch ( RemoteException re ) {
         System.out.println( "Error starting service" );
         System.out.println( "" + re );
         re.printStackTrace(System.err);
      }
      catch ( MalformedURLException mURLe )
      {
         System.out.println( "Internal error" + mURLe );
      }
      catch ( NotBoundException nbe )
      {
         System.out.println( "Not Bound" );
         System.out.println( "" + nbe );
      }
      catch ( AlreadyBoundException abe )
      {
         System.out.println( "Already Bound" );
         System.out.println( "" + abe );
      }
   }
}
