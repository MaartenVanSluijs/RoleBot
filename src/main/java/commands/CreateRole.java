package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class CreateRole extends Command {

    Connection conn;

    public CreateRole() {
        this.name = "createRole";
        this.guildOnly = true;
        this.arguments = "<id> <name> [description] [emoji] [gated]";

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
        String roleId = null;
        String roleName = null;
        String description = null;
        String emoji = null;
        String gated = null;

        for (String pair : pairs) {

            String[] words = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("role")) {
                roleName = words[1];
            } else if (words[0].equals("id")) {
                roleId = words[1];
            } else if (words[0].equals("description") || words[0].equals("desc") || words[0].equals("dc")) {
                description = words[1];
            } else if (words[0].equals("emoji")) {
                emoji = words[1];
            } else if (words[0].equals("gated") || words[0].equals("gate")) {
                gated = words[1];
            }
        }

        if (event.getArgs().length() != 0 && (roleName == null && roleId == null && description == null && emoji == null && gated == null)) {
            event.reply("Please use the following syntax: name/role{<roleName>} id{<roleId>} description/desc/dc{[value]} emoji{[value]} gated/gate{[roleId]}");
            return;
        }

        if (roleName == null) {
            event.reply("No role name found, please give at least a name");
            return;
        }

        if (roleId == null) {
            event.reply("No role id found, please give at least an id");
            return;
        }

        if (!event.getGuild().getRoles().contains(event.getGuild().getRoleById(roleId))) {
            event.reply("This is not a valid role id, please make the role in the discord first before creating a role here");
            return;
        }

        if (gated != null && !event.getGuild().getRoles().contains(event.getGuild().getRoleById(gated))) {
            event.reply("This is not a valid gate-role id, please make the gate-role first before creating the role here");
            return;
        }

        String roleQuery = "SELECT * FROM roles WHERE id = ? OR name = ?";
        int size = 0;

        try {

            PreparedStatement rolePstmt = conn.prepareStatement(roleQuery);
            rolePstmt.setString(1, roleId);
            rolePstmt.setString(2, roleName);
            ResultSet roleRs = rolePstmt.executeQuery();

            while (roleRs.next()) {
                size++;
            }

            if (size != 0) {
                event.reply(String.format("A role already exists with id: *%s*, or with name: *%s*", roleId, roleName));
                return;
            }
            roleRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insertQuery = "INSERT INTO roles (id, name, description, emoji, gated) VALUES (?, ?, ?, ?, ?)";

        try {

            PreparedStatement insertPstmt = conn.prepareStatement(insertQuery);
            insertPstmt.setString(1, roleId);
            insertPstmt.setString(2, roleName);
            insertPstmt.setString(3, description);
            insertPstmt.setString(4, emoji);
            insertPstmt.setString(5, gated);
            insertPstmt.executeUpdate();

            event.reply(String.format("Successfully added role *%s*", roleName));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
