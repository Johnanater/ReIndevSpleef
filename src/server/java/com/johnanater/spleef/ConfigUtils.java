package com.johnanater.spleef;

import lombok.var;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class ConfigUtils
{
    public final String modFolder = "mods/Spleef/";
    public final String spleefConfigFile = modFolder + "config.cfg";
    public final String arenaConfigFile =  modFolder + "arena.cfg";

    private final SpleefManager manager;

    public ConfigUtils(SpleefManager manager)
    {
        this.manager = manager;

        manager.spleefConfig = getSpleefConfig();
        manager.arenaConfig = tryGetArenaConfig();
    }

    public void createArena(int pos1X, int pos1Y, int pos1Z, int pos2X, int pos2Y, int pos2Z)
    {
        manager.arenaConfig = new ArenaConfig(new Coordinate(pos1X, pos1Y, pos1Z), new Coordinate(pos2X, pos2Y, pos2Z), new Coordinate(), new ArrayList<>());
        writeArenaConfig(manager.arenaConfig);
    }

    public void addArenaSpawn(double x, double y, double z, float yaw, float pitch)
    {
        manager.arenaConfig.spawns.add(new Coordinate(x, y, z, yaw, pitch));
        writeArenaConfig(manager.arenaConfig);
    }

    public void setLobbySpawn(double x, double y, double z, float yaw, float pitch)
    {
        manager.arenaConfig.lobbySpawn = new Coordinate(x, y, z, yaw, pitch);
        writeArenaConfig(manager.arenaConfig);
    }

    public SpleefConfig getSpleefConfig()
    {
        if (!new File(spleefConfigFile).exists())
        {
            writeTemplateSpleefConfig();
        }

        var properties = new Properties();
        try (var inputStream = new FileInputStream(spleefConfigFile))
        {
            properties.load(inputStream);

            var blockId = Integer.parseInt(properties.getProperty("blockId"));
            var maxPlayers = Integer.parseInt(properties.getProperty("maxPlayers"));
            var gameLength = Integer.parseInt(properties.getProperty("gameLength"));
            var teleportToLobby = Boolean.parseBoolean(properties.getProperty("teleportToLobby"));
            var rewards = Arrays.stream(properties.getProperty("rewards").split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();

            return new SpleefConfig(blockId, maxPlayers, gameLength, teleportToLobby, rewards);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to load Spleef Config!");
            ex.printStackTrace();
            return null;
        }
    }

    public void writeTemplateSpleefConfig()
    {
        Properties properties = new Properties();
        properties.setProperty("blockId", String.valueOf(80));
        properties.setProperty("maxPlayers", String.valueOf(40));
        properties.setProperty("gameLength", String.valueOf(120));
        properties.setProperty("teleportToLobby", String.valueOf(true));
        properties.setProperty("rewards", "264,266,54");

        try
        {
            Files.createDirectories(Paths.get(modFolder));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        try (FileOutputStream outputStream = new FileOutputStream(spleefConfigFile))
        {
            properties.store(outputStream, "Spleef Configuration");
            System.out.println("Spleef configuration saved successfully!");
        }
        catch (Exception ex)
        {
            System.out.println("Failed to write template spleef config " + spleefConfigFile);
            ex.printStackTrace();
        }
    }

    public ArenaConfig tryGetArenaConfig()
    {
        if (!new File(arenaConfigFile).exists())
        {
            System.out.println("Arena config does not exist yet, please create it!");
            return null;
        }

        var properties = new Properties();
        try (var inputStream = new FileInputStream(arenaConfigFile))
        {
            properties.load(inputStream);

            var pos1X = Double.parseDouble(properties.getProperty("pos1X"));
            var pos1Y = Double.parseDouble(properties.getProperty("pos1Y"));
            var pos1Z = Double.parseDouble(properties.getProperty("pos1Z"));

            var pos2X = Double.parseDouble(properties.getProperty("pos2X"));
            var pos2Y = Double.parseDouble(properties.getProperty("pos2Y"));
            var pos2Z = Double.parseDouble(properties.getProperty("pos2Z"));

            var pos1 = new Coordinate(pos1X, pos1Y, pos1Z);
            var pos2 = new Coordinate(pos2X, pos2Y, pos2Z);

            double lobbyX, lobbyY, lobbyZ;
            float lobbyYaw, lobbyPitch;

            try
            {
                lobbyX = Double.parseDouble(properties.getProperty("lobbyX"));
                lobbyY = Double.parseDouble(properties.getProperty("lobbyY"));
                lobbyZ = Double.parseDouble(properties.getProperty("lobbyZ"));
                lobbyYaw = Float.parseFloat(properties.getProperty("lobbyYaw"));
                lobbyPitch = Float.parseFloat(properties.getProperty("lobbyPitch"));
            }

            catch (Exception ex)
            {
                System.out.println("Warning: Lobby is not defined yet!");
                lobbyX = 0;
                lobbyY = 0;
                lobbyZ = 0;
                lobbyYaw = 0;
                lobbyPitch = 0;
            }

            var lobbySpawn = new Coordinate(lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch);
            var spawns = new ArrayList<Coordinate>();

            for (int i = 0; properties.containsKey("spawns." + i + ".x"); i++)
            {
                double x = Double.parseDouble(properties.getProperty("spawns." + i + ".x"));
                double y = Double.parseDouble(properties.getProperty("spawns." + i + ".y"));
                double z = Double.parseDouble(properties.getProperty("spawns." + i + ".z"));
                float yaw = Float.parseFloat(properties.getProperty("spawns." + i + ".yaw"));
                float pitch = Float.parseFloat(properties.getProperty("spawns." + i + ".pitch"));
                spawns.add(new Coordinate(x, y, z, yaw, pitch));
            }

            return new ArenaConfig(pos1, pos2, lobbySpawn, spawns);
        }
        catch (Exception ex)
        {
            System.out.println("Failed to load config " + arenaConfigFile + "!");
            ex.printStackTrace();
            return null;
        }
    }

    public void writeArenaConfig(ArenaConfig arenaConfig)
    {
        Properties properties = new Properties();
        properties.setProperty("pos1X", String.valueOf(arenaConfig.pos1.x));
        properties.setProperty("pos1Y", String.valueOf(arenaConfig.pos1.y));
        properties.setProperty("pos1Z", String.valueOf(arenaConfig.pos1.z));

        properties.setProperty("pos2X", String.valueOf(arenaConfig.pos2.x));
        properties.setProperty("pos2Y", String.valueOf(arenaConfig.pos2.y));
        properties.setProperty("pos2Z", String.valueOf(arenaConfig.pos2.z));

        properties.setProperty("lobbyX", String.valueOf(arenaConfig.lobbySpawn.x));
        properties.setProperty("lobbyY", String.valueOf(arenaConfig.lobbySpawn.y));
        properties.setProperty("lobbyZ", String.valueOf(arenaConfig.lobbySpawn.z));
        properties.setProperty("lobbyYaw", String.valueOf(arenaConfig.lobbySpawn.yaw));
        properties.setProperty("lobbyPitch", String.valueOf(arenaConfig.lobbySpawn.pitch));

        for (int i = 0; i < arenaConfig.spawns.size(); i++)
        {
            var spawn = arenaConfig.spawns.get(i);
            properties.setProperty("spawns." + i + ".x", String.valueOf(spawn.x));
            properties.setProperty("spawns." + i + ".y", String.valueOf(spawn.y));
            properties.setProperty("spawns." + i + ".z", String.valueOf(spawn.z));
            properties.setProperty("spawns." + i + ".yaw", String.valueOf(spawn.yaw));
            properties.setProperty("spawns." + i + ".pitch", String.valueOf(spawn.pitch));
        }

        try (FileOutputStream outputStream = new FileOutputStream(arenaConfigFile))
        {
            properties.store(outputStream, "Arena Configuration");
            System.out.println("Arena configuration saved successfully!");
        }
        catch (Exception ex)
        {
            System.out.println("Failed to save Arena Config!");
            ex.printStackTrace();
        }
    }
}
