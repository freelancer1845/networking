package de.riedelgames.core.networking.api.server;

public interface UDPServer {

    /**
     * Starts the server.
     */
    public void start();

    /**
     * Stops the server.
     */
    public void stop();

    /**
     * Sets the UDP Port
     */
    public void setUDPPort(int port);

}
