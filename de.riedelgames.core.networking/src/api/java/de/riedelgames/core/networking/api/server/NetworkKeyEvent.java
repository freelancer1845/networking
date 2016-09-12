package de.riedelgames.core.networking.api.server;

public class NetworkKeyEvent {

    /** Key down Identifier. */
    public static final byte KEY_EVENT_DOWN = 0x00;

    /** Key Up Identifier. */
    public static final byte KEY_EVENT_UP = 0x01;

    private final byte keyEventType;

    private final byte keyEventCode;

    public NetworkKeyEvent(byte keyEventType, byte keyEventCode) {
        this.keyEventType = keyEventType;
        this.keyEventCode = keyEventCode;
    }

    public byte getKeyEventType() {
        return keyEventType;
    }

    public byte getKeyEventCode() {
        return keyEventCode;
    }

}
