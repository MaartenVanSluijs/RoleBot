package slashCommands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMessage extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String messageName = null;
        MessageChannel channel = null;
        Boolean ephemeral = null;

        String type = null;
        String content = null;
        String roleDescription = null;
        String roleEmoji = null;
        Emoji emoji = null;
        String oldMessageId = null;
        String oldChannelId = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("message")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("channel")) {
                channel = option.getAsMessageChannel();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            }
        }

        //Check if message exists
        String messageQuery = "SELECT * FROM messages WHERE messages.name = ?";
        int size = 0;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {
                type = messageRs.getString("type");
                content = messageRs.getString("content");
                oldMessageId = messageRs.getString("id");
                size++;
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Found no message with name: *%s*, try /get messages for an overview", messageName)).setEphemeral(ephemeral).queue();
            return;
        }

        if (content == null) {
            event.reply("Can't send a message without content").setEphemeral(ephemeral).queue();
            return;
        }

        try {
            if (channel.getType() != ChannelType.TEXT) {
                event.reply("This is not a valid text channel to send a message in").setEphemeral(ephemeral).queue();
                return;
            }
        } catch (NullPointerException e) {
            event.reply("This is not a valid text channel to send a message in").setEphemeral(ephemeral).queue();
            return;
        }

        //Deletes message if it had been sent before elsewhere
        if (oldMessageId != null) {

            String channelQuery = "SELECT * FROM channels WHERE message = ?";

            try {

                PreparedStatement channelPstmt = conn.prepareStatement(channelQuery);
                channelPstmt.setString(1, messageName);
                ResultSet channelRs = channelPstmt.executeQuery();

                while (channelRs.next()) {
                    oldChannelId = channelRs.getString("channelId");
                }
                channelRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (oldChannelId != null) {
                event.getGuild().getTextChannelById(oldChannelId).deleteMessageById(oldMessageId).queue();
            }

        }

        //Get role information if message type is single
        if (type.equals("single")) {

            String typeQuery = "SELECT * FROM roles WHERE messageName = ?";

            try {

                PreparedStatement typePstmt = conn.prepareStatement(typeQuery);
                typePstmt.setString(1, messageName);
                ResultSet typeRs = typePstmt.executeQuery();

                while (typeRs.next()) {
                    roleDescription = typeRs.getString("description");
                    roleEmoji = typeRs.getString("emoji");
                    emoji = roleEmoji != null && roleEmoji.charAt(0) == '<' ? Emoji.fromMarkdown(roleEmoji) : roleEmoji != null ? Emoji.fromUnicode(roleEmoji) : null;
                }
                typeRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //Send message and update tables messages and channels accordingly
        String finalMessageName = messageName;
        MessageChannel finalChannel = channel;
        String finalOldMessageId = oldMessageId;
        channel.sendMessage(content).setActionRow(
                Button.primary(type, type.equals("single") ? roleDescription : "Click me to toggle roles!")
                        .withEmoji(type.equals("single") ? emoji : null)).queue(message -> {

            String idQuery = "UPDATE messages SET id = ? WHERE name = ?";
            String channelQuery = finalOldMessageId == null ? "INSERT INTO channels VALUES(?, ?)" :
                                                                "UPDATE channels SET channelId = ? WHERE message = ?";

            try {

                PreparedStatement idPstmt = conn.prepareStatement(idQuery);
                idPstmt.setString(1, message.getId());
                idPstmt.setString(2, finalMessageName);

                idPstmt.executeUpdate();

                PreparedStatement channelPstmt = conn.prepareStatement(channelQuery);
                if (finalOldMessageId == null) {
                    channelPstmt.setString(1, finalMessageName);
                    channelPstmt.setString(2, finalChannel.getId());
                } else {
                    channelPstmt.setString(1, finalChannel.getId());
                    channelPstmt.setString(2, finalMessageName);
                }
                channelPstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        event.reply("Successfully sent message").setEphemeral(ephemeral).queue();
    }
}
