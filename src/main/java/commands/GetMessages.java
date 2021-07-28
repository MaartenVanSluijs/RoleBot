package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.sql.*;

public class GetMessages extends Command {

    Connection conn;

    public GetMessages() {
        this.name = "getMessages";
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

        String messageQuery = "SELECT * FROM messages";
        String response = "Found the following messages (For content use !getContent name/message{<messageName>}): ";
        int size = 0;

        try {

            Statement messageStmt = conn.createStatement();
            ResultSet messageRs = messageStmt.executeQuery(messageQuery);

            while (messageRs.next()) {
                response += String.format("\n-Name: *%s*, Type: *%s*, Id: *%s*",
                                            messageRs.getString("name"),
                                            messageRs.getString("type"),
                                            messageRs.getString("id"));
                size++;
            }

            if (size != 0) {
                event.reply(response);
            } else {
                event.reply("No messages found");
            }
            messageRs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
