package com.discordbot.fileFilterBot.commands;

import com.discordbot.fileFilterBot.manager.ConfigManager;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public class BlacklistCommand {
    private final ConfigManager configManager;

    public BlacklistCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public static ApplicationCommandRequest getCommandRequest() {
        return ApplicationCommandRequest.builder()
                .name("blacklistfiletype")
                .description("Blacklist a file type")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("file-type")
                        .description("The file type to add to blacklist")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    public Mono<Void> handle(ChatInputInteractionEvent event) {
        String fileType = event.getOption("file-type")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .orElse("");
        if (fileType.isEmpty()) {
            return event.reply("Please specify a file type.").withEphemeral(true); // Responds privately
        }

        configManager.addToBlackList(fileType);


        return event.reply()
                .withEphemeral(true)
                .withContent(fileType + " added to blacklist");
    }
}