package slashCommands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CreateRole extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String roleName = null;
        String roleId = null;
        String description = null;
        String emoji = null;
        String gated = null;
        String messageName = null;
        Boolean ephemeral = null;

        //Load in data
        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("role")) {
                roleName = option.getAsRole().getName();
                roleId = option.getAsRole().getId();
            } else if (option.getName().equals("description")) {
                description = option.getAsString();
            } else if (option.getName().equals("emoji")) {
                emoji = option.getAsString();
            } else if (option.getName().equals("gate")) {
                gated = option.getAsRole().getId();
            } else if (option.getName().equals("message")) {
                messageName = option.getAsString();
            } else if (option.getName().equals("ephemeral")) {
                ephemeral = option.getAsBoolean();
            }
        }

        //Check if role already exists
        String roleQuery = "SELECT * FROM roles WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
            rolePstmt.setString(1, roleName);
            ResultSet roleRs = rolePstmt.executeQuery();

            while (roleRs.next()) {
                size++;
            }
            roleRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            event.reply(String.format("This role *%s* already exists", roleName)).setEphemeral(ephemeral).queue();
            return;
        }

        //Create role in database
        String insertQuery = "INSERT INTO roles VALUES (?, ?, ?, ?, ?, ?)";

        try {

            PreparedStatement insertPstmt = conn.prepareStatement(insertQuery);
            insertPstmt.setString(1, roleName);
            insertPstmt.setString(2, roleId);
            insertPstmt.setString(3, description);
            insertPstmt.setString(4, emoji);
            insertPstmt.setString(5, gated);
            insertPstmt.setString(6, messageName);
            insertPstmt.executeUpdate();

            event.reply(String.format("Successfully added role *%s*", roleName)).setEphemeral(ephemeral).queue();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //If added to message on creation update message
        //Count amount of roles now bound to this message
        if (messageName != null) {

            String messageQuery = "SELECT * FROM roles, messages, channels WHERE roles.messageName = ? AND roles.messageName = messages.name AND messages.name = channels.message";
            String channelId = null;
            String messageId = null;
            String content = null;
            Emoji roleEmoji = emoji != null && emoji.charAt(0) == '<' ? Emoji.fromMarkdown(emoji) : emoji != null ? Emoji.fromUnicode(emoji) : null;
            int amount = 0;

            try {

                PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
                messagePstmt.setString(1, messageName);
                ResultSet messageRs = messagePstmt.executeQuery();

                while (messageRs.next()) {
                    amount++;
                }
                messageRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Update type of message
            String updateQuery = "UPDATE messages SET type = ? WHERE name = ?";

            try {

                PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
                updatePstmt.setString(1, amount <= 1 ? "single" : "multi");
                updatePstmt.setString(2, messageName);
                updatePstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Check if message has already been sent, if so update button in sent message
            //Gets discord message information
            String channelQuery = "SELECT * FROM messages, channels WHERE messages.name = ? AND messages.name = channels.message";
            Boolean sent = false;

            try {

                PreparedStatement channelPstmt = conn.prepareStatement(channelQuery);
                channelPstmt.setString(1, messageName);
                ResultSet channelRs = channelPstmt.executeQuery();

                while (channelRs.next()) {
                    messageId = channelRs.getString("id");
                    channelId = channelRs.getString("channelId");
                    content = channelRs.getString("content");
                    sent = true;
                }
                channelRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Update button
            if (sent) {

                int finalAmount = amount;
                String finalContent = content;
                String finalDescription = description;
                event.getGuild().getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {

                    message.editMessage(finalContent).setActionRow(
                            Button.primary(finalAmount == 1 ? "single" : "multi", finalAmount == 1 ? finalDescription : "Click me to toggle roles!")
                            .withEmoji(finalAmount == 1 ? roleEmoji : null)
                    ).queue();
                });
            }
        }
    }
}
