package jsemolik.dev.preppyLevels.storage;

import java.util.UUID;

public class PlayerData {
    private UUID playerId;
    private String playerName;
    private int level;
    private long xp;

    public PlayerData(UUID playerId, String playerName, int level, long xp) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.level = level;
        this.xp = xp;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public void addXp(long amount) {
        this.xp += amount;
    }
}

