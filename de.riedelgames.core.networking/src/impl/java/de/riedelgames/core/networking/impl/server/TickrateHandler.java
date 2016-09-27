package de.riedelgames.core.networking.impl.server;

/**
 * This class monitors the tickrate of the connection.
 * 
 * @author Jascha Riedel
 *
 */
public class TickrateHandler {

    /** The time that will be waited at least between every check. */
    private static final long TIME_BETWEEN_CHECKS = 500;

    private enum State {
        OPTIMAL, GOOD, BAD, VERY_BAD;
    }

    private State state = State.OPTIMAL;

    private final UdpConnection connection;

    private final PackageSender packageSender;

    private final int desiredTickrate;

    private boolean stateSwitch = true;

    private long timeOfStateSwitch;



    public TickrateHandler(UdpConnection connection, PackageSender packageSender) {
        this.connection = connection;
        this.packageSender = packageSender;
        this.desiredTickrate = packageSender.getTickrate();
    }

    public void update() {
        if (connection.getCurrentRtt() == -1) {
            // The current RTT is only claculated after the first 50 packages
            return;
        }
        switch (state) {
            case OPTIMAL:
                if (stateSwitch) {
                    timeOfStateSwitch = System.currentTimeMillis();
                    packageSender.setTickrate(desiredTickrate);
                    stateSwitch = false;
                }
                if (System.currentTimeMillis() - timeOfStateSwitch > TIME_BETWEEN_CHECKS) {
                    if (connection.getCurrentRtt() > 100) {
                        state = State.GOOD;
                        stateSwitch = true;
                    } else {
                        timeOfStateSwitch = System.currentTimeMillis();
                    }
                }

                break;
            case GOOD:
                if (stateSwitch) {
                    timeOfStateSwitch = System.currentTimeMillis();
                    packageSender.setTickrate((int) (desiredTickrate / 1.5));
                    stateSwitch = false;
                }
                if (System.currentTimeMillis() - timeOfStateSwitch > TIME_BETWEEN_CHECKS) {
                    if (connection.getCurrentRtt() > 100) {
                        stateSwitch = true;
                        state = State.BAD;
                    } else if (connection.getCurrentRtt() < 50) {
                        stateSwitch = true;
                        state = State.OPTIMAL;
                    } else {
                        timeOfStateSwitch = System.currentTimeMillis();
                    }

                }
                break;
            case BAD:
                if (stateSwitch) {
                    timeOfStateSwitch = System.currentTimeMillis();
                    stateSwitch = false;
                    packageSender.setTickrate((int) (desiredTickrate / 2.0));
                }
                if (System.currentTimeMillis() - timeOfStateSwitch > TIME_BETWEEN_CHECKS) {
                    if (connection.getCurrentRtt() > 200) {
                        stateSwitch = true;
                        state = State.VERY_BAD;
                    } else if (connection.getCurrentRtt() < 100) {
                        stateSwitch = true;
                        state = State.GOOD;
                    } else {
                        timeOfStateSwitch = System.currentTimeMillis();
                    }

                }
                break;
            case VERY_BAD:
                if (stateSwitch) {
                    timeOfStateSwitch = System.currentTimeMillis();
                    stateSwitch = false;
                    packageSender.setTickrate((int) (desiredTickrate / 3.0));
                }
                if (System.currentTimeMillis() - timeOfStateSwitch > TIME_BETWEEN_CHECKS) {
                    if (connection.getCurrentRtt() < 200) {
                        stateSwitch = true;
                        state = State.BAD;
                    } else {
                        timeOfStateSwitch = System.currentTimeMillis();
                    }

                }
                break;
            default:
                break;
        }



    }



}
