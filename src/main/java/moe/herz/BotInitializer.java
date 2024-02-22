package moe.herz;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import javax.net.ssl.SSLSocketFactory;
import java.sql.SQLException;

public class BotInitializer {

    private final Config config;
    private final OnichanBot botInstance;

    public BotInitializer() throws SQLException {
        config = new Config();
        YoutubeService youtubeService = new YoutubeService(config);
        LastFmService lastFmService = new LastFmService(config);
        UrbanDictionaryService urbanDictionaryService = new UrbanDictionaryService(config);

        botInstance = new OnichanBot(youtubeService, lastFmService, urbanDictionaryService, config);
        botInstance.loadIgnoredUrls("ignored_urls.txt");
    }

    public PircBotX initializeBot() {
        Configuration.Builder builder = new Configuration.Builder()
                .setName(botInstance.BOT_NAME)
                .addServer(botInstance.SERVER_NAME, botInstance.SERVER_PORT)
                .addListener(botInstance)
                .setSocketFactory(SSLSocketFactory.getDefault());

        for(String channel : config.getChannelNames()) {
            builder.addAutoJoinChannel(channel.trim());
        }

        Configuration configuration = builder.buildConfiguration();

        return new PircBotX(configuration);
    }
}