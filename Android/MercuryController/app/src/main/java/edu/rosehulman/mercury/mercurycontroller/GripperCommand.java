package edu.rosehulman.mercury.mercurycontroller;

public class GripperCommand {
    private int angle;
    private String position;
    private long timestamp;

    public GripperCommand() {

    }

    public GripperCommand(int angle, String position, long timestamp) {
        this.angle = angle;
        this.position = position;
        this.timestamp = timestamp;
    }

    public int getAngle() {
        return angle;
    }

    public String getPosition() {
        return position;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
