package moe.herz;

import java.sql.SQLException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.net.URISyntaxException;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.User;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.ConnectEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnichanBot extends ListenerAdapter {
    private final YoutubeService youtubeService;
    private final LastFmService lastFmService;
    private final UrbanDictionaryService urbanDictionaryService;
    private final HelpService helpService;
    private Set<String> ignoredUrls;
    final String BOT_NAME;
    private final String BOT_VERSION = "0.8.2 rev. 1";
    private final String BOT_NICKSERV_PW;
    private final String BOT_NICKSERV_EMAIL;
    private final String BOT_ADMIN;
    final String SERVER_NAME;
    final int SERVER_PORT;
    public String[] CHANNEL_NAMES;
    private final Config config;
    private static final Logger logger = LoggerFactory.getLogger(OnichanBot.class);

    public OnichanBot(YoutubeService youtubeService, LastFmService lastFmService, UrbanDictionaryService urbanDictionaryService, Config config) {
        this.config = config;
        this.youtubeService = youtubeService;
        this.lastFmService = lastFmService;
        this.BOT_NAME = config.getBotName();
        this.SERVER_NAME = config.getServerName();
        this.SERVER_PORT = config.getServerPort();
        this.CHANNEL_NAMES = config.getChannelNames();  // Populate from Config
        this.urbanDictionaryService = urbanDictionaryService;
        this.helpService = new HelpService();
        this.BOT_NICKSERV_PW = config.getNickservPw();
        this.BOT_NICKSERV_EMAIL = config.getNickservEmail();
        this.BOT_ADMIN = config.getBotAdmin();
    }

    public static void main(String[] args) throws SQLException {
        BotInitializer initializer = new BotInitializer();
        PircBotX bot = initializer.initializeBot();

        try {
            bot.startBot();
        } catch (Exception e) {
            logger.error("An error occurred", e);
        }
    }

    @Override
    public void onConnect(ConnectEvent event) {
        boolean isRegistered = config.isBotRegistered(SERVER_NAME);

        if (isRegistered) {
            // Send identify command to NickServ
            event.getBot().sendIRC().message("NickServ", "IDENTIFY " + BOT_NICKSERV_PW);
        } else {
            // Register with NickServ and save to the database
            event.getBot().sendIRC().message("NickServ", "REGISTER " + BOT_NICKSERV_PW + " " + BOT_NICKSERV_EMAIL);
            config.setBotRegistered(SERVER_NAME);
        }
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        // Ignore private/direct messages
        if (event instanceof PrivateMessageEvent) {
            return;
        }

        String message = event.getMessage();
        User user = event.getUser();
        String nick = user != null ? user.getNick() : "";
        Pattern urlPattern = Pattern.compile("(https?://[\\w.-]+\\.[\\w.-]+[\\w./?=&#%\\-()@]*)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(message);

        if (message.startsWith(".help")) {
            handleHelpCommand(event);
        } else if (message.startsWith("!botcheck")){
            event.respondWith("Onichan! Onichan!, I'm " + BOT_NAME + " (Version " + BOT_VERSION + ")");
        } else if (message.startsWith(".np")) {
            handleNowPlayingCommand(event, message);
        } else if (message.startsWith(".yt ")) {
            handleYoutubeCommand(event, message);
        } else if (message.startsWith(".ud ")) {
            handleUrbanDictionaryCommand(event, message);
        } else if (message.startsWith(".deepl ")) {
            handleDeepLCommand(event, message);
        } else if (message.startsWith("!reload")) {
            if (nick != null && nick.equals(BOT_ADMIN)) {
                loadIgnoredUrls("ignored_urls.txt");
                event.respondWith("Ignore list reloaded.");
            } else {
                event.respondWith("You're not my master! Hmpf!");
            }
        } else {
            handleUrlFetching(event, matcher);
        }
    }

    private void handleDeepLCommand(GenericMessageEvent event, String message) {
        // Split the command message by spaces. Expecting format: .deepl [targetLang] [text]
        String[] parts = message.split(" ", 3);

        // Check if the command has the minimum required parts (command, language, text)
        if (parts.length < 3) {
            event.respond("Usage: .deepl [targetLang] [text to translate]");
            return;
        }

        String targetLanguage = parts[1].toUpperCase(); // Ensure the language code is in upper case
        String textToTranslate = parts[2];

        try {
            // Assuming you have a DeepLTranslator instance accessible here, or create one
            DeepLService translator = new DeepLService(config);
            String translatedText = translator.translateText(textToTranslate, targetLanguage);

            // Respond with the translated text
            event.respond(translatedText);
        } catch (IOException | URISyntaxException e) {
            // Log the exception or respond with an error message
            logger.error("Error during translation", e);
            event.respond("Failed to translate text due to an error.");
        } catch (Exception e) {
            // Catch all for any other unexpected errors
            logger.error("Unexpected error during translation", e);
            event.respond("An unexpected error occurred.");
        }
    }


    private void handleNowPlayingCommand(GenericMessageEvent event, String message) {
        String ircUsername = event.getUser().getNick();
        String username;

        if (message.length() > 4) {
            // Extract the username from the message if it's provided
            username = message.substring(4);
            lastFmService.saveLastFmUsername(ircUsername, username);
        } else {
            // If no Last.fm username was specified in the message, get it from the database
            username = lastFmService.getLastFmUsernameFromDb(ircUsername);

            // If the Last.fm username couldn't be retrieved from the database, there's nothing more to do
            if (username == null) {
                event.respondWith("No Last.fm username associated with " + ircUsername + ". Please provide your Last.fm username.");
                return;
            }
        }

        try {
            String response = lastFmService.getCurrentTrack(username);
            event.respondWith(response);
        } catch (Exception e) {
            logger.error("An error occurred", e);
        }
    }

    private void handleYoutubeCommand(GenericMessageEvent event, String message) {
        String query = message.substring(4);
        String videoUrl = youtubeService.searchYoutube(query);
        if (videoUrl != null) {
            event.respondWith(videoUrl);
        }
    }

    void loadIgnoredUrls(String filePath) {
        try {
            ignoredUrls = new HashSet<>(Files.readAllLines(Paths.get(filePath)));
        } catch (IOException e) {
            logger.error("An error occurred", e);
        }
    }

    private void handleUrbanDictionaryCommand(GenericMessageEvent event, String message) {
        String term = message.substring(4);
        List<String> definitions = urbanDictionaryService.searchUrbanDictionary(term);
        for (int i = 0; i < definitions.size() && i < 4; i++) {
            String definition = definitions.get(i);
            if (!definition.trim().isEmpty()) {
                event.respondWith(definition);
            }
        }
        if (definitions.size() > 4) {
            event.respondWith("... [message truncated due to length]");
        }
    }

    private void handleUrlFetching(GenericMessageEvent event, Matcher matcher) {
        if (matcher.find()) {
            String url = matcher.group(1);

            boolean shouldIgnore = false;
            for (String ignoredUrl : ignoredUrls) {
                if (url.startsWith(ignoredUrl)) {
                    shouldIgnore = true;
                    break;
                }
            }
            if (shouldIgnore) {
                return;  // Exit the method if the URL should be ignored
            }

            String videoId = null;

            if (url.contains("youtube.com/watch?v=")) {
                Pattern pattern = Pattern.compile("v=([^&]*)");
                Matcher videoMatcher = pattern.matcher(url);
                if (videoMatcher.find()) {
                    videoId = videoMatcher.group(1);
                }
            } else if (url.contains("youtu.be/")) {
                Pattern pattern = Pattern.compile("youtu\\.be/([^?&]*)");
                Matcher videoMatcher = pattern.matcher(url);
                if (videoMatcher.find()) {
                    videoId = videoMatcher.group(1);
                }
            } else if (url.contains("youtube.com/playlist?list=")) {
                Pattern pattern = Pattern.compile("list=([^&]*)");
                Matcher playlistMatcher = pattern.matcher(url);
                if (playlistMatcher.find()) {
                    String playlistId = playlistMatcher.group(1);
                    String playlistDetails = youtubeService.getPlaylistDetails(playlistId);
                    if (playlistDetails != null) {
                        event.respondWith(playlistDetails);
                    }
                }
            } else if (url.contains("youtube.com/@")) {
                Pattern pattern = Pattern.compile("@([a-zA-Z0-9_-]+)");
                Matcher usernameMatcher = pattern.matcher(url);
                if (usernameMatcher.find()) {
                    String username = usernameMatcher.group(1);
                    // Use your new method to get the channel ID from the username
                    String channelId = youtubeService.getChannelIdFromUsernameUsingSearch(username);
                    if (channelId != null) {
                        String channelDetails = youtubeService.getChannelDetails(channelId);
                        if (channelDetails != null) {
                            event.respondWith(channelDetails);
                        }
                    }
                }

            } else if (url.contains("youtube.com/channel/")) {
                Pattern pattern = Pattern.compile("channel/([a-zA-Z0-9_-]+)");
                Matcher channelMatcher = pattern.matcher(url);
                if (channelMatcher.find()) {
                    String channelId = channelMatcher.group(1);
                    String channelDetails = youtubeService.getChannelDetails(channelId);
                    if (channelDetails != null) {
                        event.respondWith(channelDetails);
                    }
                }
            }

            if (videoId != null) {
                String videoDetails = youtubeService.getVideoDetails(videoId);
                if (videoDetails != null) {
                    if (event instanceof MessageEvent messageEvent) {
                        messageEvent.getBot().sendIRC().message(messageEvent.getChannel().getName(), videoDetails);
                    }
                }
            } else {
                // Skip non-HTML files
                String[] skippedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".webm", ".mp4", ".mp3", ".wav", ".ogg", ".flac", ".mkv", ".avi", ".flv"};
                boolean skip = false;
                for (String extension : skippedExtensions) {
                    if (url.toLowerCase().endsWith(extension)) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    // Use the UrlMetadataFetcher class to get the metadata
                    String metadata = UrlMetadataFetcher.fetchWebsiteMetadata(url);
                    event.respondWith(metadata);
                }
            }
        }
    }

    @Override
    public void onInvite(InviteEvent event) {
        User user = event.getUser();
        if (user != null && user.getNick() != null && user.getNick().equals(BOT_ADMIN)) {
            String channelName = event.getChannel();
            if (channelName != null) {
                event.getBot().sendIRC().joinChannel(channelName);
            }
        }
    }

    private void handleHelpCommand(GenericMessageEvent event) {
        User user = event.getUser();
        if(user == null) {
            return;
        }

        if (event instanceof MessageEvent messageEvent) {
            messageEvent.getChannel().send().message("I will send you a list of all my commands per DM");
            helpService.sendHelp(user, event.getBot());
        } else if (event instanceof PrivateMessageEvent) {
            helpService.sendHelp(user, event.getBot());
        }
    }

}