package com.kexon5.ddbot.services;

import com.kexon5.ddbot.bot.DDBot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TaskManager {

    private final PriorityQueue<Task> queue = new PriorityQueue<>(Task.COMPARATOR);

    private final DDBot bot;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public void init() {
        executorService.scheduleAtFixedRate(this::runTask, 0, 10, TimeUnit.MICROSECONDS);
    }

    public synchronized void runTask() {
        while (true) {
            if (queue.isEmpty()) break;

            Task task = queue.poll();
            if (task.needToExecute()) {
                task.accept(bot);
                Optional.ofNullable(task.nextTime()).ifPresent(queue::add);
            } else {
                queue.add(task);
                break;
            }
        }
    }

    public boolean addBeforeTask(Consumer<BaseAbilityBot> task,
                                 LocalDateTime timePoint,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        return queue.add(new Task(task, timePoint.minus(timeShift, temporalUnit)));
    }


    public boolean addAfterTask(Consumer<BaseAbilityBot> task,
                                LocalDateTime timePoint,
                                long timeShift,
                                TemporalUnit temporalUnit) {
        return queue.add(new Task(task, timePoint.plus(timeShift, temporalUnit)));
    }

    public boolean addRepeatableTask(Consumer<BaseAbilityBot> task,
                                     long timeShift,
                                     TemporalUnit temporalUnit) {
        return queue.add(new Task(task, LocalDateTime.now(), timeShift, temporalUnit));
    }

    public boolean removeById(int taskId) {
        return queue.removeIf(task -> task.getId() == taskId);
    }


    @Getter
    @AllArgsConstructor
    public static class Task implements Comparable<Task> {

        private static final Comparator<Task> COMPARATOR = Comparator.comparing(Task::getTime);

        private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

        private final int id = ID_GENERATOR.getAndIncrement();
        private final Consumer<BaseAbilityBot> task;
        private LocalDateTime time;

        private long timeShift = 0L;
        @Nullable
        private TemporalUnit temporalUnit;

        public Task(Consumer<BaseAbilityBot> task, LocalDateTime time) {
            this.task = task;
            this.time = time;
        }

        public Task nextTime() {
            if (!isRepeatableTask()) return null;

            time = time.plus(timeShift, temporalUnit);
            return this;
        }

        public boolean isRepeatableTask(){
            return timeShift != 0L && temporalUnit != null;
        }

        public boolean needToExecute() {
            return time.isBefore(LocalDateTime.now());
        }

        public void accept(BaseAbilityBot bot) {
            task.accept(bot);
        }

        @Override
        public int compareTo(@NotNull Task o) {
            return COMPARATOR.compare(this, o);
        }
    }

}
