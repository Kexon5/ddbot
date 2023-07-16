package com.kexon5.publisher.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioStep {

    private Update update;
    private List<String> answers = new ArrayList<>();

    public ScenarioStep(Update update) {
        this.update = update;
    }

    public void addAnswer(String answer) {
        answers.add(answer);
    }


    public ScenarioStep replaceId(long oldId, long newId) {
        ScenarioStep newStep = new ScenarioStep(SerializationUtils.clone(update));

        setChatId(newStep.update, newId);
        newStep.answers = answers.stream()
                                 .map(answer -> answer.replace(String.valueOf(oldId), String.valueOf(newId)))
                                 .toList();

        return newStep;
    }

    public static void setChatId(Update update, long newId) {
        if (MESSAGE.test(update)) {
            update.getMessage().getChat().setId(newId);
            update.getMessage().getFrom().setId(newId);
        } else if (CALLBACK_QUERY.test(update)) {
            update.getCallbackQuery().getMessage().getChat().setId(newId);
            update.getCallbackQuery().getMessage().getFrom().setId(newId);
        }
    }

}
