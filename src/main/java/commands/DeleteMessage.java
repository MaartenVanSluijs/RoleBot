package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.*;

public class DeleteMessage extends Command {

    Connection conn;

    public DeleteMessage() {
        this.name = "deleteMessage";
        this.arguments = "<name>";
        this.guildOnly = true;
        this.aliases = new String[]{};

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
        String messageId = null;

        for (String pair : pairs) {

            String words[] = pair.split("\\{");

            if (words[0].equals("name") || words[0].equals("message") || words[0].equals("msg")) {
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

        String nameQuery = "SELECT * FROM messages WHERE name = ?";
        int size = 0;

        try {

            PreparedStatement namePstmt = conn.prepareStatement(nameQuery);
            namePstmt.setString(1, messageName);
            ResultSet nameRs = namePstmt.executeQuery();

            while (nameRs.next()) {
                size++;
                messageId = nameRs.getString("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (size == 0) {
            event.reply(String.format("No message found with name: *%s*", messageName));
            return;
        }

        String deleteQuery = "DELETE FROM messages WHERE name = ?";

        try {

            PreparedStatement deletePstmt = conn.prepareStatement(deleteQuery);
            deletePstmt.setString(1, messageName);
            deletePstmt.executeUpdate();

            event.reply("Successfully deleted message!");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (messageId != null) {

            for (TextChannel channel : event.getGuild().getTextChannels()) {

                channel.deleteMessageById(messageId).queue(null, failure -> {});
            }
        }
    }
}
