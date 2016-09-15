package de.riedelgames.core.networking.api.server;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import de.riedelgames.core.networking.api.constants.PackageCodes;
import de.riedelgames.core.networking.impl.server.UDPServerImpl;

/**
 * Easy Wrapper class that wraps the UDP code into game specific code.
 * 
 * @author Jascha Riedel
 *
 */
public class GameServerWrapper {

    /** The underlying UDP Server. */
    private final UDPServer udpServer;

    /** Hash Map containing newest Data. */
    private Map<InetAddress, NetworkDataWrapper> sortedDataMap = new HashMap<InetAddress, NetworkDataWrapper>();

    public GameServerWrapper() {
        this.udpServer = new UDPServerImpl();
    }

    public boolean startServer() {
        return udpServer.start();
    }

    public void stopServer() {
        udpServer.stop();
    }

    /**
     * Gets all recieved packages and puts them to their corresponding lists.
     */
    public void sortNetworkPackages() {
        InetAddress[] clients = udpServer.getConnectedClients();
        sortedDataMap.clear();
        for (int i = 0; i < clients.length; i++) {
            sortedDataMap.put(clients[i], sortPackages(clients[i]));
        }
    }

    public Map<InetAddress, NetworkDataWrapper> getSortedDataMap() {
        return sortedDataMap;
    }

    private NetworkDataWrapper sortPackages(InetAddress inetAddress) {

        NetworkDataWrapper dataWrapper = new NetworkDataWrapper();
        byte[] currentData;
        while ((currentData = udpServer.getHeadData(inetAddress)) != null) {
            switch (currentData[0]) {
            case PackageCodes.KEY_DOWN:
                dataWrapper.addKeyEvent(new NetworkKeyEvent(NetworkKeyEvent.KEY_EVENT_DOWN, currentData[1]));
                break;
            case PackageCodes.KEY_UP:
                dataWrapper.addKeyEvent(new NetworkKeyEvent(NetworkKeyEvent.KEY_EVENT_UP, currentData[1]));
                break;
            default:
                dataWrapper.addUnsortedData(currentData);
                break;
            }
        }

        return dataWrapper;
    }

    public void setVisibility(boolean visibility) {
        this.udpServer.setServerVisibility(visibility);
    }

    public void dispose() {
        this.udpServer.stop();
    }

    public InetAddress[] getConnectedClients() {
        return this.udpServer.getConnectedClients();
    }
}
