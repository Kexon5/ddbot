package com.kexon5.publisher.service;

import com.kexon5.publisher.models.TestScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class UpdateTestService extends UpdateService {

    private final Map<Long, TestScenario> testScenarios;

    private final Map<Long, List<Long>> scenarioReplyTimes = new HashMap<>();


    private static Duration getAverage(List<Long> durationMillis) {
        long average = (long) durationMillis.stream()
                                            .mapToLong(a -> a)
                                            .average()
                                            .orElse(0.);

        return Duration.ofMillis(average);
    }


    @Scheduled(initialDelay = 5L, fixedDelay = 2L, timeUnit = TimeUnit.SECONDS)
    public void test() {
        if (testScenarios.isEmpty()) {
            scenarioReplyTimes.forEach((id, durationList) -> log.info("[{}] average answer elapsed time {}", id,
                                                                      getAverage(durationList)));
            System.exit(0);
        }

        List<Update> updates = testScenarios.values().stream().map(TestScenario::getNext).toList();

        updates.forEach(upd -> {
            if (upd != null) {
                if (upd.getUpdateId() < 0) {
                    Long id = (long) -upd.getUpdateId();
                    scenarioReplyTimes.put(id, testScenarios.get(id).getReplyTimes());
                    testScenarios.remove(id);
                } else {
                    addUpdate(upd);
                }
            }
        });

    }

    @Override
    public void addUpdate(Update update) {
        Optional.ofNullable(getUserEnv(update))
                .ifPresentOrElse(env -> env2UpdateUnicast.get(env).onNext(update), () -> envUnavailable(update, false));
    }

    private String getStringId(String method) {
        return method.substring(12, method.indexOf('\"', 12));
    }

    private long getId(String method) {
        return Long.parseLong(getStringId(method));
    }

    @Override
    public Optional<String> execute(String method, String path) {
        return testScenarios.get(getId(method)).execute(method);
    }

}
