import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import slashCommands.*;

public class CommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {

        switch (event.getName()) {

            case "getmessages":
                new GetMessages().run(event);
                break;
            case "getroles":
                new GetRoles().run(event);
                break;
            case "createrole":
                new CreateRole().run(event);
                break;
            case "sendmessage":
                new SendMessage().run(event);
                break;
            case "createmessage":
                new CreateMessage().run(event);
                break;
            case "deletemessage":
                new DeleteMessage().run(event);
                break;
            case "editmessage":
                new EditMessage().run(event);
                break;
            case "editrole":
                new EditRole().run(event);
                break;
        }
    }
}
