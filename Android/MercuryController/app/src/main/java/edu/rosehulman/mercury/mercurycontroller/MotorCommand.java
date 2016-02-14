package edu.rosehulman.mercury.mercurycontroller;

public class MotorCommand {
    private int distance;
    private int angle;
    private boolean serpentine;
    private long timestamp;

    public MotorCommand() {

    }

    public MotorCommand(int distance, int angle, boolean serpentine, long timestamp) {
        this.distance = distance;
        this.angle = angle;
        this.serpentine = serpentine;
        this.timestamp = timestamp;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isSerpentine() {
        return serpentine;
    }

    public int getAngle() {
        return angle;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
