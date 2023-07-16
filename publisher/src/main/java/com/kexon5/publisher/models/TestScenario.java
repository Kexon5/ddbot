package com.kexon5.publisher.models;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class TestScenario {

    private final Scenario scenario;
    private final long id;


    private List<String> activeCheckList;
    private int step = -1;
    boolean stepEnded = true;
    private int indexList;

    private long startTime;

    @Getter
    private List<Long> replyTimes = new ArrayList<>();


    public TestScenario(Scenario scenario, long id) {
        this.scenario = scenario.replaceId(825648974, id);
        this.id = id;
    }


    public Update getNext() {
        if (!stepEnded) return null;

        stepEnded = false;
        step++;

        if (step >= scenario.getSteps().size()) {
            Update end = new Update();
            end.setUpdateId((int) -id);
            return end;
        }

        indexList = 0;
        ScenarioStep scenarioStep = scenario.getSteps().get(step);
        activeCheckList = scenarioStep.getAnswers();

        setStartTime();

        return scenarioStep.getUpdate();
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }


    public Optional<String> execute(String method) {
        if (!activeCheckList.get(indexList).equals(method)) {
            log.error("[{}] Test error on {} step", id, step);
        }

        indexList++;
        if (activeCheckList.size() <= indexList + 1) {
            stepEnded = true;
        }

        replyTimes.add(System.currentTimeMillis() - startTime);

        return Optional.of(activeCheckList.get(indexList++));
    }
}
