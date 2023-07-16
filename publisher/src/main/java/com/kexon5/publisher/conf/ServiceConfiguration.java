package com.kexon5.publisher.conf;

import com.kexon5.publisher.models.Scenario;
import com.kexon5.publisher.models.TestScenario;
import com.kexon5.publisher.repositories.ScenarioRepository;
import com.kexon5.publisher.service.UpdateBenchmarkService;
import com.kexon5.publisher.service.UpdateScenarioService;
import com.kexon5.publisher.service.UpdateSenderService;
import com.kexon5.publisher.service.UpdateTestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ServiceConfiguration {

    @Bean
    @Profile("standard")
    public UpdateSenderService updateSenderService() {
        return new UpdateSenderService();
    }

    @Bean
    @Profile("scenario")
    public UpdateScenarioService updateScenarioService(ScenarioRepository scenarioRepository,
                                                       @Value("${scenario.name}") String scenarioName) {
        return new UpdateScenarioService(scenarioRepository, new Scenario(scenarioName));
    }


    @Bean
    @Profile("test")
    public UpdateTestService updateTestService(ScenarioRepository scenarioRepository,
                                               @Value("${scenario.name}") String scenarioName,
                                               @Value("${sender.count}") int senderCount) {
        Scenario scenario = scenarioRepository.findByName(scenarioName);
        Map<Long, TestScenario> testScenarioMap = new ConcurrentHashMap<>();
        for (long i = 1; i <= senderCount; i++) {
            testScenarioMap.put(i, new TestScenario(scenario, i));
        }

        return new UpdateTestService(testScenarioMap);
    }

    @Bean
    @Profile("benchmark")
    public UpdateBenchmarkService updateBenchmarkService() {
        return new UpdateBenchmarkService();
    }

}
