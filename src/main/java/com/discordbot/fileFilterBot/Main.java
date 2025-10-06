package com.discordbot.fileFilterBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.discordbot.fileFilterBot.commands.BlacklistCommand;
import com.discordbot.fileFilterBot.commands.UnblacklistCommand;
import com.discordbot.fileFilterBot.commands.UnwhitelistCommand;
import com.discordbot.fileFilterBot.commands.WhitelistCommand;
import com.discordbot.fileFilterBot.manager.ConfigManager;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import reactor.core.publisher.Mono;

public class Main {
	private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static ConfigManager configManager;

    public static void main(String[] args) {
        configManager = new ConfigManager();
        if (!configManager.load()) {
            LOGGER.severe("Failed to load configuration. Exiting.");
            return;
        }
        
        final GatewayDiscordClient client = DiscordClientBuilder.create(configManager.getToken())
                .build()
                .login()
                .block();
                
        if (client == null) {
            LOGGER.severe("Failed to initialize Discord client. Check your token. Exiting.");
            return;
        }
        
        client.updatePresence(ClientPresence.online(ClientActivity.playing("File Type Monitor")))
                .subscribe();
                
        registerCommands(client.getRestClient(), configManager.getApplicationId());
        
        registerCommandHandlers(client);
        
        registerEventListeners(client);
        
        client.onDisconnect().block();
    }
    
    private static void registerCommands(RestClient restClient, long applicationId) {
        List<ApplicationCommandRequest> commandRequests = new ArrayList<>();

        commandRequests.add(BlacklistCommand.getCommandRequest());
        commandRequests.add(UnblacklistCommand.getCommandRequest());
        commandRequests.add(WhitelistCommand.getCommandRequest());
        commandRequests.add(UnwhitelistCommand.getCommandRequest());
        
        LOGGER.info("Registering " + commandRequests.size() + " commands");
        restClient.getApplicationService()
                .bulkOverwriteGlobalApplicationCommand(applicationId, commandRequests)
                .subscribe();
    }
    
    private static void registerCommandHandlers(GatewayDiscordClient client) {
        client.on(ChatInputInteractionEvent.class, event -> {
            String commandName = event.getCommandName();
            
            switch (commandName) {
                case "blacklistfiletype":
                    return new BlacklistCommand(configManager).handle(event);
                case "unblacklistfiletype":
                    return new UnblacklistCommand(configManager).handle(event);
                case "whitelistfiletype":
                    return new WhitelistCommand(configManager).handle(event);
                case "unwhitelistfiletype":
                    return new UnwhitelistCommand(configManager).handle(event);
                default:
                    return event.reply("Unknown command: " + commandName)
                            .withEphemeral(true);
            }
        }).subscribe();
    }
    
    private static void registerEventListeners(GatewayDiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class)
            .flatMap(event -> {
                Message message = event.getMessage();
                if (message.getAttachments().isEmpty()) return Mono.empty();
                processAttachments(event);
                return Mono.empty();
            })
        .subscribe();
        
        client.on(ButtonInteractionEvent.class, Main::handleButtonInteraction)
        .subscribe();
    }

    private static void processAttachments(MessageCreateEvent event) {
        Message message = event.getMessage();
    	List<Attachment> attachments = message.getAttachments();
        boolean isWhitelist = configManager.isWhitelist();
        Set<String> WHITELISTED_EXTENSIONS = configManager.getWhiteListedFileTypes();
        Set<String> BLACKLISTED_EXTENSIONS = configManager.getBlackListedFileTypes();
        for (Attachment attachment : attachments) {
            String fileExtension = getFileExtension(attachment.getFilename());
            if (isWhitelist) {
                if (WHITELISTED_EXTENSIONS.contains(fileExtension)) return;
                message.delete().block();
                sendMessage(message, "Message deleted due to file type of " + fileExtension + " not being whitelisted");
                return;
            }
            if (BLACKLISTED_EXTENSIONS.contains(fileExtension)) {
                message.delete().block();
                sendMessage(message, "Message deleted due to blacklisted file type: " + fileExtension);
            }
        }
    }

    private static void sendMessage(Message message, String string) {
        User author = message.getAuthor().orElse(null);
    	if (author != null && !author.isBot()) {
            Button dismissButton = Button.primary("dismiss", "Dismiss");
            MessageChannel channel = message.getChannel().block();

            Message m = channel.createMessage(
                    MessageCreateSpec.builder()
                    .content(string)
                    .addComponent(ActionRow.of(dismissButton))
                    .build()
                ).block();
            
            Runnable task = () -> {
                m.delete().block();
            };
    		
    		try {
				executor.schedule(task, 5, TimeUnit.SECONDS);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
    }
    
    private static String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1 || lastIndexOfDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastIndexOfDot + 1).toLowerCase();
    }
    
    public static Mono<Void> handleButtonInteraction(ButtonInteractionEvent interaction) {
        if ("dismiss".equals(interaction.getCustomId())) {
            interaction.reply("Message dismissed!").withEphemeral(true).subscribe();
            interaction.getMessage().ifPresent(message -> {
                message.delete().subscribe();
            });
        }
        return Mono.empty();
    }
}