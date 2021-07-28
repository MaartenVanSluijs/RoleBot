package slashCommands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteMessage extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        Boolean ephemeral = null;

        String channelId = null;
        String messageId = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("name")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            }
        }

        //Check if message has been sent yet
        String messageQuery = "SELECT * FROM channels WHERE message = ?";
        int size = 0;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {
                size++;
                channelId = messageRs.getString("channelId");
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Can't delete a message: *%s*, as it has not been sent yet", messageName)).setEphemeral(ephemeral).queue();
            return;
        }

        //Get message id from message table
        String idQuery = "SELECT * FROM messages WHERE name = ?";

        try {

            PreparedStatement idPstmt = conn.prepareStatement(idQuery);
            idPstmt.setString(1, messageName);
            ResultSet idRs = idPstmt.executeQuery();

            while (idRs.next()) {
                messageId = idRs.getString("id");
            }
            idRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Delete message from channel
        event.getGuild().getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {

            message.delete().queue();
        }, failure -> {});

        event.reply(String.format("Successfully deleted message: *%s*", messageName)).setEphemeral(ephemeral).queue();

        //Update tables messages and channels
        String updateQuery = "UPDATE messages SET id = ? WHERE name = ?";
        String channelQuery = "DELETE FROM channels WHERE message = ?";

        try {

            PreparedStatement idPstmt = conn.prepareStatement(updateQuery);
            idPstmt.setString(1, null);
            idPstmt.setString(2, messageName);
            idPstmt.executeUpdate();

            PreparedStatement channelPstmt = conn.prepareStatement(channelQuery);
            channelPstmt.setString(1, messageName);
            channelPstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}