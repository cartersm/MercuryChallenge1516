package edu.rosehulman.mercury.mercurycontroller;

public class MotorCommand {
    private int distance;
    private int angle;
    private long timestamp;

    public MotorCommand() {

    }

    public MotorCommand(int distance, int angle, long timestamp) {
        this.distance = distance;
        this.angle = angle;
        this.timestamp = timestamp;
    }

    public int getDistance() {
        return distance;
    }

    public int getAngle() {
        return angle;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
