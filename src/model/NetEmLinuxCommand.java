package model;

import java.math.BigDecimal;

public class NetEmLinuxCommand extends LinuxCommand {
    private Integer delayValue;
    private Double packetLossValue;
    private Double packetDuplicationValue;
    private Double packetCorruptValue;
    private String networkInterfaceName;

    public NetEmLinuxCommand(Integer delayValue, Double packetLossValue, Double packetCorruptValue, String networkInterfaceName) {
        this.delayValue = delayValue;
        this.packetLossValue = packetLossValue;
        this.packetCorruptValue = packetCorruptValue;
        this.networkInterfaceName = networkInterfaceName;
    }

    public String getRunningCommand(String password) {
        String initialCommand = "echo " + password + "| sudo -S tc qdisc add dev " + this.networkInterfaceName + " root netem";
        String appendedParam = "";
        if (delayValue != null) {
            appendedParam += " delay " + delayValue + "ms";
        }
        if (packetLossValue != null) {
            appendedParam += " loss " + BigDecimal.valueOf(packetLossValue).setScale(2) + "%";
        }
        if (packetCorruptValue != null) {
            appendedParam += " corrupt " + BigDecimal.valueOf(packetCorruptValue).setScale(2) + "%";
        }
        this.setCommand(initialCommand + appendedParam);
        return this.getCommand();
    }

    public String getClearCommand(String password) {
        return "echo " + password + "| sudo -S tc qdisc delete dev " + this.networkInterfaceName + " root";
    }

}
