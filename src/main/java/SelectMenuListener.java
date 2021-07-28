
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Role;

import java.sql.*;
import java.util.*;

public class SelectMenuListener extends ListenerAdapter{

    List<Role> roles = new ArrayList<Role>();       //Roles list used to update user's roles
    public Connection conn;                         //Database connection

    public SelectMenuListener() {

        //Sets up connection to database
        String url = "jdbc:sqlite:C:/Users/20182667/Documents/Programming/Discord Bots/TestBot/DummyBase.db";
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {

        roles.clear();
        roles.addAll(event.getMember().getRoles());

        for (String roleId: event.getValues()) {

            Role role = event.getGuild().getRoleById(roleId);

            //Adds roles if user doesn't already have them
            if (!roles.contains(role)) {
                roles.add(role);
            //Starts removing roles if user does have them
            } else if (roles.contains(role)) {

                try {
                    //Query for selecting roles gated by the to-be-removed role
                    String gatedQuery = "SELECT id FROM roles WHERE gated = ?";
                    PreparedStatement gatedPstmt = conn.prepareStatement(gatedQuery);
                    gatedPstmt.setString(1, roleId);
                    ResultSet gatedrs = gatedPstmt.executeQuery();

                    //Removes additional roles if they're gated behind the to-be-removed role
                    while (gatedrs.next()) {
                        if (roles.contains(event.getGuild().getRoleById(gatedrs.getString("id")))) {
                            roles.remove(event.getGuild().getRoleById(gatedrs.getString("id")));
                        }
                    }
                    roles.remove(role);

                    gatedrs.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        //Feedback
        event.getGuild().modifyMemberRoles(event.getMember(), roles).queue(success -> {

            MessageEmbed embed = new EmbedBuilder().setTitle("Successfully toggled roles!").build();
            event.replyEmbeds(embed).setEphemeral(true).queue();
        });
    }
}
