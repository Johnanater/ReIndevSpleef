package com.johnanater.spleef;

import com.fox2code.foxloader.loader.Mod;

public class Spleef extends Mod {

    private final String version = "1.0.0";

    @Override
    public void onPostInit() {
        System.out.println("Loaded Spleef, by Johnanater, version " + version + "!");
    }
}
