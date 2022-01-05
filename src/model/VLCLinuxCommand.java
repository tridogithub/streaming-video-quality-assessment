package model;

public class VLCLinuxCommand extends LinuxCommand {
    private String serverIp;
    private Integer port;
    private String pathToFile;

    public VLCLinuxCommand(String serverIp, Integer port) {
        this.serverIp = serverIp;
        this.port = port;
    }

    public String getVLCConnectCommand() {
        this.setCommand("vlc http://" + this.serverIp + ":" + this.port);
        return this.getCommand();
    }

    public String getVLCStreamVideoCommand(String pathToFile) {
        this.setCommand("vlc -vvv " + pathToFile + " --sout "
                + "\'#standard{access=http,mux=ts,dst="
                + this.serverIp
                + ":"
                + this.port
                + "}\'"
        );
        return this.getCommand();
    }

    public String getVLCStreamTranscodeVideoCommand(String pathToFile) {
        this.setCommand("vlc -vvv " + pathToFile + " --sout "
                + "\'#transcode{vcodec=hevc}:std{access=http,mux=ts,dst="
                + this.serverIp
                + ":"
                + this.port
                + "}\'"
        );
        return this.getCommand();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
