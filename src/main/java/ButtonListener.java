
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import slashCommands.Config;
import slashCommands.ConfigLoader;

import java.sql.*;
import java.util.*;

//Runs whenever a button is pressed
public class ButtonListener extends ListenerAdapter{

    public Connection conn;                     //Database connection
    public ArrayList<SelectOption> options;     //Options for selectMenu
    public List<Role> roles;                    //Corresponding roles
    public Config conf;
    public String url;
    public String user;
    public String password;

    public ButtonListener() {

        ConfigLoader cl = new ConfigLoader();
        conf = cl.loadConfig();

        url = "jdbc:mysql://172.18.0.1:3306/s241_roles";
        user = conf.getUser();
        password = conf.getPassword();
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {

        //Sets up connection with database

        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //User's roles before toggling
        roles = new ArrayList<Role>();
        roles.addAll(event.getMember().getRoles());

        //Button to toggle multiple roles is clicked
        if (event.getButton().getId().equals("multi")) {

            try {
                options = getOptions(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (options.size() == 0) {
                event.reply("There are no roles to toggle with this message").setEphemeral(true).queue();
                return;
            }

            //Creates the selection menu
            SelectionMenu menu = SelectionMenu.create("toggle")
                    .setPlaceholder("Choose your class")
                    .setRequiredRange(1, options.size())
                    .addOptions(options)
                    .build();

            event.reply("Please choose below:").addActionRow(menu).setEphemeral(true).queue();
        }
        //Button to toggle single role is clicked
        else if (event.getButton().getId().equals("single")) {

            //Query selecting role ids and gated based on messageId clicked button
            String query = "SELECT roles.id, roles.gated FROM roles, messages WHERE messages.name = roles.messageName AND messages.id = ?";
            try {
                //Executes query
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, event.getMessageId());
                ResultSet rs = pstmt.executeQuery();

                String roleId = null;
                String gate = null;
                while (rs.next()) {
                    roleId = rs.getString(1);
                    gate = rs.getString(2);
                }
                rs.close();
                Role role = event.getGuild().getRoleById(roleId);

                //If user didn't already have role -> start to add role
                if (!roles.contains(role)) {

                    //Check if role is gated, and if so if user meets pre-requisite
                    if (gate != null) {
                        if (roles.contains(event.getGuild().getRoleById(gate))) {
                            roles.add(role);
                        }
                    //If not gated add regardless
                    } else {
                        roles.add(role);
                    }

                //If user already has role -> start removing role and roles gated by that role
                } else {

                    //Executes query to find other roles gated by to-be-removed role
                    String gatedQuery = "SELECT id FROM roles WHERE gated = ?";
                    PreparedStatement gatedPstmt = conn.prepareStatement(gatedQuery);
                    gatedPstmt.setString(1, roleId);
                    ResultSet gatedrs = gatedPstmt.executeQuery();

                    //If user has roles gated by the removed role, remove those too
                    while (gatedrs.next()) {
                        if (roles.contains(event.getGuild().getRoleById(gatedrs.getString("id")))) {
                            roles.remove(event.getGuild().getRoleById(gatedrs.getString("id")));
                        }
                    }
                    //Removes original role
                    roles.remove(role);
                    gatedrs.close();
                }

                //Feedback
                if (!event.getMember().getRoles().equals(roles)) {
                    event.getGuild().modifyMemberRoles(event.getMember(), roles).queue(success -> {

                        MessageEmbed embed = new EmbedBuilder().setTitle("Successfully toggled role!").build();
                        event.replyEmbeds(embed).setEphemeral(true).queue();
                    });

                } else {
                    MessageEmbed embed = new EmbedBuilder().setTitle("Changed Nothing!").build();
                    event.replyEmbeds(embed).setEphemeral(true).queue();
                }

                rs.close();

            } catch (SQLException | HierarchyException e) {
                e.printStackTrace();
            }
        }
    }

    //Returns a list of options for the selectMenu
    public ArrayList<SelectOption> getOptions(ButtonClickEvent event) throws SQLException {

        ArrayList<SelectOption> options = new ArrayList<>();

        //Query to select the information on roles corresponding to clicked button
        String query = "SELECT roles.id, roles.name, description, emoji, gated FROM roles, messages WHERE messages.id = ? AND messages.name = roles.messageName";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, event.getMessageId());
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {

            //If user does not meet pre-requisite to gated role, don't show role in selectMenu
            if (rs.getString("gated") != null) {
                if (!roles.contains(event.getGuild().getRoleById(rs.getString("gated")))) {
                    continue;
                }
            }

            SelectOption option = SelectOption.of(rs.getString("name"), rs.getString("id"));

            //Adds description to role if not null
            if (!rs.getString("description").equals("null")) {
                option = option.withDescription(rs.getString("description"));
            }

            //Adds emoji to role if not null, either markdown or unicode
            if (!rs.getString("emoji").equals("null")) {

                if (rs.getString("emoji").charAt(0) == '<') {
                    option = option.withEmoji(Emoji.fromMarkdown(rs.getString("emoji")));
                } else if (rs.getString("emoji").charAt(0) == 'U') {
                    option = option.withEmoji(Emoji.fromUnicode(rs.getString("emoji")));
                }
            }

            options.add(option);

        }
        rs.close();
        return options;
    }
}

