package mazeoblig;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>Title: BoxMazeInterface</p>
 *
 * <p>Description: A interface that describes one method used in BoxMaze, getMaze</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface BoxMazeInterface extends Remote {
    Box [][] getMaze() throws RemoteException;
}
