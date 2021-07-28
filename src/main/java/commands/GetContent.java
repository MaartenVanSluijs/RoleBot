package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class GetContent extends Command {

    Connection conn;

    public GetContent() {

        this.name = "getContent";
        this.arguments = "<messageName>";
        this.guildOnly = true;

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

            String words[] = pair.split("\\{");

            if (words[0].equals("message") || words[0].equals("name") || words[0].equals("msg")) {
                messageName = words[1];
            }
        }

        if (event.getArgs().length() != 0 && messageName == null) {
            event.reply("Please use the following syntax: name/message/msg{<messageName>}");
            return;
        }

        if (messageName == null) {
            event.reply("No message name found, please give at least a message name");
            return;
        }

        String contentQuery = "SELECT * FROM messages WHERE name = ?";
        String response = String.format("This is the content for the message with name *%s*: \n", messageName);
        int size = 0;

        try {

            PreparedStatement contentPstmt = conn.prepareStatement(contentQuery);
            contentPstmt.setString(1, messageName);
            ResultSet contentRs = contentPstmt.executeQuery();

            while (contentRs.next()) {
                response += contentRs.getString("content");
                size++;
            }
            contentRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("Found no message with name: *%s*, try !getMessages for an overview", messageName));
            return;
        }

        event.reply(response);

    }
}
