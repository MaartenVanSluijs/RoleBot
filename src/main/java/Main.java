
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;

import com.jagrosh.jdautilities.command.CommandClient;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import slashCommands.Config;
import slashCommands.ConfigLoader;

import javax.security.auth.login.LoginException;

public class Main implements EventListener {

    public static void main(String[] args) throws LoginException, InterruptedException {

        CommandClientBuilder commandBuilder = new CommandClientBuilder();

        commandBuilder.setOwnerId("153171032465866753");
        commandBuilder.setEmojis("U+2714", "U+26A0", "U+274C");
        commandBuilder.setPrefix("!");
        commandBuilder.useHelpBuilder(true);
        commandBuilder.addCommands(
            new GetRoles(),
            new CreateMessage(),
            new DeleteMessage(),
            new GetMessages(),
            new CreateRole(),
            new DeleteRole(),
            new AddRole(),
            new GetContent(),
            new SendMessage(),
            new EditRole(),
            new EditMessage(),
            new RemoveRole()
        );

        CommandClient commandClient = commandBuilder.build();
        ConfigLoader cl = new ConfigLoader();
        Config conf = cl.loadConfig();

        JDA jda = JDABuilder.createDefault(conf.getToken())
                .addEventListeners(new ButtonListener())
                .addEventListeners(new SelectMenuListener())
                .addEventListeners(new CommandListener())
                .addEventListeners(new Main())
                .addEventListeners(commandClient).build().awaitReady();

        jda.getGuildById(conf.getGuildId()).updateCommands().addCommands(
                new CommandData("getmessages", "Get all the role messages")
                        .addOption(OptionType.BOOLEAN, "content", "Include content in response", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "messagename", "Give a specific message"),
                new CommandData("getroles", "Get all the roles")
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "messagename", "Give the roles for a specific message"),
                new CommandData("createrole", "Create a new role")
                        .addOption(OptionType.ROLE, "role", "The role to be created", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "description", "The description of this role")
                        .addOption(OptionType.STRING, "emoji", "The emoji for this role")
                        .addOption(OptionType.ROLE, "gate", "The role this role is gated behind")
                        .addOption(OptionType.STRING, "message", "The message this role belongs to"),
                new CommandData("sendmessage", "Sends a role message to a channel")
                        .addOption(OptionType.STRING, "message", "The message to be sent", true)
                        .addOption(OptionType.CHANNEL, "channel", "The channel to send the message in", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true),
                new CommandData("createmessage", "Create a new message")
                        .addOption(OptionType.STRING, "name", "The name of the new message", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "type", "The type this message needs to have (multi or single)")
                        .addOption(OptionType.STRING, "content", "The content for this message"),
                new CommandData("deletemessage", "Deletes a role message from a channel")
                        .addOption(OptionType.STRING, "name", "The message to be deleted", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true),
                new CommandData("editmessage", "Edits a role message")
                        .addOption(OptionType.STRING, "name", "Name of the message", true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "type", "The type this message needs to have (single or multi)")
                        .addOption(OptionType.STRING, "content", "The content for this message")
                        .addOption(OptionType.BOOLEAN, "delete", "Delete this message from the database"),
                new CommandData("editrole", "Edits a role")
                        .addOption(OptionType.ROLE, "role", "The role to be edited",true)
                        .addOption(OptionType.BOOLEAN, "ephemeral", "Make this response ephemeral", true)
                        .addOption(OptionType.STRING, "description", "The description of this role")
                        .addOption(OptionType.STRING, "emoji", "The emoji of this role")
                        .addOption(OptionType.ROLE, "gate", "The role this role is gated behind")
                        .addOption(OptionType.STRING, "message", "Change the message this role belongs to (null to remove from message)")
                        .addOption(OptionType.BOOLEAN, "delete", "Delete this role from the database")
        ).queue();
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            System.out.println("Api is ready!");
        }
    }
}
