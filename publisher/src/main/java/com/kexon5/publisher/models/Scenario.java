package com.kexon5.publisher.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TEST_SCENARIOS")
public class Scenario {

    @Id
    private ObjectId id;

    @Field("NAME")
    private String name;

    @Field("STEPS")
    private List<ScenarioStep> steps = new ArrayList<>();


    public Scenario(String name) {
        this.name = name;
    }

    public Scenario(String name, List<ScenarioStep> steps) {
        this.name = name;
        this.steps.addAll(steps);
    }

    public void addStep(ScenarioStep scenarioStep) {
        steps.add(scenarioStep);
    }


    public Scenario replaceId(long oldId, long newId) {
        Scenario newScenario = new Scenario(name, steps);

        newScenario.steps = newScenario.steps.stream()
                                             .map(step -> step.replaceId(oldId, newId))
                                             .toList();

        return newScenario;
    }

}
