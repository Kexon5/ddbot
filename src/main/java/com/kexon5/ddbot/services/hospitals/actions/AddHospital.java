package com.kexon5.ddbot.services.hospitals.actions;

import com.kexon5.ddbot.markup.MarkupList;
import com.kexon5.ddbot.models.Location;
import com.kexon5.ddbot.services.actions.AbstractAction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AddHospital extends AbstractAction {
    @RequiredArgsConstructor
    public enum AddSteps implements ActionSteps {
        NAME("Введите название ОПК") {
            public String handle(String text) {
                builder.placeName(text);
                return null;
            }
        },
        COORDS("Введите десятичные координаты этой ОПК") {
            public String handle(String text) {
                String[] coords = text.split(" ");
                if (coords.length == 2) {
                    builder.longitude(Double.parseDouble(coords[0]));
                    builder.latitude(Double.parseDouble(coords[1]));
                }
                return null;
            }
        },
        HOW_TO_GET("Инструкция как добраться (Пожалуйста, одним сообщением)") {
            public String handle(String text) {
                builder.howToGet(new MarkupList<>(Arrays.stream(text.split("\n")).toList()));
                return null;
            }
        },
        IMPORTANT_INFO("Какие-то ещё важные вещи? Документы, возможная специфика ОПК (Так же одним сообщением") {
            public String handle(String text) {
                builder.importantInfo(text);
                return null;
            }
        },
        FINAL("Successfully added");

        @Getter
        private final String msg;

        private static final Location.LocationBuilder builder = Location.builder();

    }
    private final Consumer<Location> addHospital;

    public AddHospital(List<Location> hospitals) {
        this.addHospital = hospitals::add;
    }

    @Override
    public String getButtonText() {
        return "Добавить ОПК";
    }

    @Override
    public void actionResult() {
        addHospital.accept(AddSteps.builder.build());
    }

    @Override
    public ActionSteps[] actionSteps() {
        return AddSteps.values();
    }

}
