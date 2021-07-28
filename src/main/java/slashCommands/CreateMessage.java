package slashCommands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateMessage extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        Boolean ephemeral = null;
        String type = null;
        String content = null;

        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("name")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            } else if (option.getName().equals("type")) {
                type = option.getAsString();
            } else if (option.getName().equals("content")) {
                content = option.getAsString();
            }
        }

        int size = 0;
        String nameQuery = "SELECT name FROM messages WHERE name = ?";

        try {

            PreparedStatement namePstmt = conn.prepareStatement(nameQuery);
            namePstmt.setString(1, messageName);
            ResultSet nameRs = namePstmt.executeQuery();

            while (nameRs.next()) {
                size++;
            }
            nameRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            event.reply(String.format("A message already exists with name: *%s*", messageName)).setEphemeral(ephemeral).queue();
            return;
        }

        if (type != null && !type.equals("single") && !type.equals("multi")) {
            event.reply(String.format("*%s* is not a valid message type, message types are either single or multi", type)).setEphemeral(ephemeral).queue();
            return;
        }

        if (type == null) {
            type = "multi";
        }

        String updateQuery = "INSERT INTO messages (name, type, content) VALUES (?, ?, ?)";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, messageName);
            updatePstmt.setString(2, type);
            updatePstmt.setString(3, content);
            updatePstmt.executeUpdate();

            event.reply("Successfully created message!").setEphemeral(ephemeral).queue();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
