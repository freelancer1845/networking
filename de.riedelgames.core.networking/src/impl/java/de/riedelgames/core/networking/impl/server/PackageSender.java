package de.riedelgames.core.networking.impl.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UDPPackage;

public class PackageSender implements Runnable {

    private final DatagramSocket socket;

    private final UdpConnection connection;

    private final BlockingQueue<byte[]> outQueue;

    private int tickrate;

    private TickrateHandler tickrateHandler;

    private boolean running = true;

    /**
     * Constructor.
     * 
     * @param socket that will be used.
     * @param connection that will be used.
     * @param outQueue containing data to send.
     * @param tickrate initial tickrate of the sender.
     */
    public PackageSender(DatagramSocket socket, UdpConnection connection,
            BlockingQueue<byte[]> outQueue, int tickrate) {
        this.socket = socket;
        this.connection = connection;
        this.outQueue = outQueue;
        this.tickrate = tickrate;
        tickrateHandler = new TickrateHandler(connection, this);
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        int ticks = 0;
        long fpsTimer = System.currentTimeMillis();
        while (running) {

            tickrateHandler.update();

            long now = System.currentTimeMillis();
            sendDataPackage();
            long delta = System.currentTimeMillis() - now;
            long sleepTime = (long) (1.0 / tickrate * 1000 - delta);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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
            socket.send(new DatagramPacket(packageData, packageData.length,
                    connection.getInetAddress(), connection.getPort()));
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

    public void stop() {
        this.running = false;
    }

}
