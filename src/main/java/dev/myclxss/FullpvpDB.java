package dev.myclxss;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import dev.myclxss.components.Board;
import dev.myclxss.components.Color;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FullpvpDB extends JavaPlugin implements Listener {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> asesinatosCollection;
    private MongoCollection<Document> muertesCollection;
    private MongoCollection<Document> killstreakCollection;

    @Override
    public void onEnable() {

        new API(this);
        // Conectar a MongoDB usando una URI
        String mongoURI = "mongodb+srv://myclxss:anhuar2004@cluster0.lpdiqzf.mongodb.net/";
        MongoClientURI uri = new MongoClientURI(mongoURI);
        mongoClient = new MongoClient(uri);

        database = mongoClient.getDatabase("fullpvpdb");
        asesinatosCollection = database.getCollection("asesinatos");
        muertesCollection = database.getCollection("muertes");
        killstreakCollection = database.getCollection("killstreak");

        // Registrar eventos y comando
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("stats").setExecutor(this);

        Logger logger = Logger.getLogger("org.mongodb.driver");

        logger.setLevel(Level.OFF);

        try {
            scoreboardTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Cerrar la conexión de MongoDB al deshabilitar el plugin
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        if (player != null) {
            String playerName = player.getName();

            // Buscar el documento del killer en la colección
            Document filtro2 = new Document("jugador", playerName);
            Document muerteDocumeto = muertesCollection.find(filtro2).first();

            if (muerteDocumeto == null) {
                // Si no existe, crear un nuevo documento
                muerteDocumeto = new Document("jugador", playerName).append("muertes", 1);
                muertesCollection.insertOne(muerteDocumeto);
            } else {
                int muertes = muerteDocumeto.getInteger("muertes");
                muerteDocumeto.put("muertes", muertes + 1);
                muertesCollection.replaceOne(filtro2, muerteDocumeto);
            }
        }

        if (killer != null) {
            String killerName = killer.getName();

            // Buscar el documento del killer en la colección
            Document filtro = new Document("jugador", killerName);
            Document killerDocumento = asesinatosCollection.find(filtro).first();

            if (killerDocumento == null) {
                // Si no existe, crear un nuevo documento
                killerDocumento = new Document("jugador", killerName)
                        .append("asesinatos", 1);
                asesinatosCollection.insertOne(killerDocumento);
            } else {
                // Si existe, actualizar el número de asesinatos
                int asesinatos = killerDocumento.getInteger("asesinatos");
                killerDocumento.put("asesinatos", asesinatos + 1);
                asesinatosCollection.replaceOne(filtro, killerDocumento);

                // Actualiza la killstreak del jugador asesino
                int currentKillStreak = getKillStreak(killer) + 1;
                updateKillStreak(killer, currentKillStreak);

                // Otorga recompensas según la killstreak
                if (currentKillStreak == 5) {
                    killer.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
                } else if (currentKillStreak == 10) {
                    killer.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                } else {
                    // Restablece la killstreak del jugador muerto a 0
                    updateKillStreak(player, 0);
                }
            }
        }
    }

    public void createScoreboard(Player player) {
        Board helper = Board.createScore(player);
        String sbtitle = API.getInstance().getScoreboard().getString("SCOREBOARD.TITLE");

        helper.setTitle(PlaceholderAPI.setPlaceholders(player, sbtitle));

        List<String> list = API.getInstance().getScoreboard().getStringList("SCOREBOARD.LINES");

        // Obtener el número de asesinatos y muertes del jugador
        int asesinatos = getAsesinatos(player.getName());
        int muertes = getMuertes(player.getName());
        int currentKillStreak = getKillStreak(player.getPlayer());

        int index = 15;

        for (int i = 0; i < list.size(); i++) {
            String lineText = list.get(i);
            lineText = PlaceholderAPI.setPlaceholders(player, lineText);

            // Puedes usar placeholders personalizados para asesinatos y muertes
            lineText = lineText.replace("%asesinatos%", String.valueOf(asesinatos));
            lineText = lineText.replace("%muertes%", String.valueOf(muertes));
            lineText = lineText.replace("%killstreak%", String.valueOf(currentKillStreak));

            helper.setSlot(index, lineText);
            index--;
        }
    }

    public void updateScoreboard(Player player) {
        if (Board.hasScore(player)) {
            Board helper = Board.getByPlayer(player);
            String sbtitle = API.getInstance().getScoreboard().getString("SCOREBOARD.TITLE");
            helper.setTitle(PlaceholderAPI.setPlaceholders(player, sbtitle));

            List<String> list = API.getInstance().getScoreboard().getStringList("SCOREBOARD.LINES");

            // Obtener el número de asesinatos y muertes del jugador
            int asesinatos = getAsesinatos(player.getName());
            int muertes = getMuertes(player.getName());
            int currentKillStreak = getKillStreak(player.getPlayer());

            int index = 15;

            for (int i = 0; i < list.size(); i++) {
                String lineText = list.get(i);
                lineText = PlaceholderAPI.setPlaceholders(player, lineText);

                // Puedes usar placeholders personalizados para asesinatos y muertes
                lineText = lineText.replace("%asesinatos%", String.valueOf(asesinatos));
                lineText = lineText.replace("%muertes%", String.valueOf(muertes));
                lineText = lineText.replace("%killstreak%", String.valueOf(currentKillStreak));

                helper.setSlot(index, lineText);
                index--;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.createScoreboard(player);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("stats")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String playerName = player.getName();

                // Buscar el documento del jugador en la colección
                Document filtro = new Document("jugador", playerName);
                Document jugadorDocumento = asesinatosCollection.find(filtro).first();

                Document filtro2 = new Document("jugador", playerName);
                Document jugadorDocumento2 = muertesCollection.find(filtro2).first();

                // Verificar si alguno de los documentos no es nulo
                if (jugadorDocumento != null || jugadorDocumento2 != null) {
                    int asesinatos = jugadorDocumento != null ? jugadorDocumento.getInteger("asesinatos") : 0;
                    int muertes = jugadorDocumento2 != null ? jugadorDocumento2.getInteger("muertes") : 0;

                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&7&m------------------------------"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&e⁕ &8| &6&lTus Estadisticas"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&e⁕ &8| &fAsesinatos:" + " " + ChatColor.YELLOW + asesinatos));
                    player.sendMessage(Color.set("&e⁕ &8| &fMuertes:" + " " + ChatColor.YELLOW + muertes));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&7&m------------------------------"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&r"));

                } else {
                    player.sendMessage("No tienes datos registrados.");
                }
            }
        }
        return false;
    }

    // Obtener el número de asesinatos de un jugador desde MongoDB
    public int getAsesinatos(String playerName) {
        Document filtro = new Document("jugador", playerName);
        Document asesinatosDocumento = asesinatosCollection.find(filtro).first();

        if (asesinatosDocumento != null) {
            return asesinatosDocumento.getInteger("asesinatos");
        }

        return 0;
    }

    // Obtener el número de muertes de un jugador desde MongoDB
    public int getMuertes(String playerName) {
        Document filtro = new Document("jugador", playerName);
        Document muertesDocumento = muertesCollection.find(filtro).first();

        if (muertesDocumento != null) {
            return muertesDocumento.getInteger("muertes");
        }

        return 0;
    }

    public void scoreboardTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.updateScoreboard(player);

            }
        }, 0, 20);
    }

    private int getKillStreak(Player player) {
        // Consulta MongoDB para obtener la killstreak actual del jugador
        Document query = new Document("name", player.getName());
        Document result = killstreakCollection.find(query).first();

        if (result != null) {
            return result.getInteger("killStreak", 0);
        }

        return 0;
    }

    private void updateKillStreak(Player player, int killStreak) {
        // Actualiza la killstreak del jugador en MongoDB
        Document query = new Document("name", player.getName());
        Document update = new Document("$set", new Document("killStreak", killStreak));
        killstreakCollection.updateOne(query, update, new UpdateOptions().upsert(true));
    }
}