package com.johnanater.spleef;

import com.fox2code.foxloader.loader.ServerMod;
import com.fox2code.foxloader.network.NetworkPlayer;
import com.fox2code.foxloader.registry.CommandCompat;
import com.fox2code.foxloader.registry.RegisteredItemStack;
import com.johnanater.spleef.commands.CommandSpleef;

public class SpleefServer extends Spleef implements ServerMod
{

    @Override
    public void onInit()
    {
        CommandCompat.registerCommand(new CommandSpleef());

        new SpleefManager();
    }

    @Override
    public boolean onPlayerStartBreakBlock(NetworkPlayer networkPlayer, RegisteredItemStack itemStack,
                                           int x, int y, int z, int facing, boolean cancelled)
    {
        return SpleefManager.instance.onPlayerStartBreakBlock(networkPlayer, itemStack, x, y, z, facing, cancelled);
    }

    @Override
    public boolean onPlayerUseItem(NetworkPlayer networkPlayer, RegisteredItemStack itemStack, boolean cancelled)
    {
        return SpleefManager.instance.onPlayerUseItem(networkPlayer, itemStack, cancelled);
    }

    @Override
    public boolean onPlayerUseItemOnBlock(NetworkPlayer networkPlayer, RegisteredItemStack itemStack,
                                          int x, int y, int z, int facing,
                                          float xOffset, float yOffset, float zOffset, boolean cancelled)
    {
        return SpleefManager.instance.onPlayerUseItemOnBlock(networkPlayer, itemStack, x, y, z, facing, xOffset, yOffset, zOffset, cancelled);
    }
}
