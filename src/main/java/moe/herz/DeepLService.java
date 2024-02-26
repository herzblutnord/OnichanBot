package moe.herz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URISyntaxException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class DeepLService {

    private String apiKey;
    private static final Logger logger = LoggerFactory.getLogger(DeepLService.class);

    public DeepLService(Config config) {
        this.apiKey = config.getdeeplapiKey();
    }

    public String translateText(String text, String targetLanguage) throws UnsupportedEncodingException {
        String apiUrl = "https://api-free.deepl.com/v2/translate";
        String charset = "UTF-8";
        String requestUrl = apiUrl + "?auth_key=" + apiKey + "&target_lang=" + targetLanguage + "&text=" + URLEncoder.encode(text, charset);

        HttpURLConnection connection = null;

        try {
            URI uri = new URI(requestUrl);
            URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    content.append(inputLine);
                }
                bufferedReader.close();
                JsonElement jsonElement = JsonParser.parseString(content.toString());
                return jsonElement.getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
            } else {
                logger.error("Failed to translate text. HTTP error code: {}", responseCode);
                return null; // or a predefined error message that doesn't expose internal details
            }
        } catch (IOException e) {
            logger.error("Exception occurred during translation", e);
            return null; // or a predefined error message
        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
