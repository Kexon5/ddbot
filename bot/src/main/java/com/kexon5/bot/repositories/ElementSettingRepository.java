package com.kexon5.bot.repositories;

import com.kexon5.bot.models.ElementSetting;
import com.kexon5.common.statemachine.ButtonReply;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.kexon5.bot.bot.states.ServiceState.*;

public interface ElementSettingRepository extends MongoRepository<ElementSetting, ObjectId> {

    List<ElementSetting> findAllByType(ElementSetting.Type type);
    ElementSetting findByElementName(String elementName);

    boolean existsByElementName(String elementName);

    default boolean isWorking(String elementName) {
        return findByElementName(elementName).isWorking();

    }

    default void createNew(String elementName, ElementSetting.Type type) {
        ElementSetting newElementSetting = new ElementSetting(elementName, type);
        save(newElementSetting);
    }


    default List<ElementSetting> getServiceSettings() {
        Set<String> removeSet = Set.of(MAIN_MENU, ADMINISTRATION_MENU, SERVICE_SWITCHER_MENU, ACTION_SWITCHER_MENU).stream()
                                   .map(Enum::name)
                                   .collect(Collectors.toSet());

        List<ElementSetting> serviceSettings = findAllByType(ElementSetting.Type.SERVICE);

        serviceSettings.removeIf(element -> removeSet.contains(element.getElementName()));

        return serviceSettings;
    }

    default List<ElementSetting> getActionSettings() {
        return findAllByType(ElementSetting.Type.ACTION);
    }

    default <T extends Enum> List<ButtonReply.ButtonReplyBuilder> getBuilders(List<ElementSetting> elementSettings, T element) {
        AtomicInteger counter = new AtomicInteger();

        return elementSettings.stream()
                              .map(elementSetting ->
                                           ButtonReply.builder(element.name(), counter.getAndIncrement())
                                                      .buttonChange((index, id) -> save(elementSetting.inverseWorking()))
                              )
                              .toList();
    }
}
