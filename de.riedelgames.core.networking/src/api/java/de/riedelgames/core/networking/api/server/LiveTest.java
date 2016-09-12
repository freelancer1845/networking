package de.riedelgames.core.networking.api.server;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.riedelgames.core.networking.api.constants.NetworkingConstants;
import de.riedelgames.core.networking.impl.server.UDPClientImpl;
import de.riedelgames.core.networking.impl.server.UDPServerImpl;

public class LiveTest {

    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        System.out.println("Starting Server");
        GameServerWrapper gameServer = new GameServerWrapper();
        while (true) {
            Thread.yield();
        }

    }
}
