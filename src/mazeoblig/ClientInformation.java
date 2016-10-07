package mazeoblig;

import simulator.CallbackInterface;
import simulator.PositionInMaze;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * <p>Title: ClientInformation</p>
 *
 * <p>Description: Contains some methods for interaction with the server</p>
 *
 * <p>Copyright: Copyright (c) 20016</p>
 */
public class ClientInformation extends BoxMaze implements ServerInterface {

    private HashMap<Integer, CallbackInterface> clientList = new HashMap();
    private HashMap<Integer, PositionInMaze> clientPositions = new HashMap();
    private ArrayList<Integer> listOfDisposables = new ArrayList();
    private int nextID = 0;
    private int sizeOfMaze = 50;

    /**
     * Calls the init(size) method in {@link BoxMaze } to create a new maze.
     * also invokes the {@link #startUpdater } method.
     * @param size: size of the maze to be created.
     * @throws RemoteException
     */
    ClientInformation(Integer size) throws RemoteException {
        if(size != null){
            sizeOfMaze = size;
        }
        init(sizeOfMaze);
        startUpdater();
    }

    /**
     * Creates a new instance of the {@link Updater },
     * and sets it as a daemon to make sure it doesn't exit along with JVM.
     * At the end it starts the new updater thread.
     */
    private void startUpdater() {
        Updater updater = new Updater();
        updater.setDaemon(true);
        updater.start();
    }

    /**
     * Connect the given {@link CallbackInterface} to this {@link BoxMaze}.
     * @param callback current {@link CallbackInterface}
     * @return The assigned {@link CallbackInterface} ID
     * @throws RemoteException
     */
    @Override
    public Integer setID(CallbackInterface callback) throws RemoteException {
        synchronized (clientList) {
            int id = nextID++;
            clientList.put(id, callback);
            return id;
        }
    }

    /**
     * Updates the position of the {@link CallbackInterface } identified by the given id
     * in the list of client positions {@link #clientList}.
     * @param id: id of the {@link CallbackInterface}
     * @param position
     */
    @Override
    public void updatePosition(Integer id, PositionInMaze position) {
        synchronized (clientPositions) {
            clientPositions.put(id, position);
        }
    }

    /**
     * Removes all {@link CallbackInterface}s in {@link #listOfDisposables}.
     */
    public void removeDisposableClients() {
        synchronized (clientList) {
            synchronized (clientPositions) {
                for (int i = 0; i < listOfDisposables.size(); i++) {
                    clientList.remove(listOfDisposables.get(i));
                    clientPositions.remove(listOfDisposables.get(i));
                }
                listOfDisposables = new ArrayList<>();
            }
        }
    }

    /**
     * Updates all known clients positions, and sends it to all Clients {@link CallbackInterface}
     */
    public synchronized void sendUpdatedPositionsToAllClients() {
        synchronized (clientList) {
            synchronized (clientPositions) {
                HashMap<String, Color> map = new HashMap<String, Color>();

                /** Prepare position/color map. */
                Set<Integer> keys = clientPositions.keySet();
                for(Integer key : keys){
                    PositionInMaze pos = clientPositions.get(key);
                    map.put(pos.getXpos() + "," + pos.getYpos(), pos.getColor());
                }

                /** Send the map to all current Users. */
                Set<Integer> ids = clientList.keySet();
                for(Integer id : ids){
                    CallbackInterface callbackInterface = clientList.get(id);
                    try {
                        callbackInterface.updateMazeMap(map);
                    } catch (RemoteException e) {
                        listOfDisposables.add(id);
                    }
                }

            }
        }
    }

    /**
     * Helper class that starts a new thread, calls {@link #removeDisposableClients()}
     * and {@link #sendUpdatedPositionsToAllClients()}.
     */
    private class Updater extends Thread{
        public void run() {
            try {
                while(true){
                    sleep(75);
                    removeDisposableClients();
                    if(clientList.size() > 0){
                        sendUpdatedPositionsToAllClients();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
