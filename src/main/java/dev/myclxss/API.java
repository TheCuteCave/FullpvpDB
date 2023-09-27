package dev.myclxss;

import dev.myclxss.components.Files;

public class API {

    public static final String SB = null;
    private static API instance;
    private final FullpvpDB main;

    private Files scoreboard;

    public API(final FullpvpDB plugin) {

        instance = this;
        main = plugin;

        scoreboard = new Files(plugin, "scoreboard");
    }
    public FullpvpDB getMain() {
        return main;
    }

    public static API getInstance() {
        return instance;
    }
    public Files getScoreboard(){
        return scoreboard;
    }
    
}
