package com.kexon5.publisher.service;

import com.kexon5.publisher.models.Scenario;
import com.kexon5.publisher.models.ScenarioStep;
import com.kexon5.publisher.repositories.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@RequiredArgsConstructor
public class UpdateScenarioService extends UpdateService {

    private final ScenarioRepository scenarioRepository;
    private final Scenario scenario;
    private ScenarioStep scenarioStep;


    @Override
    public void addUpdate(Update update) {
        if (scenarioStep != null) {
            scenario.addStep(scenarioStep);
            scenarioRepository.save(scenario);
        }
        scenarioStep = new ScenarioStep(update);

        Optional.ofNullable(getUserEnv(update))
                .ifPresentOrElse(env -> env2UpdateUnicast.get(env).onNext(update), () -> envUnavailable(update, false));
    }

    @Override
    public Optional<String> execute(String method, String path) {
        scenarioStep.addAnswer(method);
        Optional<String> message = sender.execute(method, path);
        message.ifPresent(msg -> scenarioStep.addAnswer(msg));
        return message;
    }

}
