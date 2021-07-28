package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.sql.*;

public class EditRole extends Command {

    Connection conn;

    public EditRole() {

        this.name = "editRole";
        this.guildOnly = true;
        this.arguments = "<name> [description] [emoji] [gated]";
        this.aliases = new String[]{"changeRole", "updateRole"};

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
        String roleName = null;
        String description  = null;
        String emoji = null;
        String gated = null;

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("role")) {
                roleName = words[1];
            } else if (words[0].equals("description") || words[0].equals("desc") || words[0].equals("dc")) {
                description = words[1];
            } else if (words[0].equals("emoji")) {
                emoji = words[1];
            } else if (words[0].equals("gated") || words[0].equals("gate")) {
                gated = words[1];
            }
        }

        if (event.getArgs().length() != 0 && (roleName == null && description == null && emoji == null && gated == null)) {
            event.reply("Please use the following syntax: name/role{<roleName>} description/desc/dc{[value]} emoji{[value]} gated/gate{[roleId]}");
            return;
        }

        if (roleName == null) {
            event.reply("No role name found, please give at least a name");
            return;
        }

        String roleQuery = "SELECT * FROM roles WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
            rolePstmt.setString(1, roleName);
            ResultSet roleRs = rolePstmt.executeQuery();

            while (roleRs.next()) {
                size++;

                if (description == null) {
                    description = roleRs.getString("description");
                }

                if (emoji == null) {
                    emoji = roleRs.getString("emoji");
                }

                if (gated == null) {
                    gated = roleRs.getString("gated");
                }
            }
            roleRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        if (gated != null && gated.equals("null")) {
            gated = null;
        }

        if (description != null && description.equals("null")) {
            description = null;
        }

        if (emoji != null && emoji.equals("null")) {
            emoji = null;
        }

        if (gated != null && !event.getGuild().getRoles().contains(event.getGuild().getRoleById(gated))) {
            event.reply("This is not a valid gate-role id, please make the gate-role first before creating the role here");
            return;
        }

        if (size == 0) {
            event.reply(String.format("Could not find role with name: *%s*", roleName));
            return;
        }

        String updateQuery = "UPDATE roles SET description = ?, emoji = ?, gated = ? WHERE name = ?";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, description);
            updatePstmt.setString(2, emoji);
            updatePstmt.setString(3, gated);
            updatePstmt.setString(4, roleName);

            updatePstmt.executeUpdate();
            event.reply(String.format("Successfully update role *%s*", roleName));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String messageQuery = "SELECT messages.type, messages.id, messages.content FROM roles, messages WHERE roles.name = ? AND roles.messageName = messages.name";

        try {

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, roleName);
            ResultSet messageRs = messagePstmt.executeQuery();

            while (messageRs.next()) {

                if (messageRs.getString("id") != null && messageRs.getString("type").equals("single")) {

                    String messageId = messageRs.getString("id");
                    String messageContent = messageRs.getString("content");

                    for (TextChannel channel : event.getGuild().getTextChannels()) {

                        String finalDescription = description;
                        Emoji roleEmoji = emoji != null && emoji.charAt(0) == '<' ? Emoji.fromMarkdown(emoji) : emoji != null ? Emoji.fromUnicode(emoji) : null;
                        channel.retrieveMessageById(messageId).queue(message -> {

                            message.editMessage(messageContent).setActionRow(Button.primary("single", finalDescription)
                                    .withEmoji(roleEmoji)).queue();

                        }, failure -> {});
                    }
                }
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
