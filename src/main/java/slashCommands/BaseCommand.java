package slashCommands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BaseCommand extends ListenerAdapter {

    ConfigLoader cl = new ConfigLoader();
    Config conf = cl.loadConfig();

    Connection conn;

    public BaseCommand() {
        //Sets up connection with database
        String url = "jdbc:sqlite:" + conf.getSqliteDatabase();
        try {
            this.conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract void run(SlashCommandEvent event);
}
