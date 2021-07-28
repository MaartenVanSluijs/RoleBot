package slashCommands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditMessage extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        Boolean ephemeral = null;
        String type = null;
        String content = null;
        Boolean delete = false;

        String messageId = null;
        String channelId = null;
        String roleDescription = null;
        String roleEmoji = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("name")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            } else if (option.getName().equals("type")) {
                type = option.getAsString();
            } else if (option.getName().equals("content")) {
                content = option.getAsString();
            } else if (option.getName().equals("delete")) {
                delete = option.getAsBoolean();
            }
        }

        //Check if message exists
        String messageQuery = "SELECT * FROM messages WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {
                size++;
                messageId = messageRs.getString("id");

                //Set data to current data if none is given
                if (type == null) {
                    type = messageRs.getString("type");
                }
                if (content == null) {
                    content = messageRs.getString("content");
                }
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Could not find message with name: *%s*", messageName)).setEphemeral(ephemeral).queue();
            return;
        }

        //Get information on role if message type is getting set to single
        //Also check if message has 0 or 1 roles, cannot be manually set to single if it has more than 1
        if (type != null && type.equals("single")) {
            String typeQuery = "SELECT * FROM roles WHERE messageName = ?";
            int amount = 0;

            try {

                PreparedStatement typePstmt = conn.prepareStatement(typeQuery);
                typePstmt.setString(1, messageName);
                ResultSet typeRs = typePstmt.executeQuery();

                while (typeRs.next()) {
                    roleDescription = typeRs.getString("description");
                    roleEmoji = typeRs.getString("emoji");
                    amount++;
                }
                typeRs.close();

            } catch(SQLException e) {
                e.printStackTrace();
            }

            if (amount > 1) {
                event.reply(String.format("Message *%s* has too many corresponding roles to be converted to single, please reduce the amount of roles to a max. of 1", messageName)).setEphemeral(ephemeral).queue();
                return;
            }
        }

        //Get channel if message has already been sent
        if (messageId != null) {

            String channelQuery = "SELECT * FROM channels WHERE message = ?";
            channelId = null;

            try {

                PreparedStatement channelPstmt = conn.prepareStatement(channelQuery);
                channelPstmt.setString(1, messageName);
                ResultSet channelRs = channelPstmt.executeQuery();

                while (channelRs.next()) {
                    channelId = channelRs.getString("channelId");
                }
                channelRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Handles updating if message is not getting deleted from database
        if (!delete) {

            if (type != null && !type.equals("single") && !type.equals("multi")) {
                event.reply(String.format("*%s* is not a valid message type, message types are either *single* or *multi*", type)).setEphemeral(ephemeral).queue();
                return;
            }

            //Updates message if it has already been sent
            if (messageId != null) {

                Emoji emoji = roleEmoji != null && roleEmoji.charAt(0) == '<' ? Emoji.fromMarkdown(roleEmoji) : roleEmoji != null ? Emoji.fromUnicode(roleEmoji) : null;

                String finalContent = content;
                String finalType = type;
                String finalRoleDescription = roleDescription;
                event.getGuild().getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {

                    message.editMessage(finalContent).setActionRow(
                            Button.primary(finalType, finalRoleDescription != null ? finalRoleDescription : "Click me to toggle roles!")
                            .withEmoji(emoji)
                    ).queue();

                }, failure -> {});

            }

            //Updates database if it has already been sent
            String updateQuery = "UPDATE messages SET type = ?, content = ? WHERE name = ?";

            try {

                PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
                updatePstmt.setString(1, type);
                updatePstmt.setString(2, content);
                updatePstmt.setString(3, messageName);
                updatePstmt.executeUpdate();

                event.reply(String.format("Successfully updated message *%s*", messageName)).setEphemeral(ephemeral).queue();

            } catch (SQLException e) {
                e.printStackTrace();
            }

        //Message to be deleted from database
        } else {

            //Delete message from discord if sent and update channels table accordingly
            if (messageId != null) {

                event.getGuild().getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {
                    message.delete().queue();
                }, failure -> {});

                String removeQuery = "DELETE FROM channels WHERE message = ?";

                try {

                    PreparedStatement channelPstmt = conn.prepareStatement(removeQuery);
                    channelPstmt.setString(1, messageName);
                    channelPstmt.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            //Remove message from database
            String deleteQuery = "DELETE FROM messages WHERE name = ?";

            try {

                PreparedStatement channelPstmt = conn.prepareStatement(deleteQuery);
                channelPstmt.setString(1, messageName);
                channelPstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            event.reply(String.format("Successfully deleted message: *%s*", messageName)).setEphemeral(ephemeral).queue();
        }
    }
}
