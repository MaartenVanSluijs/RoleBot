package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.*;

public class EditMessage extends Command {

    Connection conn;

    public EditMessage() {

        this.name = "editMessage";
        this.guildOnly = true;
        this.arguments = "<messageName> [type] [content]";
        this.aliases = new String[]{"changeMessage", "updateMessage"};

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
        String type = null;
        String content = null;
        String messageId = null;
        String roleDescription = null;
        String roleEmoji = null;

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("message") || words[0].equals("msg")) {
                messageName = words[1];
            } else if (words[0].equals("type")) {
                type = words[1];
            } else if (words[0].equals("content")) {
                content = words[1];
            }
        }

        if (event.getArgs().length() != 0 && (messageName == null && type == null && content == null)) {
            event.reply("Please use the following syntax: name/message/msg{<messageName>} type{<type>} content{[content]}");
            return;
        }

        if (messageName == null) {
            event.reply("No message name found, please give at least a message name");
            return;
        }

        if (type != null && !type.equals("single") && !type.equals("multi")) {
            event.reply(String.format("*%s* is not a valid message type, message types are either *single* or *multi*", type));
            return;
        }

        if (type != null && type.equals("single")) {
            String typeQuery = "SELECT * FROM roles WHERE messageName = ?";
            int size = 0;

            try {

                PreparedStatement typePstmt = conn.prepareStatement(typeQuery);
                typePstmt.setString(1, messageName);
                ResultSet typeRs = typePstmt.executeQuery();

                while (typeRs.next()) {
                    roleDescription = typeRs.getString("description");
                    roleEmoji = typeRs.getString("emoji");
                    size++;
                }
                typeRs.close();

            } catch(SQLException e) {
                e.printStackTrace();
            }

            if (size > 1) {
                event.reply(String.format("Message *%s* has too many corresponding roles to be converted to single, please reduce the amount of roles to a max. of 1", messageName));
                return;
            }
        }

        String messageQuery = "SELECT * FROM messages WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {
                size++;
                messageId = messageRs.getString("id");

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
            event.reply(String.format("Could not find message with name: *%s*", messageName));
            return;
        }

        if (content != null && content.equals("null")) {
            content = null;
        }

        String updateQuery = "UPDATE messages SET type = ?, content = ? WHERE name = ?";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, type);
            updatePstmt.setString(2, content);
            updatePstmt.setString(3, messageName);
            updatePstmt.executeUpdate();

            event.reply(String.format("Successfully updated message *%s*", messageName));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (messageId != null) {

            if (type.equals("single")) {

                for (TextChannel channel : event.getGuild().getTextChannels()) {

                    String finalContent = content;
                    String finalRoleEmoji = roleEmoji;
                    String finalRoleDescription = roleDescription;
                    String finalType = type;
                    channel.retrieveMessageById(messageId).queue(message -> {

                        Emoji emoji = finalRoleEmoji.charAt(0) == '<' ? Emoji.fromMarkdown(finalRoleEmoji) : Emoji.fromUnicode(finalRoleEmoji);
                        message.editMessage(finalContent).setActionRow(Button.primary(finalType, finalRoleDescription)
                                .withEmoji(emoji)).queue();

                    }, failure -> {});
                }

            } else if (type.equals("multi")) {

                for (TextChannel channel :event.getGuild().getTextChannels()) {

                    String finalContent1 = content;
                    String finalType1 = type;
                    channel.retrieveMessageById(messageId).queue(message -> {

                        message.editMessage(finalContent1).setActionRow(Button.primary(finalType1, "Click me to toggle roles!")).queue();
                    }, failure -> {});
                }
            }
        }
    }
}
