package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class GetRoles extends Command {

    Connection conn;

    public GetRoles() {
        this.name = "getRoles";
        this.guildOnly = true;
        this.aliases = new String[]{};
        this.arguments = "<message name>";

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

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("message") || words[0].equals("msg")) {
                messageName = words[1];
            }
        }

        if (event.getArgs().length() != 0 && messageName == null) {
            event.reply("Please use the following syntax: name/message/msg{<messageName>} (use messageName 'all' for an overview of all roles)");
            return;
        }

        if (messageName == null) {
            event.reply("No message name given, please give at least a message name (use messageName 'all' for an overview of all roles)");
            return;
        }

        if (!messageName.equals("all")) {

            String messageQuery = "SELECT * FROM messages WHERE name = ?";
            String roleQuery = "SELECT * FROM roles WHERE messageName = ?";

            try {
                PreparedStatement messagePstmt = conn.prepareStatement(messageQuery);
                messagePstmt.setString(1, messageName);

                PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
                rolePstmt.setString(1, messageName);

                ResultSet messageRs = messagePstmt.executeQuery();
                ResultSet roleRs = rolePstmt.executeQuery();

                String response = "";

                int size = 0;

                while (messageRs.next()) {
                    response += String.format("Roles for *%s*, of type *%s*, with id *%s*:",
                            messageRs.getString("name"),
                            messageRs.getString("type"),
                            messageRs.getString("id"));
                    size++;
                }

                while (roleRs.next()) {
                    response += String.format("\n- Name: *%s*, Id: *%s*, Description: *%s*, Emoji: *%s*, Gated: *%s*",
                            roleRs.getString("name"),
                            roleRs.getString("id"),
                            roleRs.getString("description"),
                            roleRs.getString("emoji"),
                            roleRs.getString("gated"));
                }
                if (size != 0) {
                    event.reply(response);
                } else {
                    event.reply(String.format("Found no message with name: *%s*, try !getMessages for an overview", messageName));
                }

                messageRs.close();
                roleRs.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {

            String roleQuery = "SELECT * FROM roles";

            try {

                Statement roleStmt = conn.createStatement();
                ResultSet roleRs = roleStmt.executeQuery(roleQuery);

                String response = "These are all the available roles:";

                while(roleRs.next()) {
                    response += String.format("\n- Name: *%s*, Id: *%s*, Description: *%s*, Emoji: *%s*, Gated: *%s*, MessageName: *%s*",
                            roleRs.getString("name"),
                            roleRs.getString("id"),
                            roleRs.getString("description"),
                            roleRs.getString("emoji"),
                            roleRs.getString("gated"),
                            roleRs.getString("messageName"));
                }

                roleRs.close();
                event.reply(response);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
