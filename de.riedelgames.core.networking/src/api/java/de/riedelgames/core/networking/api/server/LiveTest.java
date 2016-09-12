package de.riedelgames.core.networking.api.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.impl.server.UDPClientImpl;
import de.riedelgames.core.networking.impl.server.UDPServerImpl;

public class LiveTest {

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        System.out.println("Starting Server");
        UDPServer server;
        server = new UDPServerImpl();
        server.start();
        // Thread.sleep(1000);
        // UDPClient client = new UDPClientImpl(InetAddress.getLocalHost(),
        // NetworkingConstants.DEFAULT_PORT);
        // client.start(true);
        while (true) {
            Thread.yield();
        }

    }
}
