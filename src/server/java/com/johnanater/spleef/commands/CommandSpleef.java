package com.johnanater.spleef.commands;

import com.fox2code.foxloader.network.ChatColors;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.johnanater.spleef.SpleefManager;
import lombok.var;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.game.entity.player.EntityPlayerMP;

public class CommandSpleef extends CommandCompat
{
    public CommandSpleef()
    {
        super("spleef", false, false, new String[] { "s" }, false);
    }

    @Override
    public String commandSyntax()
    {
        return "/spleef <join/leave/start>";
    }

    @Override
    public void onExecute(String[] args, NetworkPlayer commandExecutor)
    {
        var player = (EntityPlayerMP) commandExecutor;
        var manager = SpleefManager.instance;

        if (args.length < 2)
        {
            commandExecutor.displayChatMessage(ChatColors.GOLD + commandSyntax());
            return;
        }

        if (args[1].equalsIgnoreCase("join"))
        {
            if (!manager.isInGame(player) && !manager.isInLobby(player))
                manager.addToLobby(player);
            else
                player.displayChatMessage(ChatColors.RED + "You are already in the game!");
        }

        if (args[1].equalsIgnoreCase("leave"))
        {
            if (manager.isInLobby(player))
            {
                manager.lobbiers.remove(player);
                manager.teleportToLastPos(player);
            }
            else if (manager.isInGame(player))
            {
                manager.spleefers.remove(player);
                manager.teleportToLastPos(player);
            }
            else
                player.displayChatMessage(ChatColors.RED + "You are not in the game!");
        }

        if (args[1].equalsIgnoreCase("start"))
        {
            if (!manager.isInLobby(player))
            {
                player.displayChatMessage(ChatColors.RED + "You are not in the lobby!");
                return;
            }

            if (manager.isGameRunning)
            {
                player.displayChatMessage(ChatColors.RED + "The game is already running!");
                return;
            }

            if (manager.lobbiers.size() >= 2)
                manager.startGame();
            else
                player.displayChatMessage(ChatColors.RED + "There needs to be at least 2 players to start the game!");
        }

        // OP Only, todo perms eventually?
        if (MinecraftServer.getInstance().configManager.isOp(player.username))
        {
            if (args[1].equalsIgnoreCase("create"))
            {
                var netPlayer = commandExecutor.getNetworkPlayerController();
                manager.cfg.createArena(netPlayer.getMinX(), netPlayer.getMinY(), netPlayer.getMinZ(),
                        netPlayer.getMaxX(), netPlayer.getMaxY(), netPlayer.getMaxZ());
                player.displayChatMessage(ChatColors.AQUA + "Created arena successfully!");
            }

            if (args[1].equalsIgnoreCase("addspawn"))
            {
                manager.cfg.addArenaSpawn(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                player.displayChatMessage(ChatColors.AQUA + "Added spawn!");
            }

            if (args[1].equalsIgnoreCase("lobbyspawn"))
            {
                manager.cfg.setLobbySpawn(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                player.displayChatMessage(ChatColors.AQUA + "Added lobby spawn!");
            }
        }
    }
}
