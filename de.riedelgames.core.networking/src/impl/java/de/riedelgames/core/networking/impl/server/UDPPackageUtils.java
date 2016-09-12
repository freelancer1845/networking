package de.riedelgames.core.networking.impl.server;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;

/**
 * Utility class for functions concerning packages.
 * 
 * @author Jascha Riedel
 *
 */
public class UDPPackageUtils {

    public static int getProtocolId(byte[] data) {
        return ByteBuffer.wrap(data).getInt();
    }

    public static int getSequenceNumber(byte[] data) {
        return ByteBuffer.wrap(data).getInt(4);
    }

    public static int getAck(byte[] data) {
        return ByteBuffer.wrap(data).getInt(8);
    }

    public static short getAckBitField(byte[] data) {
        return ByteBuffer.wrap(data).getShort(12);
    }

    public static byte[] getData(byte[] data) {
        return Arrays.copyOfRange(data, 14, data.length);
    }

    public static boolean isGamePackage(byte[] data) {
        return getProtocolId(data) == NetworkingConstants.PROTOCOL_ID;
    }

    public static boolean isEmpty(byte[] data) {
        byte[] rawData = getData(data);
        boolean isEmpty = true;
        for (int i = 0; i < rawData.length; i++) {
            isEmpty |= rawData[i] == 0;
        }
        if (!isEmpty) {
            System.out.println("Not Empty Package");
        }
        return isEmpty;
    }

    private UDPPackageUtils() {
    }
}
