package de.riedelgames.core.networking.api.server;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Structure containing a Data Set for one Client.
 * 
 * @author Jascha Riedel
 *
 */
public class NetworkDataWrapper {

    /** List containing the KeyEvents in the correct Order. */
    private List<NetworkKeyEvent> keyEventList = new ArrayList<NetworkKeyEvent>();

    /** All other Data not sorted. */
    private List<byte[]> unsortedData = new ArrayList<byte[]>();

    public void addKeyEvent(NetworkKeyEvent event) {
        this.keyEventList.add(event);
    }

    public List<NetworkKeyEvent> getKeyEventList() {
        return keyEventList;
    }

    public List<byte[]> getUnsortedData() {
        return unsortedData;
    }

    public void addUnsortedData(byte[] unsortedData) {
        this.unsortedData.add(unsortedData);
    }

}
