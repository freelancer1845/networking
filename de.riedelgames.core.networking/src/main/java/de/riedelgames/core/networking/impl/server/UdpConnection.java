package de.riedelgames.core.networking.impl.server;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.riedelgames.core.networking.api.server.UDPPackage;

public class UdpConnection {

    private final InetAddress inetAddress;

    private final int port;

    private int localSequenceNumber = new Random().nextInt();

    private int remoteSequenceNumber;

    private Map<Integer, UDPPackage> recievedPackages;

    private Map<Integer, UDPPackage> sendPackages;

    private Map<Integer, Long> rttQueue;

    private List<Long> rttTimes = new ArrayList<Long>();

    /** Variables used for stats only. */
    private int packagesSend = 0;

    private int packagesRecieved = 0;

    private int packagesLost = 0;

    /**
     * Constructor.
     * 
     * @param inetAddress mapped to this connection.
     * @param port mapped to this connection.
     */
    public UdpConnection(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
        recievedPackages = new HashMap<Integer, UDPPackage>();
        sendPackages = new HashMap<Integer, UDPPackage>();
        rttQueue = new HashMap<Integer, Long>();
    }

    /**
     * Adds this Package to the connection.
     * 
     * @param udpPackage package to add.
     */
    public void addSendPackage(UDPPackage udpPackage) {
        if (localSequenceNumber != udpPackage.getSequenceNumber()) {
            throw new RuntimeException("Error in adding Package. Local Sequence Number set wrong");
        }
        sendPackages.put(localSequenceNumber, udpPackage);
        rttQueue.put(localSequenceNumber, System.nanoTime());
        if (sendPackages.size() > 200) {
            if (sendPackages.containsKey(localSequenceNumber - 200)) {
                if (!sendPackages.get(localSequenceNumber - 17).isAcknowledeged()) {
                    packagesLost++;
                }
            }
            sendPackages.remove(localSequenceNumber - 200);
        }
        localSequenceNumber = getIncreasedSequenceNumber(localSequenceNumber);
        packagesSend++;
    }

    /**
     * Adds this Package to the connection.
     * 
     * @param udpPackage package to add.
     */
    public void addRecievedPackage(UDPPackage udpPackage) {
        int sequenceNumber = udpPackage.getSequenceNumber();
        if (sequenceNumber > remoteSequenceNumber) {
            remoteSequenceNumber = sequenceNumber;
        }
        recievedPackages.put(sequenceNumber, udpPackage);
        int ack = udpPackage.getAck();
        if (sendPackages.containsKey(ack)) {
            sendPackages.get(ack).acknowledegePackage();
            if (rttQueue.containsKey(ack)) {
                addRttValueToMean(System.nanoTime() - rttQueue.get(ack));
            }

        }
        short ackBitField = udpPackage.getAckBitField();
        processAckBitField(ackBitField);
        if (recievedPackages.size() > 200) {
            recievedPackages.remove(remoteSequenceNumber - 200);
        }
        packagesRecieved++;
    }

    public int getLocalSequenceNumber() {
        return localSequenceNumber;
    }

    public int getRemoteSequenceNumber() {
        return remoteSequenceNumber;
    }

    /**
     * Calculates the ack bit field and returns it.
     * 
     * @return the ack bit field.
     */
    public short getAckBitField() {
        short returnShort = 0;
        int sequenceNumberToCheck = remoteSequenceNumber;
        for (int i = 0; i < Short.SIZE; i++) {
            sequenceNumberToCheck = getDecreasedSequenceNumber(sequenceNumberToCheck);
            if (recievedPackages.containsKey(sequenceNumberToCheck)) {
                returnShort |= (1 << i);
            }
        }
        return returnShort;
    }

    private void processAckBitField(short ackBitField) {
        int currentSequenceNumber = localSequenceNumber;
        for (int i = 0; i < Short.SIZE; i++) {
            currentSequenceNumber = getDecreasedSequenceNumber(currentSequenceNumber);
            if (sendPackages.containsKey(currentSequenceNumber)
                    && !sendPackages.get(currentSequenceNumber).isAcknowledeged()
                    && ((ackBitField >> i) & 1) > 0) {
                sendPackages.get(currentSequenceNumber).acknowledegePackage();

                if (rttQueue.containsKey(currentSequenceNumber)) {
                    addRttValueToMean(System.nanoTime() - rttQueue.get(currentSequenceNumber));
                }
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
        synchronized (rttTimes) {
            rttTimes.add(value);
            if (rttTimes.size() > 100) {
                rttTimes.remove(0);
            }
        }

    }

    /**
     * Calculates the current RTT.
     * 
     * @return the current RTT in ms.
     */
    public double getCurrentRtt() {
        if (rttTimes.size() > 50) {
            List<Long> rttTimesSnapshot;
            synchronized (rttTimes) {
                rttTimesSnapshot = Collections.unmodifiableList(rttTimes);
            }

            double value = rttTimesSnapshot.get(0);
            for (int i = 1; i < rttTimesSnapshot.size(); i++) {
                value = (value + rttTimesSnapshot.get(i)) / 2;
            }
            return value / 1000000;
        } else {
            return -1;
        }

    }

    public String getStats() {
        String message = "### Network Status ###";

        message += "Packages Send: " + packagesSend + "\n";
        message += "Local Sequence Number: " + localSequenceNumber + "\n";
        message += "Packages Recived: " + packagesRecieved + "\n";
        message += "Remote Sequence Number: " + remoteSequenceNumber + "\n";
        message += "RTT: " + this.getCurrentRtt() + "\n";
        short ackBitField = this.getAckBitField();
        byte[] ackBitArray = ByteBuffer.allocate(2).putShort(ackBitField).array();
        message += "AckBitField: ";
        for (int i = 0; i < ackBitArray.length; i++) {
            for (int j = 0; j < 8; j++) {
                message += (i * 8 + j) + ":[" + ((ackBitArray[i] >> j) & 1) + "] ";
            }
        }
        message += "\n";
        message += "Packages Lost: " + packagesLost;

        message += "\n";
        return message;
    }

    private int getIncreasedSequenceNumber(int sequenceNumber) {
        int returnValue;
        if (sequenceNumber == Integer.MAX_VALUE) {
            returnValue = Integer.MIN_VALUE;
        } else {
            returnValue = sequenceNumber + 1;
        }
        return returnValue;
    }

    private int getDecreasedSequenceNumber(int sequenceNumber) {
        int returnValue = 0;
        if (returnValue == Integer.MIN_VALUE) {
            returnValue = Integer.MAX_VALUE;
        } else {
            returnValue = sequenceNumber - 1;
        }
        return returnValue;
    }

}
