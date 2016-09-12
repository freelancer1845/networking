package de.riedelgames.core.networking.api.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.riedelgames.core.networking.api.constants.PackageCodes;
import de.riedelgames.core.networking.impl.server.UDPClientImpl;

/**
 * Object to be used by the game. Wraps all the UDP depended code. May be used
 * as Input event handler. (I. e. for ward all Input evenst to this class via
 * fireInputEvent()).
 * 
 * @author Jascha Riedel
 *
 */
public class ClientNetworkTunnel {

    /** Underlying UDP Client. */
    private final UDPClient udpClient;

    /**
     * 
     * @param inetAddress
     *            of the server.
     * @param port
     *            to be used.
     */
    public ClientNetworkTunnel(InetAddress inetAddress, int port) {
        this.udpClient = new UDPClientImpl(inetAddress, port);

        intializeTunnel();
    }

    private void intializeTunnel() {
        udpClient.start();
    }

    /**
     * 
     * @param keyCode
     *            form {@link de.riedelgames.core.networking.api.constants.Keys}
     */
    public void fireInputKeyDown(byte keyCode) {
        byte[] packageData = new byte[] { PackageCodes.KEY_DOWN, keyCode };
        udpClient.queueData(packageData);
    }

    /**
     * 
     * @param keyCode
     *            form {@link de.riedelgames.core.networking.api.constants.Keys}
     */
    public void fireInputKeyUp(byte keyCode) {
        byte[] packageData = new byte[] { PackageCodes.KEY_UP, keyCode };
        udpClient.queueData(packageData);
    }

    public void dispose() {
        udpClient.stop();
    }

}
