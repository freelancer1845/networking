package de.riedelgames.core.networking.impl.server;

import java.nio.ByteBuffer;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.api.server.UDPPackage;

public class DefaultPackage implements UDPPackage {

    private final int protcolId;

    private final int sequenceNumber;

    private final int ack;

    private final short ackBitField;

    private final byte[] data;

    private boolean isAcknowledeged = false;

    /**
     * Use this constructor if you have recieved a package that you want to
     * wrap.
     * 
     * @param fullPackageArray
     *            the complete Package with protcol id, sequenceNumber, ...
     */
    public DefaultPackage(byte[] fullPackageArray) {
        this.protcolId = UDPPackageUtils.getProtocolId(fullPackageArray);
        this.sequenceNumber = UDPPackageUtils.getSequenceNumber(fullPackageArray);
        this.ack = UDPPackageUtils.getAck(fullPackageArray);
        this.ackBitField = UDPPackageUtils.getAckBitField(fullPackageArray);
        this.data = UDPPackageUtils.getData(fullPackageArray);
    }

    /**
     * Use this constructor if you want to build a package to be send away.
     * 
     * @param connection
     *            package will be send on.
     * @param data
     *            of the package.
     */
    public DefaultPackage(UDPConnection connection, byte[] data) {
        this.protcolId = NetworkingConstants.PROTOCOL_ID;
        this.sequenceNumber = connection.getLocalSequenceNumber();
        this.ack = connection.getRemoteSequenceNumber();
        this.ackBitField = connection.getAckBitField();
        if (data == null) {
            this.data = new byte[0];
        } else {
            this.data = data;
        }

    }

    @Override
    public byte[] getNetworkPackage() {
        ByteBuffer wrapper = ByteBuffer.allocate(data.length + 4 + 4 + 4 + 2);
        wrapper.putInt(protcolId);
        wrapper.putInt(sequenceNumber);
        wrapper.putInt(ack);
        wrapper.putShort(ackBitField);
        wrapper.put(data);
        return wrapper.array();
    }

    @Override
    public boolean isAcknowledeged() {
        return isAcknowledeged;
    }

    @Override
    public void acknowledegePackage() {
        isAcknowledeged = true;
    }

    @Override
    public int getProtocolId() {
        return this.protcolId;
    }

    @Override
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    @Override
    public int getAck() {
        return this.ack;
    }

    @Override
    public short getAckBitField() {
        return this.ackBitField;
    }

    @Override
    public byte[] getDataArray() {
        return this.data;
    }

    @Override
    public boolean isEmpty() {
        boolean returnValue = false;
        for (int i = 0; i < data.length; i++) {
            returnValue |= data[i] != 0;
            if (returnValue) {
                break;
            }
        }
        return !returnValue;
    }

}
