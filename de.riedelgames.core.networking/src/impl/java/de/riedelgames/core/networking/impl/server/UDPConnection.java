package de.riedelgames.core.networking.impl.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.riedelgames.core.networking.api.server.UDPPackage;

public class UDPConnection {

    private final InetAddress inetAddress;

    private final int port;

    private int localSequenceNumber;

    private int remoteSequenceNumber;

    private Map<Integer, UDPPackage> recievedPackages;

    private Map<Integer, UDPPackage> sendPackages;

    private Map<Integer, Long> rttQueue;

    private List<Long> rttTimes = new ArrayList<Long>();

    public UDPConnection(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
        recievedPackages = new HashMap<Integer, UDPPackage>();
        sendPackages = new HashMap<Integer, UDPPackage>();
        rttQueue = new HashMap<Integer, Long>();
    }

    public void addSendPackage(UDPPackage udpPackage) {
        if (localSequenceNumber != udpPackage.getSequenceNumber()) {
            throw new RuntimeException("Error in adding Package. Local Sequence Number set wrong");
        }
        sendPackages.put(localSequenceNumber, udpPackage);
        rttQueue.put(localSequenceNumber, System.nanoTime());
        localSequenceNumber++;
    }

    public void addRecievedPackage(UDPPackage udpPackage) {
        int sequenceNumber = udpPackage.getSequenceNumber();
        if (sequenceNumber > remoteSequenceNumber) {
            remoteSequenceNumber = sequenceNumber;
        }
        recievedPackages.put(sequenceNumber, udpPackage);
        int ack = udpPackage.getAck();
        if (sendPackages.containsKey(ack)) {
            sendPackages.get(ack).acknowledegePackage();
            addRttValueToMean(System.nanoTime() - rttQueue.get(ack));
        }
        short ackBitField = udpPackage.getAckBitField();
        processAckBitField(ackBitField);

    }

    public int getLocalSequenceNumber() {
        return localSequenceNumber;
    }

    public int getRemoteSequenceNumber() {
        return remoteSequenceNumber;
    }

    public short getAckBitField() {
        short returnShort = 0;
        for (int i = 0; i < Short.SIZE; i++) {
            if (recievedPackages.containsKey(localSequenceNumber - Short.SIZE + i - 1)) {
                returnShort |= (1 << i);
            }
        }
        return returnShort;
    }

    private void processAckBitField(short ackBitField) {
        for (int i = 0; i < Short.SIZE; i++) {
            int currentSequenceNumber = localSequenceNumber - Short.SIZE + i - 1;
            if (currentSequenceNumber < 0) {
                continue;
            }
            if (sendPackages.containsKey(currentSequenceNumber)
                    && !sendPackages.get(currentSequenceNumber).isAcknowledeged() && ((ackBitField >> i) & 1) > 0) {
                sendPackages.get(currentSequenceNumber).acknowledegePackage();
                addRttValueToMean(System.nanoTime() - rttQueue.get(currentSequenceNumber));
            }
        }

    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    private void addRttValueToMean(long value) {
        rttTimes.add(value);
        if (rttTimes.size() > 100) {
            rttTimes.remove(0);
        }
    }

    public double getCurrentRTT() {
        if (rttTimes.size() > 50) {
            double value = rttTimes.get(0);
            for (int i = 1; i < rttTimes.size(); i++) {
                value = (value + rttTimes.get(i)) / 2;
            }
            return value / 1000000;
            // return rttTimes.stream().mapToLong(val ->
            // val).average().getAsDouble() / 1000000;
        } else {
            return -1;
        }

    }

    public String getStats() {
        String message = "### Network Status ###";

        message += "Packages Send: " + sendPackages.size() + "\n";
        message += "Local Sequence Number: " + localSequenceNumber + "\n";
        message += "Packages Recived: " + recievedPackages.size() + "\n";
        message += "Remote Sequence Number: " + remoteSequenceNumber + "\n";
        message += "RTT: " + this.getCurrentRTT() + "\n";
        short ackBitField = this.getAckBitField();
        byte[] ackBitArray = ByteBuffer.allocate(2).putShort(ackBitField).array();
        message += "AckBitField: ";
        for (int i = 0; i < ackBitArray.length; i++) {
            for (int j = 0; j < 8; j++) {
                message += (i * 8 + j) + ":[" + ((ackBitArray[i] >> j) & 1) + "] ";
            }
        }
        message += "\n";

        message += "\n";
        return message;
    }

}
