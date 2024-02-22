# OnichanBot

OnichanBot is an IRC bot designed to enhance the interaction within IRC channels through a variety of commands and services. It can search YouTube, display the most recent song played by a Last.fm user, fetch definitions from Urban Dictionary, and translate text using the DeepL API.

## Features

- **YouTube Video Search**: Users can search for YouTube videos with `.yt <search term>`.
- **Last.fm Now Playing**: Displays the current song a specified Last.fm user is listening to with `.np <last.fm username>`.
- **Urban Dictionary Search**: Fetches definitions from Urban Dictionary for a given term with `.ud <search term>`.
- **DeepL Text Translation**: Translates text to a specified language using `.deepl <language code> <text to translate>`.

## Dependencies

OnichanBot is built with Maven and depends on the following libraries:

- **PircBotX**: For IRC bot functionalities.
- **Logback-classic**: For logging capabilities.
- **Gson**: For handling JSON data.
- **Google HTTP Client Libraries**: For making HTTP requests.
- **OkHttp3**: For making efficient HTTP requests.
- **Jsoup**: For HTML parsing.
- **HtmlUnit**: For web page rendering and interaction.
- **PrettyTime**: For human-readable time formatting.
- **PostgreSQL JDBC Driver**: For database connectivity.
- **Commons-Text**: For text operations.
- **Last.fm Java Library**: For interacting with the Last.fm API.
- **YouTube Data API**: For accessing YouTube services.
- **Google HTTP Client Jackson2**: For processing JSON with Jackson 2.x.

Ensure you have Maven installed and run `mvn clean install` to resolve and download all dependencies specified in the `pom.xml`.

## Configuration

To get OnichanBot up and running, a `config3.properties` file is required in the same directory as the bot. This file contains essential configurations that the bot needs to operate correctly. Below is an explanation of each configuration section and how to modify it for your use.

### API Keys (Omit actual keys)

- `yt.apiKey`= Your YouTube Data API key.
- `lfm.apiKey`= Your Last.fm API key.
- `ud.apiKey`= Your Urban Dictionary API key.
- `yd.apiKey`= Your DeepL API key.

**Note:** For security reasons, do not share or expose your API keys in public repositories or forums.

### PostgreSQL Database Settings

- `db.url`= The JDBC URL for your PostgreSQL database.
- `user`= The username for database access.
- `password`= The password for the database user.

### IRC Server Settings

- `bot.name`= The name your bot will use on IRC.
- `bot.admin`= The nickname of the bot's admin user. This user can execute admin-only commands.
- `server.name`= The hostname of the IRC server you wish to connect to.
- `server.port`= The port number of the IRC server.
- `channel.name`= The name of the channel your bot should join automatically.
- `nickserv.pw`= Your bot's NickServ password, it should use to register and auth itself.
- `nickserv.email`= The email associated with your bot's NickServ account.

**Important**: Ensure that all sensitive information, such as passwords and API keys, is kept confidential and secure. Do not include actual passwords or API keys in the `config3.properties` file when sharing your bot's code or configuration files.

### How to Use

1. **Edit the `config3.properties` file**: Fill in the required fields with your specific configuration values. Be sure to replace placeholder values with actual data without exposing sensitive information.

2. **Place the file in the bot's directory**: Ensure that `config3.properties` is located in the same directory as your bot's executable or main class file.

3. **Start your bot**: When launching OnichanBot, it will automatically read and apply the settings from `config3.properties`. Ensure the bot has permission to read the configuration file.

By following these steps and correctly setting up your `config3.properties` file, OnichanBot will be configured to connect to the desired IRC server, join the specified channel, and utilize the defined services through their respective APIs.

## Database Setup

### Creating the `lastfmnames` Table

To store IRC usernames and their corresponding Last.fm usernames, you need to create a table in your PostgreSQL database. Use the SQL statement below to create the `lastfmnames` table:

```sql
CREATE TABLE public.lastfmnames (
    id serial PRIMARY KEY,
    username character varying(50) NOT NULL UNIQUE,
    lastfm_username character varying(50)
);
```

## Acknowledgments

A big thank you to my dear friends that help me with my little projects.

## Contact

For any questions or feedback, please contact me or open an issue on GitHub.
