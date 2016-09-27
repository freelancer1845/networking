package de.riedelgames.core.networking.api.constants;

/**
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
    public static int PACKAGE_SIZE = 16;

    /** Intial Package ID for first contact. */
    public static int INTIAL_PACKAGE_PROTOCOL_ID = 15723895;

    /** Default Tickrate. */
    public static int DEFAULT_TICKRATE = 66;

    /** Maximum size for queue. */
    public static int MAXIMUM_QUEUE_SIZE = 500;

    /** Is Network Output Verbose. */
    public static final boolean VERBOSE = true;

    /** UDP Groupname for server detection. */
    public static final String GROUPNAME = "229.127.12.17";

    private NetworkingConstants() {}

}
