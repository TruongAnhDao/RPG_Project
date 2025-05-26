package com.mygdx.game.desktop;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.rpg.*;

public class DesktopLauncher {
    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("My RPG Game");
        config.setWindowedMode(2400, 1700);
        new Lwjgl3Application(new RPGGame(), config);
    }
}
