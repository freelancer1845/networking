package de.riedelgames.core.networking.api.server;

/**
 * Implementations provide basic functions for filling the package and creating
 * it.
 */
public interface UDPPackage {

    /**
     * @return the byte array representing this package.
     */
    public byte[] getNetworkPackage();

    /**
     * @return Integer protocol id.
     */
    public int getProtocolId();

    /**
     * @return Integer sequence number.
     */
    public int getSequenceNumber();

    /**
     * @return Integer ack.
     */
    public int getAck();

    /**
     * @return Short ackBitField.
     */
    public short getAckBitField();

    /**
     * @return byte[] data array.
     */
    public byte[] getDataArray();

    /**
     * @return whether the package has been acknowledged.
     */
    public boolean isAcknowledeged();

    /**
     * Acknowledges the package.
     */
    public void acknowledegePackage();

    /**
     * @return true if the package has no data attached.
     */
    public boolean isEmpty();
}
