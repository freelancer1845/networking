package de.riedelgames.core.networking.api.server;

public interface UDPClient {

    /**
     * Starts the client thread with standard tick of 30.
     */
    public void start();

    /**
     * Starts the client thread with a specified tick.
     */
    public void start(int tick);

    /** Stops the client thread. */
    public void stop();

    /** Queues data that will be send on the next Tick. */
    public void queueData(byte[] data);

    /** Gets data from the inbound queue. Returns null if there isn't any. */
    public byte[] getQueuedData();
}
