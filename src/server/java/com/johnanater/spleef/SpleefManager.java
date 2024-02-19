package com.johnanater.spleef;

import com.fox2code.foxloader.network.ChatColors;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import lombok.var;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.game.entity.player.EntityPlayerMP;
import net.minecraft.src.game.item.ItemStack;

import java.util.*;

public class SpleefManager
{
    public static SpleefManager instance;
    public GameThread gameThread;

    public ConfigUtils cfg;

    public SpleefConfig spleefConfig;
    public ArenaConfig arenaConfig;

    public ArrayList<EntityPlayerMP> spleefers;
    public ArrayList<EntityPlayerMP> lobbiers;

    public HashMap<EntityPlayerMP, Coordinate> lastPosition;

    public ArrayList<Coordinate> availableSpawns;

    public Boolean isGameRunning = false;

    public Random random = new Random();

    public SpleefManager()
    {
        instance = this;
        cfg = new ConfigUtils(this);

        lastPosition = new HashMap<>();
        lobbiers = new ArrayList<>();
        spleefers = new ArrayList<>();
    }

    public void startGame()
    {
        fillArena();
        spleefers = (ArrayList<EntityPlayerMP>) lobbiers.clone();
        lobbiers.clear();
        availableSpawns = (ArrayList<Coordinate>) arenaConfig.spawns.clone();

        for (var spleefer : spleefers)
        {
            if (!spleefConfig.teleportToLobby)
                lastPosition.put(spleefer, new Coordinate(spleefer.posX, spleefer.posY, spleefer.posZ, spleefer.rotationYaw, spleefer.rotationPitch));
            var s = availableSpawns.get(random.nextInt(availableSpawns.size()));
            availableSpawns.remove(s);
            spleefer.playerNetServerHandler.teleportTo(s.x, s.y, s.z, s.yaw, s.pitch);
            spleefer.displayChatMessage(ChatColors.AQUA + "Game started, let's go!");
        }

        gameThread = new GameThread(this);
        gameThread.start();
        isGameRunning = true;
    }

    public void endGame()
    {
        for (var spleefer : spleefers)
        {
            if (spleefConfig.teleportToLobby)
                teleportToLobby(spleefer);
            else
                teleportToLastPos(spleefer);
        }

        spleefers.clear();
        lobbiers.clear();
        isGameRunning = false;
        fillArena();
    }

    public void addToLobby(EntityPlayerMP entityPlayer)
    {
        if (spleefConfig.teleportToLobby)
        {
            lastPosition.put(entityPlayer, new Coordinate(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, entityPlayer.rotationYaw, entityPlayer.rotationPitch));
            teleportToLobby(entityPlayer);
        }
        else
            entityPlayer.displayChatMessage(ChatColors.GREEN + "You have been added to the lobby!");

        announceToPlayers(ChatColors.GREEN + entityPlayer.username + " has joined the lobby!");

        lobbiers.add(entityPlayer);
    }

    public void awardWinner(EntityPlayerMP winner)
    {
        announceToServer(ChatColors.AQUA + winner.username + " has won the game of spleef!", true);
        endGame();

        var item = spleefConfig.rewards[random.nextInt(spleefConfig.rewards.length -1)];
        winner.inventory.addItemStackToInventory(new ItemStack(item, 1));
        winner.inventory.onInventoryChanged();
    }

    public void eliminateSpleefer(EntityPlayerMP spleefer)
    {
        if (spleefConfig.teleportToLobby)
            teleportToLobby(spleefer);
        else
            teleportToLastPos(spleefer);

        announceToPlayers(ChatColors.DARK_RED + spleefer.username + " has been eliminated!");
        spleefers.remove(spleefer);
    }

    public void teleportToLobby(EntityPlayerMP player)
    {
        player.playerNetServerHandler.teleportTo(arenaConfig.lobbySpawn.x, arenaConfig.lobbySpawn.y, arenaConfig.lobbySpawn.z, arenaConfig.lobbySpawn.yaw, arenaConfig.lobbySpawn.pitch);
        player.displayChatMessage(ChatColors.GREEN + "You have been teleported to the lobby!");
    }

    public void teleportToLastPos(EntityPlayerMP player)
    {
        var lastPos = lastPosition.get(player);
        player.playerNetServerHandler.teleportTo(lastPos.x, lastPos.y, lastPos.z, lastPos.yaw, lastPos.pitch);
    }

    public boolean isInLobby(EntityPlayerMP player)
    {
        return lobbiers.stream().anyMatch(l -> l.username.equalsIgnoreCase(player.username));
    }

    public boolean isInGame(EntityPlayerMP player)
    {
        if (spleefers == null)
            return false;

        return spleefers.stream().anyMatch(s -> s.username.equalsIgnoreCase(player.username));
    }

    public boolean isBlockInArena(int x, int y, int z)
    {
        boolean xBetween = (x >= Math.min(arenaConfig.pos1.x, arenaConfig.pos2.x)) && (x <= Math.max(arenaConfig.pos1.x, arenaConfig.pos2.x));
        boolean yBetween = (y >= Math.min(arenaConfig.pos1.y, arenaConfig.pos2.y)) && (y <= Math.max(arenaConfig.pos1.y, arenaConfig.pos2.y));
        boolean zBetween = (z >= Math.min(arenaConfig.pos1.z, arenaConfig.pos2.z)) && (z <= Math.max(arenaConfig.pos1.z, arenaConfig.pos2.z));

        return xBetween && yBetween && zBetween;
    }

    public void announceToServer(String msg, Boolean log)
    {
        for (var player : MinecraftServer.getInstance().configManager.playerEntities)
        {
            player.displayChatMessage(msg);
        }
        if (log)
            System.out.println(msg);
    }

    public void announceToPlayers(String msg)
    {
        for (var spleefer : spleefers)
        {
            spleefer.displayChatMessage(msg);
        }

        for (var lobbier : lobbiers)
        {
            lobbier.displayChatMessage(msg);
        }
    }

    public void fillArena()
    {
        for (int x = (int) arenaConfig.pos1.x; x <= arenaConfig.pos2.x; x++)
        {
            for (int y = (int) arenaConfig.pos1.y; y <= arenaConfig.pos2.y; y++)
            {
                for (int z = (int) arenaConfig.pos1.z; z <= arenaConfig.pos2.z; z++)
                {
                    MinecraftServer.getInstance().worldMngr[0].setBlockWithNotify(x, y, z, spleefConfig.blockId);
                }
            }
        }
    }

    // todo add world check?
    // for snow blocks
    public boolean onPlayerStartBreakBlock(NetworkPlayer networkPlayer, RegisteredItemStack itemStack,
                                           int x, int y, int z, int facing, boolean cancelled)
    {
        var ingame = isInGame((EntityPlayerMP) networkPlayer);

        if (!ingame)
            return false;

        var world = ((EntityPlayerMP) networkPlayer).worldObj;

        var blockId = world.getBlockId(x, y, z);

        if (blockId == spleefConfig.blockId && isBlockInArena(x, y, z))
        {
            world.setBlockWithNotify(x, y, z, 0);
            return true;
        }
        return false;
    }

    public boolean onPlayerUseItem(NetworkPlayer networkPlayer, RegisteredItemStack itemStack, boolean cancelled)
    {
        return isInGame((EntityPlayerMP) networkPlayer);
    }

    public boolean onPlayerUseItemOnBlock(NetworkPlayer networkPlayer, RegisteredItemStack itemStack,
                           int x, int y, int z, int facing,
                           float xOffset, float yOffset, float zOffset, boolean cancelled)
    {
        return isInGame((EntityPlayerMP) networkPlayer);
    }
}
