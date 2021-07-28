package slashCommands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetRoles extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        Boolean ephemeral = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("messagename")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            }
        }

        //Retrieve information
        String query = messageName == null ? "SELECT * FROM roles" : "SELECT * FROM roles WHERE messageName = ?";
        String response = messageName == null ? "These are all the existing roles:" : String.format("Roles for message with name *%s*:", messageName);

        try {

            PreparedStatement pstmt = conn.prepareStatement(query);
            if (messageName != null) {
                pstmt.setString(1, messageName);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                response += String.format("\n- Name: *%s*, Id: *%s*, Description: *%s*, Emoji: *%s*, Gate *%s*, Message name: *%s*",
                        rs.getString("name"),
                        rs.getString("id"),
                        rs.getString("description"),
                        rs.getString("emoji"),
                        rs.getString("gated"),
                        rs.getString("messageName"));
            }
            rs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        event.reply(response).setEphemeral(ephemeral).queue();
    }
}
