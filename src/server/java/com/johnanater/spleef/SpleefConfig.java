package com.johnanater.spleef;

public class SpleefConfig
{
    public int blockId;
    public int maxPlayers;
    public int gameLength;
    public boolean teleportToLobby;
    public int[] rewards;

    public SpleefConfig(int blockId, int maxPlayers, int gameLength, boolean teleportToLobby, int[] rewards)
    {
        this.blockId = blockId;
        this.maxPlayers = maxPlayers;
        this.gameLength = gameLength;
        this.teleportToLobby = teleportToLobby;
        this.rewards = rewards;
    }
}
