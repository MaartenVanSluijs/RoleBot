import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import slashCommands.*;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {

        System.out.println(event.getName() + event.getSubcommandName());

        switch (event.getName() + event.getSubcommandName()) {

            case "getmessages":
                new GetMessages().run(event);
                break;
            case "getroles":
                new GetRoles().run(event);
                break;
            case "rolecreate":
                new CreateRole().run(event);
                break;
            case "messagesend":
                new SendMessage().run(event);
                break;
            case "messagecreate":
                new CreateMessage().run(event);
                break;
            case "messagedelete":
                new DeleteMessage().run(event);
                break;
            case "messageedit":
                new EditMessage().run(event);
                break;
            case "roleedit":
                new EditRole().run(event);
                break;
        }
    }
}
