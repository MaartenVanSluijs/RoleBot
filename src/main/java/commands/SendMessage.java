package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.*;

public class SendMessage extends Command {

    Connection conn;

    public SendMessage() {

        this.name = "sendMessage";
        this.guildOnly = true;
        this.arguments = "<messageName> <channelId>";

        //Sets up connection with database
        String url = "jdbc:sqlite:C:/Users/20182667/Documents/Programming/Discord Bots/TestBot/DummyBase.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void execute(CommandEvent event) {

        String[] pairs = event.getArgs().split("}\\s*");
        String messageName = null;
        String channelId = null;
        String messageId = null;
        String roleDescription = null;
        String roleEmoji = null;
        Emoji emoji = null;

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("message") || words[0].equals("msg")) {
                messageName = words[1];
            } else if (words[0].equals("channel") || words[0].equals("id")) {
                channelId = words[1];
            }
        }

        if (event.getArgs().length() != 0 && messageName == null && channelId == null) {
            event.reply("Please use the following syntax: name/message/msg{<messageName>} channel/id{<channelId>}");
            return;
        }

        if (messageName == null) {
            event.reply("No message name found, please give at least a message name");
            return;
        }

        if (channelId == null) {
            event.reply("No channel id found, please give at least a channel id");
            return;
        }

        if (!event.getGuild().getChannels().contains(event.getGuild().getTextChannelById(channelId))) {
            event.reply(String.format("Channel with id *%s* does not exist", channelId));
            return;
        }

        String messageQuery = "SELECT * FROM messages WHERE name = ?";
        int size = 0;

        String type = null;
        String content = null;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {

                type = messageRs.getString("type");
                content = messageRs.getString("content");
                messageId = messageRs.getString("id");
                size++;
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Found no message with name: *%s*, try !getMessages for an overview", messageName));
            return;
        }

        if (messageId != null) {

            for (TextChannel channel : event.getGuild().getTextChannels()) {

                channel.deleteMessageById(messageId).queue(null, failure -> {});
            }
        }

        if (type.equals("single")) {

            String typeQuery = "SELECT * FROM roles WHERE messageName = ?";

            try {

                PreparedStatement typePstmt = conn.prepareStatement(typeQuery);
                typePstmt.setString(1, messageName);
                ResultSet typeRs = typePstmt.executeQuery();

                while (typeRs.next()) {
                    roleDescription = typeRs.getString("description");
                    roleEmoji = typeRs.getString("emoji");
                    emoji = roleEmoji.charAt(0) == '<' ? Emoji.fromMarkdown(roleEmoji) : Emoji.fromUnicode(roleEmoji);
                }
                typeRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String finalMessageName = messageName;
        event.getGuild().getTextChannelById(channelId).sendMessage(content).setActionRow(
                Button.primary(type, type.equals("single") ? roleDescription : "Click me to toggle roles!")
                        .withEmoji(type.equals("single") ? emoji : null)).queue(message -> {

                    String idQuery = "UPDATE messages SET id = ? WHERE name = ?";

                    try {

                        PreparedStatement idPstmt = conn.prepareStatement(idQuery);
                        idPstmt.setString(1, message.getId());
                        idPstmt.setString(2, finalMessageName);
                        idPstmt.executeUpdate();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
        });
    }
}
