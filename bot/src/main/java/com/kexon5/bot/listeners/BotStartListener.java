package com.kexon5.bot.listeners;

import com.kexon5.common.models.ActiveEnvironment;
import com.kexon5.common.repositories.ActiveEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

@RequiredArgsConstructor
public class BotStartListener implements ApplicationListener<ApplicationStartedEvent> {

    private final ActiveEnvironment activeEnvironment;
    private final ActiveEnvironmentRepository activeEnvironmentRepository;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (activeEnvironmentRepository.findByEnv(activeEnvironment.getEnv()).isPresent()) {
            throw new RuntimeException("Current env already working");
        }

        activeEnvironmentRepository.save(activeEnvironment);
    }
}
