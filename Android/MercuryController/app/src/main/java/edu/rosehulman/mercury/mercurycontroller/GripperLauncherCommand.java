package edu.rosehulman.mercury.mercurycontroller;

public class GripperLauncherCommand {
    private boolean launch;
    private String location;
    private String position;
    private long timestamp;

    public GripperLauncherCommand() {

    }

    public GripperLauncherCommand(boolean launch, String location, String position, long timestamp) {
        this.launch = launch;
        this.location = location;
        this.position = position;
        this.timestamp = timestamp;
    }


    public boolean getLaunch() {
        return launch;
    }

    public String getLocation() {
        return location;
    }

    public String getPosition() {
        return position;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
