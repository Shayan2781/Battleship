package Datas;

import java.io.Serializable;

public class CommunicationPackage implements Serializable {
    private final PlayerStats playerStats;
    private final String logMessage;

    public CommunicationPackage(PlayerStats playerStats, String logMessage) {
        this.playerStats = playerStats;
        this.logMessage = logMessage;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public String getLogMessage() {
        return logMessage;
    }
}
