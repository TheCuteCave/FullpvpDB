package dev.myclxss;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import dev.myclxss.components.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FullpvpDB extends JavaPlugin implements Listener {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> asesinatosCollection;

    @Override
    public void onEnable() {
        // Conectar a MongoDB usando una URI
        String mongoURI = "mongodb+srv://myclxss:anhuar2004@cluster0.lpdiqzf.mongodb.net/";
        MongoClientURI uri = new MongoClientURI(mongoURI);
        mongoClient = new MongoClient(uri);

        database = mongoClient.getDatabase("fullpvpdb");
        asesinatosCollection = database.getCollection("asesinatos");

        // Registrar eventos y comando
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("stats").setExecutor(this);

        Logger logger = Logger.getLogger("org.mongodb.driver");

        logger.setLevel(Level.OFF);
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
            }
        }
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

                if (jugadorDocumento != null) {

                    int asesinatos = jugadorDocumento.getInteger("asesinatos");

                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&7&m------------------------------"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&e⁕ &8| &6&lTus Estadisticas"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&e⁕ &8| &fAsesinatos:" + " " + ChatColor.YELLOW + asesinatos));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&7&m------------------------------"));
                    player.sendMessage(Color.set("&r"));
                    player.sendMessage(Color.set("&r"));

                } else {

                    player.sendMessage("No tienes datos registrados.");

                }
            } else {
                sender.sendMessage("Este comando solo se puede usar como jugador.");
            }
            return true;
        }
        return false;
    }
}