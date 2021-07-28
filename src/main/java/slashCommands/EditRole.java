package slashCommands;

import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditRole extends BaseCommand{

    @Override
    public void run(SlashCommandEvent event) {

        String roleName = null;
        Boolean ephemeral = null;
        String description = null;
        String emoji = null;
        String gated = null;
        String messageName = null;
        Boolean delete = false;

        String boundMessage = null;
        Boolean messageRemoved = false;


        for (OptionMapping option : event.getOptions()) {

            if (option.getName().equals("role")) {
                roleName = option.getAsRole().getName();
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
            } else if (option.getName().equals("delete")) {
                delete = option.getAsBoolean();
            }
        }

        String roleQuery = "SELECT * FROM roles WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
            rolePstmt.setString(1, roleName);
            ResultSet roleRs = rolePstmt.executeQuery();

            while (roleRs.next()) {
                size++;
                boundMessage = roleRs.getString("messageName");

                if (description == null) {
                    description = roleRs.getString("description");
                }
                if (emoji == null) {
                    emoji = roleRs.getString("emoji");
                }
                if (gated == null) {
                    gated = roleRs.getString("gated");
                }
                if (messageName == null) {
                    messageName = roleRs.getString("messageName");
                }
            }
            roleRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Role *%s* doesn't exist yet", roleName)).setEphemeral(ephemeral).queue();
            return;
        }

        if (!delete) {

            if (description != null && description.equals("null")) {
                description = null;
            }
            if (emoji != null && emoji.equals("null")) {
                emoji = null;
            }
            if (gated != null && gated.equals("null")) {
                gated = null;
            }
            if (messageName != null && messageName.equals("null")) {
                messageName = null;
                messageRemoved = true;
            }

            String updateQuery = "UPDATE roles SET description = ?, emoji = ?, gated = ?, messageName = ? WHERE name = ?";

            try {

                PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
                updatePstmt.setString(1, description);
                updatePstmt.setString(2, emoji);
                updatePstmt.setString(3, gated);
                updatePstmt.setString(4, messageName);
                updatePstmt.setString(5, roleName);
                updatePstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            event.reply(String.format("Successfully edited role *%s*", roleName)).setEphemeral(ephemeral).queue();

            System.out.println(boundMessage);
            if (boundMessage != null) {
                this.updateMessage(event, boundMessage);
                if (!messageRemoved) {
                        this.updateMessage(event, messageName);
                }
            }

        } else {

            String deleteQuery = "DELETE FROM roles WHERE name = ?";

            try {

                PreparedStatement deletePstmt = conn.prepareStatement(deleteQuery);
                deletePstmt.setString(1, roleName);
                deletePstmt.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            event.reply(String.format("Successfully deleted role: *%s*", roleName)).setEphemeral(true).queue();

            if (boundMessage != null) {

                this.updateMessage(event, boundMessage);

            }
        }
    }

    void updateMessage(SlashCommandEvent event, String messageName) {

        String messageQuery = "SELECT roles.emoji, roles.description, messages.id, channels.channelId, messages.content FROM roles, messages, channels WHERE roles.messageName = ? AND roles.messageName = messages.name AND messages.name = channels.message";
        String channelId = null;
        String messageId = null;
        String content = null;
        String description = null;
        String emoji = null;
        Boolean sent = false;
        int amount = 0;

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {
                amount++;
                emoji = amount == 1 ? messageRs.getString("emoji") : null;
                description = amount == 1 ? messageRs.getString("description") : null;
                messageId = messageRs.getString("id");
                channelId = messageRs.getString("channelId");
                content = messageRs.getString("content");
                sent = messageId != null;
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String updateQuery = "UPDATE messages SET type = ? WHERE name = ?";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, amount <= 1 ? "single" : "multi");
            updatePstmt.setString(2, messageName);
            updatePstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (sent) {

            Emoji roleEmoji = emoji != null && emoji.charAt(0) == '<' ? Emoji.fromMarkdown(emoji) : emoji != null ? Emoji.fromUnicode(emoji) : null;

            int finalAmount = amount;
            String finalContent = content;
            String finalDescription = description;
            event.getGuild().getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {

                message.editMessage(finalContent).setActionRow(
                        Button.primary(finalAmount == 1 ? "single" : "multi", finalDescription != null ? finalDescription : "Click me to toggle roles!")
                                .withEmoji(roleEmoji)
                ).queue();
            });
        }
    }
}
