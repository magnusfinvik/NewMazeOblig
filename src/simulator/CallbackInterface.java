package simulator;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * 
 * The {@link CallbackInterface} interface is here to let the {@link mazeoblig.BoxMaze
 * BoxMaze} send RMI callbacks to the {@link VirtualUser} instances running in
 * Worker Threads in the {@link mazeoblig.Maze Maze} applet.
 * 
 * @author runar
 * 
 */
public interface CallbackInterface extends Remote {

	/**
	 * Recieves and updates the mazemap of all other client's positions
	 * @param mazeMap
	 * @throws RemoteException
	 */
	void updateMazeMap(HashMap<String, Color> mazeMap) throws RemoteException;

	/**
	 * Return this {@link CallbackInterface}'s id.
	 * 
	 * @return the CallbackInterface id.
	 * @throws RemoteException
	 */
	Integer getID() throws RemoteException;
}
