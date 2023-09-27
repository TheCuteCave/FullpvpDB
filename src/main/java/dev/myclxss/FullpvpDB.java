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
    private MongoCollection<Document> muertesCollection;

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
            } else {
                sender.sendMessage("Este comando solo se puede usar como jugador.");
            }
            return true;
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
}