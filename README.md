# Discord File Filter Bot

A powerful Discord bot for preventing uploads of unwanted file attachments.

## Features

- **WhiteList**
  - Allow only specified file types

- **BlackList**
  - Block only specified file types

## Setup

1. Clone this repository
2. Rename `config.json.example` to `config.json` and fill in your bot's information:
   ```json
    {
        "token": "YOUR_BOT_TOKEN_HERE",
        "application_id": YOUR_APPLICATION_ID_HERE,
        "isWhitelist": true,
        "blacklisted_file_types": [
                "pdf",
                "exe",
                "sh",
                "cmd",
                "zip",
                "7zip",
                "tar"
        ],
        "whitelisted_file_types": [
                "mp4",
                "mov",
                "avi",
                "jpeg",
                "mp3",
                "gif"
        ]
    }
   ```
3. Build the project with Maven:
   ```
   mvn clean package
   ```
4. Run the bot:
   ```
   java -jar target/file-filter-bot-1.0-SNAPSHOT.jar
   ```

## Commands

### BlackList Management
- `/blacklist <fileType>` - Blocks messages with specified file type
- `/unblacklist <fileType>` - Removes file type from blacklist

### WhiteList Management
- `/whitelist <fileType>` - Blocks messages without specified file type
- `/unwhitelist <fileType>` - Removes file type from whitelist


## Requirements

- Java 11 or higher
- Discord Bot with proper permissions

## License

This project is licensed under the MIT License - see the LICENSE file for details.
