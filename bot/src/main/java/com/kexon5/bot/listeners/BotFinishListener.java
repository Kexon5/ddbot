package com.kexon5.bot.listeners;

import com.kexon5.common.models.ActiveEnvironment;
import com.kexon5.common.repositories.ActiveEnvironmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

@RequiredArgsConstructor
public class BotFinishListener implements ApplicationListener<ContextClosedEvent> {

    private final ActiveEnvironment activeEnvironment;
    private final ActiveEnvironmentRepository activeEnvironmentRepository;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        activeEnvironmentRepository.deleteByEnv(activeEnvironment.getEnv());
    }
}
