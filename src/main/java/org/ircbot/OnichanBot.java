package org.ircbot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.SSLSocketFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class OnichanBot extends ListenerAdapter {

    public static void main(String[] args)  {

        String botName = "onichanbot";

        // Configure the bot
        Configuration configuration = new Configuration.Builder()
                .setName(botName)
                .addServer("***REMOVED***", 6697)
                .addAutoJoinChannel("#counttoonemillion")
                .addListener(new OnichanBot())
                .setSocketFactory(SSLSocketFactory.getDefault()) // Enable SSL
                .buildConfiguration();

        // Connect and start the bot
        try (PircBotX bot = new PircBotX(configuration)) {
            bot.startBot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if (event.getMessage().startsWith(".deepl")) {
            String[] parts = event.getMessage().split(" ", 3);
            if (parts.length == 3) {
                String targetLanguage = parts[1];
                String text = parts[2].replace("\"", "");
                String translatedText = "";

                try {
                    translatedText = translateText(text, targetLanguage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                event.respond(translatedText);
            }
        }
    }

    private String translateText(String text, String targetLanguage) throws IOException {
        String apiKey = "***REMOVED***";
        String apiUrl = "https://api-free.deepl.com/v2/translate";
        String charset = "UTF-8";
        String encodedText = java.net.URLEncoder.encode(text, charset);
        String requestUrl = apiUrl + "?auth_key=" + apiKey + "&target_lang=" + targetLanguage + "&text=" + encodedText;

        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            java.io.InputStream inputStream = connection.getInputStream();
            java.io.InputStreamReader inputStreamReader = new java.io.InputStreamReader(inputStream, charset);
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(inputStreamReader);
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null) {
                content.append(inputLine);
            }
            bufferedReader.close();
            connection.disconnect();
            // Parse JSON to extract translated text
            JsonElement jsonElement = JsonParser.parseString(content.toString());
            return jsonElement.getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        } else {
            return "Error: " + responseCode;
        }
    }

}