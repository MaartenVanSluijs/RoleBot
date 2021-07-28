package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class RemoveRole extends Command {

    Connection conn;

    public RemoveRole() {

        this.name = "removeRole";
        this.guildOnly = true;
        this.aliases = new String[]{"unbindRole"};
        this.arguments = "<roleName>";

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

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("role") || words[0].equals("name")) {
                roleName = words[1];
            }
        }

        if (event.getArgs().length() != 0 && (roleName == null)) {
            event.reply("Please use the following syntax: role{<roleName>}");
            return;
        }

        if (roleName == null) {
            event.reply("No role name found, please give at least a role name");
        }

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

        if (size == 0) {
            event.reply(String.format("Could not find role with name: *%s*", roleName));
            return;
        }

        String value = null;
        String updateQuery = "UPDATE roles SET messageName = ? WHERE name = ?";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, value);
            updatePstmt.setString(2, roleName);
            updatePstmt.executeUpdate();

            event.reply("Successfully unbound role");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
