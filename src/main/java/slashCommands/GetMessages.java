package slashCommands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetMessages extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        Boolean content = null;
        Boolean ephemeral = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("messagename")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("content")) {
                content = option.getAsBoolean();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            }
        }

        //Retrieve information
        String query = messageName == null ? "SELECT * FROM messages" : "SELECT * FROM messages WHERE name = ?";
        String response = messageName == null ? "These are all the existing messages:" : String.format("Message with name *%s*:", messageName);

        try {

            PreparedStatement pstmt = conn.prepareStatement(query);
            if (messageName != null) {
                pstmt.setString(1, messageName);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                if (content) {
                    response += String.format("\n- Name: *%s*, Type: *%s*, Content: *%s*, Id: *%s*",
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("content"),
                            rs.getString("id"));
                } else {
                    response += String.format("\n- Name: *%s*, Type: *%s*, Id: *%s*",
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getString("id"));
                }
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        event.reply(response).setEphemeral(ephemeral).queue();
    }
}
