package com.kexon5.common.statemachine;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.function.BiFunction;


public interface InteractiveButtonFactory extends BiFunction<Long, List<String>, List<InlineKeyboardButton>> {}
