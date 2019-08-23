package de.riedelgames.core.networking.api.server;

import java.net.InetAddress;

public interface UDPServer {

    /**
     * Starts the server.
     * 
     * @return If successful.
     */
    public boolean start();

    /**
     * Stops the server.
     */
    public void stop();

    /**
     * Sets the UDP Port
     */
    public void setUDPPort(int port);

    /**
     * Adds data to the queue that will be send to this inetAddress.
     */
    public boolean queueData(InetAddress inetAddress, byte[] data);

    /**
     * retrieves Data for this inetAddress. Returns null if there isn't any.
     */
    public byte[] getHeadData(InetAddress inetAddress);

    /**
     * @return InetAddress[] containing the connected clients.
     */
    public InetAddress[] getConnectedClients();

    /**
     * Sets the visibility of this server, i. e. an extra UDP Package is send
     * every second.
     */
    public void setServerVisibility(boolean visible);

    /**
     * Controls whether the server accepts connections or not.
     */
    public void setServerOpen(boolean open);

}
