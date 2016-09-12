package de.riedelgames.core.networking.impl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UDPClient;
import de.riedelgames.core.networking.api.server.UDPServer;

public class UDPServerImpl implements UDPServer, Runnable {

    /** Port used. */
    private int port = NetworkingConstants.DEFAULT_PORT;

    /** Thread that the server will run on. */
    private Thread serverThread;

    /** Datagram Socket used by the server. */
    private DatagramSocket socket;

    /** Map that saves the connection corresponding to a inet address. */
    private Map<InetAddress, UDPClient> connectionsMap = new HashMap<InetAddress, UDPClient>();

    @Override
    public void run() {
        byte[] recievedData = new byte[NetworkingConstants.PACKAGE_SIZE];
        System.out.println("Server Started.");
        while (true) {
            DatagramPacket recievePacket = new DatagramPacket(recievedData, recievedData.length);
            try {
                socket.receive(recievePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!checkProtocolId(recievePacket.getData())) {
                continue;
            }
            InetAddress ipAddress = recievePacket.getAddress();
            int recievedPort = recievePacket.getPort();
            if (!connectionsMap.containsKey(ipAddress)) {
                UDPClient client = new UDPClientImpl(socket, ipAddress, recievedPort);
                client.start();
                System.out.println("Client Connected: " + ipAddress.getHostAddress());
                connectionsMap.put(ipAddress, client);
            }
        }
    }

    @Override
    public void start() {
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        serverThread = new Thread(this);
        serverThread.start();
    }

    @Override
    public void stop() {
        if (serverThread != null) {
            serverThread.interrupt();
            socket.close();
        } else {
            throw new RuntimeException("Server thread not created!");
        }
    }

    @Override
    public void setUDPPort(int port) {
        this.port = port;
    }

    private boolean checkProtocolId(byte[] receviedData) {
        ByteBuffer wrapped = ByteBuffer.wrap(receviedData);
        int id = wrapped.getInt();
        if (id == NetworkingConstants.INTIAL_PACKAGE_PROTOCOL_ID) {
            return true;
        }
        return false;
    }

}
