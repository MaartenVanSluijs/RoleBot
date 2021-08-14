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
        String url = "jdbc:mysql://172.18.0.1:3306/s241_roles";
        String user = conf.getUser();
        String password = conf.getPassword();
        try {
            this.conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract void run(SlashCommandEvent event);
}
