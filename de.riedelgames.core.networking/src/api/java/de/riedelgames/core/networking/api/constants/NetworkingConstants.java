package de.riedelgames.core.networking.api.constants;

/**
 * 
 * Class containing all constants related to networking.
 * 
 * @author Jascha Riedel
 *
 */
public class NetworkingConstants {

    /** Id of the packages. Identifies a package as an Game Package. */
    public static int PROTOCOL_ID = 297175464;

    /** Default Port. */
    public static int DEFAULT_PORT = 4000;

    /** Package Size. */
    public static int PACKAGE_SIZE = 64;

    /** Intial Package ID for first contact. */
    public static int INTIAL_PACKAGE_PROTOCOL_ID = 15723895;

    /** Default Tickrate. */
    public static int DEFAULT_TICKRATE = 30;

    private NetworkingConstants() {
    };

}