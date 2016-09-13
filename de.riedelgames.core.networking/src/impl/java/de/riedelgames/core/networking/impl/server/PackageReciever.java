package de.riedelgames.core.networking.impl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;

public class PackageReciever implements Runnable {

    private final DatagramSocket socket;

    private final UDPConnection connection;

    private final BlockingQueue<byte[]> inQueue;

    private boolean running = true;

    public PackageReciever(DatagramSocket socket, UDPConnection connection, BlockingQueue<byte[]> inQueue) {
        this.socket = socket;
        this.connection = connection;
        this.inQueue = inQueue;
    }

    @Override
    public void run() {
        while (running) {
            recieveDataPackage();
        }
    }

    private void recieveDataPackage() {
        byte[] responseData = new byte[NetworkingConstants.PACKAGE_SIZE];
        DatagramPacket packet = new DatagramPacket(responseData, responseData.length);
        try {
            socket.receive(packet);
        } catch (SocketException e) {
            // Do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!UDPPackageUtils.isGamePackage(packet.getData())) {
            System.out.println(
                    "Recived unexpected Package. Protocol ID: " + UDPPackageUtils.getProtocolId(packet.getData()));
            return;
        }
        connection.addRecievedPackage(new DefaultPackage(packet.getData()));
        if (!UDPPackageUtils.isEmpty(packet.getData())) {
            inQueue.add(UDPPackageUtils.getData(packet.getData()));
        }
    }

    public void stop() {
        running = false;
        socket.close();
    }
}
