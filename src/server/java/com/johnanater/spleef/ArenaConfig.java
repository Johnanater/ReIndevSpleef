package com.johnanater.spleef;

import java.util.ArrayList;

public class ArenaConfig
{
    public Coordinate pos1;
    public Coordinate pos2;
    public Coordinate lobbySpawn;
    public ArrayList<Coordinate> spawns;

    public ArenaConfig(Coordinate pos1, Coordinate pos2, Coordinate lobbySpawn, ArrayList<Coordinate> spawns)
    {
        this.pos1 = pos1;
        this.pos2 = pos2;

        this.lobbySpawn = lobbySpawn;

        this.spawns = spawns;
    }
}
