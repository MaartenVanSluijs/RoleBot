package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class CreateMessage extends Command {

    Connection conn;

    public CreateMessage() {
        this.name = "createMessage";
        this.aliases = new String[]{};
        this.guildOnly = true;
        this.arguments = "<name> <type> [content]";

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

        if (type == null) {
            event.reply("No message type found, please give at least a message type(either single or multi)");
            return;
        }

        if (!type.equals("single") && !type.equals("multi")) {
            event.reply(String.format("*%s* is not a valid message type, message types are either single or multi", type));
            return;
        }

        int size = 0;
        String nameQuery = "SELECT name FROM messages WHERE name = ?";

        try {

            PreparedStatement namePstmt = conn.prepareStatement(nameQuery);
            namePstmt.setString(1, messageName);
            ResultSet nameRs = namePstmt.executeQuery();

            while (nameRs.next()) {
                size++;
            }
            nameRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size != 0) {
            event.reply(String.format("A message already exists with name: *%s*", messageName));
            return;
        }

        String updateQuery = "INSERT INTO messages (name, type, content) VALUES (?, ?, ?)";

        try {

            PreparedStatement updatePstmt = conn.prepareStatement(updateQuery);
            updatePstmt.setString(1, messageName);
            updatePstmt.setString(2, type);
            updatePstmt.setString(3, content);
            updatePstmt.executeUpdate();

            event.reply("Successfully created message!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
