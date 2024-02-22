package moe.herz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URISyntaxException;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class DeepLService {

    private String apiKey;

    public DeepLService(Config config) {
        this.apiKey = config.getdeeplapiKey();
    }

    public String translateText(String text, String targetLanguage) throws IOException, URISyntaxException {
        String apiUrl = "https://api-free.deepl.com/v2/translate";
        String charset = "UTF-8";
        String encodedText = URLEncoder.encode(text, charset);
        String requestUrl = apiUrl + "?auth_key=" + apiKey + "&target_lang=" + targetLanguage + "&text=" + encodedText;

        URI uri = new URI(requestUrl);
        URL url = uri.toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
            connection.disconnect();
            // Parse JSON to extract translated text
            JsonElement jsonElement = JsonParser.parseString(content.toString());
            return jsonElement.getAsJsonObject().get("translations").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        } else {
            return "Error: " + responseCode;
        }
    }
}

