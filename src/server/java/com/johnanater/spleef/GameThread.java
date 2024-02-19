package com.johnanater.spleef;

import com.fox2code.foxloader.network.ChatColors;
import lombok.var;

public class GameThread extends Thread
{
    private SpleefManager manager;

    public int countdown;

    public GameThread(SpleefManager manager)
    {
        this.manager = manager;

        countdown = manager.spleefConfig.gameLength;
    }

    public void run()
    {
        while (countdown > 0)
        {
            if (countdown <= 10)
            {
                manager.announceToPlayers(ChatColors.DARK_GRAY + ">" + ChatColors.AQUA + countdown + "...");
            }

            for (int i = 0; i < manager.spleefers.size(); i++)
            {
                var spleefer = manager.spleefers.get(i);
                if (spleefer.isInWater())
                    manager.eliminateSpleefer(spleefer);
            }

            // someone wins!
            if (manager.spleefers.size() == 1)
            {
                manager.awardWinner(manager.spleefers.get(0));
                return;
            }

            countdown--;
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }

        // game over!
        manager.announceToPlayers(ChatColors.RED + "Times up! Nobody wins!");
        manager.endGame();
    }
}
