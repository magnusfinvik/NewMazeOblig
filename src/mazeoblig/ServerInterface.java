package mazeoblig;

import simulator.CallbackInterface;
import simulator.PositionInMaze;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>Title: ServerInterface</p>
 *
 * <p>Description: A interface that describes some methods used in {@Link ClientInformation}</p>
 *
 * <p>Copyright: Copyright (c) 2016</p>
 */
public interface ServerInterface extends Remote{

    /**
     * Connect the given {@Link CallbackInterface} to this {@Link BoxMaze}.
     * @param callback current CallbackInterface
     * @return The assigned {@Link CallbackInterface} ID
     * @throws RemoteException
     */
    Integer setID(CallbackInterface callback) throws RemoteException;

    /**
     * Updates the position of the {@Link CallbackInterface } identified by the given id
     * in the list of client positions {@Link ClientInformation clientList}.
     * @param id: id of the {@Link CallbackInterface}
     * @param positionInMaze: new {@Link PositionInMaze }
     * @throws RemoteException
     */
    void updatePosition(Integer id, PositionInMaze positionInMaze) throws RemoteException;

    /**
     * Gets {@link Maze} from the server.
     * @return
     * @throws RemoteException
     */
    Box [][] getMaze() throws RemoteException;
}
