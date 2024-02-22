package moe.herz;

import org.pircbotx.User;
import org.pircbotx.PircBotX;

import java.util.HashMap;
import java.util.Map;

public class HelpService {
    private final Map<String, String> commands = new HashMap<>();

    public HelpService() {
        commands.put(
                ".yt <search term>",
                "Searches YouTube and returns a video matching the provided search term."
        );
        commands.put(
                ".np <last.fm username>",
                "Displays the most recent song played by the specified Last.fm username. You only need to provide your Last.fm username once."
        );
        commands.put(
                ".ud <search term>",
                "Searches Urban Dictionary and provides a definition for the specified term."
        );
    }

    public void sendHelp(User user, PircBotX bot) {
        bot.sendIRC().message(user.getNick(), "Here are all my commands:");

        for (Map.Entry<String, String> command : commands.entrySet()) {
            bot.sendIRC().message(user.getNick(), command.getKey() + " - " + command.getValue());
        }
    }
}