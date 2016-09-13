package de.riedelgames.core.networking.impl.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UDPPackage;
import de.riedelgames.core.networking.api.server.UDPServer;

public class UDPServerImpl implements UDPServer, Runnable {

    /** Server open or not. */
    private boolean open = true;

    /** Server visible or not. */
    private boolean visible = true;

    /** Port used. */
    private int port = NetworkingConstants.DEFAULT_PORT;

    /** Tick rate of the Server. */
    private int tickrate = NetworkingConstants.DEFAULT_TICKRATE;

    /** Thread that the server will run on. */
    private Thread serverThread;

    /** Thread that will send all queued packages to the clients. */
    private Thread outBoundThread;

    /** Datagram Socket used by the server. */
    private DatagramSocket socket;

    /** Boolean, server running. */
    private boolean running;

    /** Map that saves the connection corresponding to a inet address. */
    private final ConcurrentMap<InetAddress, UDPConnection> connectionsMap = new ConcurrentHashMap<InetAddress, UDPConnection>();

    /**
     * HashMap that has a ArrayBlockingQueue (OutBound Packages) for every
     * client identified by its InetAddress.
     */
    private Map<InetAddress, ArrayBlockingQueue<byte[]>> outQueueMap = new HashMap<InetAddress, ArrayBlockingQueue<byte[]>>();

    /**
     * HashMap that has a ArrayBlockingQueue (InBound Packages) for every client
     * identified by its InetAddress.
     */
    private Map<InetAddress, ArrayBlockingQueue<byte[]>> inQueueMap = new HashMap<InetAddress, ArrayBlockingQueue<byte[]>>();

    private File logFile = new File("Z:/networkLog.txt");
    private PrintStream printStream;

    @Override
    public void run() {
        byte[] recievedData = new byte[NetworkingConstants.PACKAGE_SIZE];
        System.out.println("Server Started.");
        while (running) {
            DatagramPacket recievePacket = new DatagramPacket(recievedData, recievedData.length);
            try {
                socket.receive(recievePacket);
            } catch (SocketException e) {
                // Do nothing.
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!UDPPackageUtils.isGamePackage(recievePacket.getData())) {
                continue;
            }
            InetAddress inetAddress = recievePacket.getAddress();
            int recievedPort = recievePacket.getPort();
            UDPPackage udpPackage = new DefaultPackage(recievePacket.getData());
            if (!connectionsMap.containsKey(inetAddress)) {
                if (open) {
                    UDPConnection connection = new UDPConnection(inetAddress, recievedPort);
                    System.out.println("Client Connected: " + inetAddress.getHostAddress());
                    connectionsMap.put(inetAddress, connection);
                    inQueueMap.put(inetAddress, new ArrayBlockingQueue<byte[]>(500));
                    outQueueMap.put(inetAddress, new ArrayBlockingQueue<byte[]>(500));
                    connection.addRecievedPackage(udpPackage);
                    if (!udpPackage.isEmpty()) {
                        inQueueMap.get(inetAddress).add(udpPackage.getDataArray());
                    }
                }
            } else {
                UDPConnection connection = connectionsMap.get(inetAddress);
                connection.addRecievedPackage(udpPackage);
                inQueueMap.get(inetAddress).add(udpPackage.getDataArray());
            }
            for (int i = 0; i < udpPackage.getNetworkPackage().length; i++) {
                if (i == 14) {
                    printStream.print(" END OF HEADER: ");
                }
                printStream.print(i + ":[" + udpPackage.getNetworkPackage()[i] + "] ");
            }
            printStream.println();
        }
    }

    @Override
    public void start() {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        running = true;

        serverThread = new Thread(this);
        serverThread.setName("Server Reciver Thread");
        serverThread.start();
        outBoundThread = new Thread(new Runnable() {

            @Override
            public void run() {
                long lastTime = System.nanoTime();
                long visibleTimer = System.currentTimeMillis();
                while (running) {
                    sendPackagesToAllClients();
                    while ((System.nanoTime() - lastTime) < 1000000000 * 1 / tickrate) {
                        Thread.yield();
                    }
                    if (visible && System.currentTimeMillis() - visibleTimer > 1000) {
                        sendVisibilityPackage();
                        visibleTimer = System.currentTimeMillis();
                    }
                    lastTime = System.nanoTime();
                }
            }
        });
        outBoundThread.setName("Server Sender Thread");
        outBoundThread.start();

        logFile.delete();
        try {
            logFile.createNewFile();
            printStream = new PrintStream(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        if (serverThread != null) {
            running = false;
            socket.close();
        } else {
            throw new RuntimeException("Server thread not created!");
        }
    }

    @Override
    public void setUDPPort(int port) {
        this.port = port;
    }

    private void sendPackagesToAllClients() {
        for (InetAddress inetAddress : connectionsMap.keySet()) {
            byte[] outBoundData = outQueueMap.get(inetAddress).poll();
            sendSinglePackage(inetAddress, outBoundData);
        }
    }

    private void sendSinglePackage(InetAddress inetAdress, byte[] data) {
        UDPConnection connection = connectionsMap.get(inetAdress);
        UDPPackage outPackage = new DefaultPackage(connection, data);
        byte[] fullPackageData = outPackage.getNetworkPackage();
        try {
            this.socket.send(new DatagramPacket(fullPackageData, fullPackageData.length, inetAdress,
                    NetworkingConstants.DEFAULT_PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addSendPackage(outPackage);
        if (NetworkingConstants.VERBOSE) {
            System.out.println(connection.getStats());
        }
    }

    private void sendVisibilityPackage() {
        byte[] buf = new byte[NetworkingConstants.PACKAGE_SIZE];

        buf = "Standard Server".getBytes();
        InetAddress group;
        try {
            group = InetAddress.getByName(NetworkingConstants.GROUPNAME);
            DatagramPacket packet;
            packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean queueData(InetAddress inetAddress, byte[] data) {
        return outQueueMap.get(inetAddress).offer(data);
    }

    @Override
    public byte[] getHeadData(InetAddress inetAddress) {
        return inQueueMap.get(inetAddress).poll();
    }

    @Override
    public InetAddress[] getConnectedClients() {
        InetAddress[] connectedClients = new InetAddress[connectionsMap.size()];
        int i = 0;
        for (InetAddress address : connectionsMap.keySet()) {
            connectedClients[i] = address;
            i++;
        }
        return connectedClients;

    }

    @Override
    public void setServerVisibility(boolean open) {
        this.visible = open;
    }

    @Override
    public void setServerOpen(boolean open) {
        this.open = open;
    }
}
