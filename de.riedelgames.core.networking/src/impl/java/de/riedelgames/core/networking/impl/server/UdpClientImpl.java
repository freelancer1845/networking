package de.riedelgames.core.networking.impl.server;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UdpClient;

/**
 * A basic UDP Client.
 * 
 * @author Jascha Riedel
 *
 */
public class UdpClientImpl implements UdpClient {

    /** Tickrate that the client will run on. */
    private int tickrate = NetworkingConstants.DEFAULT_TICKRATE;

    /** Datagram Socket of the client. */
    private DatagramSocket datagramSocket = null;

    /** Port the socket will run on. */
    private int port = NetworkingConstants.DEFAULT_PORT;

    /** Connection of this client. */
    private final UdpConnection connection;

    /** Data queue. */
    private BlockingQueue<byte[]> outQueue = new ArrayBlockingQueue<byte[]>(500);

    /** Sender that will send all packages from the outQueue. */
    private PackageSender packageSender;

    /** PackageSender Thread. */
    private Thread packageSenderThread;

    /** Reciever that saves packages to the inQueue. */
    private PackageReciever packageReciever;

    /** Reciever Thread. */
    private Thread packageRecieverThread;

    /** In queue. */
    private BlockingQueue<byte[]> inQueue = new ArrayBlockingQueue<byte[]>(500);

    public UdpClientImpl(InetAddress inetAddress, int port) {
        this.connection = new UdpConnection(inetAddress, port);
    }

    public UdpClientImpl(DatagramSocket socket, InetAddress inetAddress, int port) {
        this.connection = new UdpConnection(inetAddress, port);
        this.datagramSocket = socket;
    }

    @Override
    public void start() {
        start(tickrate);
    }

    @Override
    public boolean start(int tick) {

        tickrate = tick;
        if (datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket(port);
            } catch (SocketException e) {
                System.out.println("Couldn't start client. Creating socket failed!");
                e.printStackTrace();
                return false;
            }
        }

        packageSender = new PackageSender(datagramSocket, connection, outQueue, tickrate);
        packageReciever = new PackageReciever(datagramSocket, connection, inQueue);
        packageSenderThread = new Thread(packageSender);
        packageSenderThread.setName("UDP Sender Thread");
        packageRecieverThread = new Thread(packageReciever);
        packageRecieverThread.setName("UDP Reciver Thread");
        packageSenderThread.start();
        packageRecieverThread.start();
        return true;

    }

    @Override
    public void stop() {
        if (packageSenderThread != null) {
            packageSender.stop();
            packageReciever.stop();
        } else {
            throw new RuntimeException("Server thread not created!");
        }
    }

    @Override
    public void queueData(byte[] data) {
        outQueue.add(data);
    }

    @Override
    public byte[] getQueuedData() {
        return inQueue.poll();
    }

}
