package dev.myclxss.components;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class Color {

    public static String set(String Color) {
        return ChatColor.translateAlternateColorCodes('&', Color);
    }

    public static List<String> set(List<String> Color) {

        if (Color != null) { 
            List<String> list = new ArrayList<>();

            for (String line : Color) {
                list.add(set(line));
            }
    
            return list;
        }

        return new ArrayList<>();
    }
}

