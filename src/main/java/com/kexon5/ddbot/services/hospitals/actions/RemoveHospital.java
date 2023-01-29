package com.kexon5.ddbot.services.hospitals.actions;

import com.kexon5.ddbot.markup.MarkupList;
import com.kexon5.ddbot.models.Location;
import com.kexon5.ddbot.services.actions.AbstractAction;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class RemoveHospital extends AbstractAction {
    private enum RemoveSteps implements ActionSteps {
        NAME() {
            public String getMsg() {
                return "Введите номер ОПК в списке, которое хотите удалить:\n" +
                        new MarkupList<>(hospitals.stream().map(Location::getPlaceName).toList());
            }

            public String handle(String text) {
                try {
                    index = Integer.parseInt(text) - 1;
                    return index < hospitals.size() && index >= 0
                            ? null
                            : "Некорректный индекс";
                } catch (Exception e) {
                    return "Число введи, баран";
                }
            }
        },
        ACCEPT() {
            public String getMsg() {
                return String.format("Вы точно хотите удалить - \"%s\"?", hospitals.get(index).getPlaceName());
            }
        },
        FINAL() {
            public String getMsg() {
                return "Successfully deleted";
            }
        };

        private static int index;
        @Setter
        private static List<Location> hospitals = new ArrayList<>();

    }

    public RemoveHospital(List<Location> hospitals) {
        RemoveSteps.hospitals = hospitals;
    }

    @Override
    public String getButtonText() {
        return "Удалить ОПК";
    }

    @Override
    public void actionResult() {
        RemoveSteps.hospitals.remove(RemoveSteps.index);
    }

    @Override
    public ActionSteps[] actionSteps() {
        return RemoveSteps.values();
    }

}
