package de.riedelgames.core.networking.impl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UDPPackage;

public class PackageSender implements Runnable {

    private final DatagramSocket socket;

    private final UDPConnection connection;

    private final BlockingQueue<byte[]> outQueue;

    private int tickrate;

    public PackageSender(DatagramSocket socket, UDPConnection connection, BlockingQueue<byte[]> outQueue,
            int tickrate) {
        this.socket = socket;
        this.connection = connection;
        this.outQueue = outQueue;
        this.tickrate = tickrate;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        int ticks = 0;
        long fpsTimer = System.currentTimeMillis();
        while (true) {
            sendDataPackage();
            while ((System.nanoTime() - lastTime) < 1000000000 * 1 / tickrate) {
                Thread.yield();
            }
            ticks++;
            if (ticks == tickrate) {
                if (NetworkingConstants.VERBOSE) {
                    double timeNeeded = (System.currentTimeMillis() - fpsTimer) / 1000;
                    System.out.println("Time Needed for: " + tickrate + " ticks: " + timeNeeded);
                    System.out.println(connection.getStats());
                    fpsTimer = System.currentTimeMillis();
                }
                ticks = 0;
            }
            lastTime = System.nanoTime();

        }
    }

    private void sendDataPackage() {
        byte[] rawData = outQueue.poll();
        if (rawData == null) {
            rawData = new byte[0];
        }
        UDPPackage udpPackage = new DefaultPackage(connection, rawData);
        byte[] packageData = udpPackage.getNetworkPackage();
        try {
            socket.send(new DatagramPacket(packageData, packageData.length, connection.getInetAddress(),
                    connection.getPort()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.addSendPackage(udpPackage);
    }

    public int getTickrate() {
        return tickrate;
    }

    public void setTickrate(int tickrate) {
        this.tickrate = tickrate;
    }

}
