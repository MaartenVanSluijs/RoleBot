package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class AddRole extends Command {

    Connection conn;

    public AddRole() {

        this.name = "addRole";
        this.guildOnly = true;
        this.aliases = new String[]{"linkRole", "setRole", "bindRole"};
        this.arguments = "<messageName> <roleName>";

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
        String messageType = null;
        String messageName = null;
        String roleName = null;

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("message") || words[0].equals("msg")) {
                messageName = words[1];
            } else if (words[0].equals("role")) {
                roleName = words[1];
            }
        }

        if (event.getArgs().length() != 0 && (messageName == null && roleName == null)) {
            event.reply("Please use the following syntax: message/msg{<messageName>} role{<roleName>}");
            return;
        }

        if (messageName == null) {
            event.reply("No message name found, please give at least a message name");
            return;
        }

        if (roleName == null) {
            event.reply("No role name found, please give at least a role name");
        }

        String roleQuery = "SELECT * FROM roles WHERE name = ?";
        String messageQuery = "SELECT * FROM messages WHERE name = ?";
        String typeQuery = "SELECT * FROM roles WHERE messageName = ?";
        String updateQuery = "UPDATE roles SET messageName = ? WHERE name = ?";

        try {

            PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
            rolePstmt.setString(1, roleName);
            ResultSet roleRs = rolePstmt.executeQuery();

            int size = 0;
            while (roleRs.next()) {
                size++;
            }

            if (size == 0) {
                event.reply(String.format("Could not find role with name: *%s*", roleName));
                return;
            }
            roleRs.close();

            PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
            messagePstmt.setString(1, messageName);
            ResultSet messageRs = messagePstmt.executeQuery();

            size = 0;
            while (messageRs.next()) {
                messageType = messageRs.getString("type");
                size++;
            }

            if (size == 0) {
                event.reply(String.format("Found no message with name: *%s*, try !getMessages for an overview", messageName));
                return;
            }
            messageRs.close();

            PreparedStatement typePstmt = conn.prepareStatement(typeQuery);
            typePstmt.setString(1, messageName);
            ResultSet typeRs = typePstmt.executeQuery();

            size = 0;
            while (typeRs.next()) {
                size++;
            }

            if (size != 0 && messageType.equals("single")) {
                event.reply(String.format("Message *%s* already has a corresponding role and is of type *single*. Remove the role or set to *multi* before trying again", messageName));
                return;
            }
            typeRs.close();

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, messageName);
            updatePstmt.setString(2, roleName);
            updatePstmt.executeUpdate();

            event.reply(String.format("Successfully bound role *%s* to message *%s*", roleName, messageName));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
