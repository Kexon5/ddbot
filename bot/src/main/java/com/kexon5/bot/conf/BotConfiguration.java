package com.kexon5.bot.conf;

import com.kexon5.bot.bot.DDBot;
import com.kexon5.bot.bot.services.mainmenu.MainMenuService;
import com.kexon5.bot.conf.statemachine.ServiceConfiguration;
import com.kexon5.bot.listeners.BotFinishListener;
import com.kexon5.bot.listeners.BotStartListener;
import com.kexon5.bot.services.MailingService;
import com.kexon5.bot.services.MethodUnicaster;
import com.kexon5.common.models.ActiveEnvironment;
import com.kexon5.common.repositories.ActiveEnvironmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.ReplyCollection;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Configuration
@AutoConfigureAfter(ServiceConfiguration.class)
public class BotConfiguration  {

    @Bean
    public ActiveEnvironment activeEnvironment(@Value("${env:dev}") String env) {
        return ActiveEnvironment.builder()
                                .env(env)
                                .isMain(true)
                                .build();
    }

    @Bean
    public BotStartListener botStartListener(ActiveEnvironment activeEnvironment,
                                             ActiveEnvironmentRepository activeEnvironmentRepository) {
        return new BotStartListener(activeEnvironment, activeEnvironmentRepository);
    }

    @Bean
    public BotFinishListener botFinishListener(ActiveEnvironment activeEnvironment,
                                               ActiveEnvironmentRepository activeEnvironmentRepository) {
        return new BotFinishListener(activeEnvironment, activeEnvironmentRepository);
    }

    @Bean
    public DefaultBotOptions options(@Value("${publisher.address}") String pubAddress) {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setBaseUrl(pubAddress);
        return options;
    }

    @Bean
    public MethodUnicaster methodUnicaster() {
        return new MethodUnicaster();
    }

    @Bean
    public DDBot ddBot(@Value("${bot.username}") String botName,
                       DBContext dbContext,
                       DefaultBotOptions options,
                       ReplyCollection actionReplyCollection,
                       MainMenuService mainMenu,
                       MethodUnicaster methodUnicaster) {
        DDBot bot = new DDBot(botName, options, dbContext, actionReplyCollection, mainMenu, methodUnicaster);
        bot.onRegister();
        return bot;
    }

    @Bean
    public SilentSender silentSender(MailingService mailingService,
                                     DDBot bot) {
        SilentSender sender = bot.silent();
        mailingService.setSender(sender);
        return sender;
    }

}
