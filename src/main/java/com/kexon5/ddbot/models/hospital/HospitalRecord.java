package com.kexon5.ddbot.models.hospital;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "HOSPITAL_RECORDS")
public class HospitalRecord implements Comparable<HospitalRecord> {
    public static final DateTimeFormatter DATE_AND_TIME = DateTimeFormatter.ofPattern("dd.MM (E), HH:mm");
    private static final Comparator<HospitalRecord> COMPARATOR = Comparator.comparing(HospitalRecord::getDate);

    @Id
    private ObjectId id;
    @Field("HOSPITAL_NAME")
    private String hospital;
    @Field("VENUE")
    private String venue;
    @Field("TIME")
    private LocalDateTime date;
    @Field("TYPE")
    private String type;
    @Field("PEOPLE_COUNT")
    private int peopleCount;
    @Field("USERS")
    private Set<Long> users = new HashSet<>();

    private static final List<Object> savedSchema = List.of("Время", "Место сбора", "Место", "Тип выезда", "Кол-во человек");

    public HospitalRecord(List<Object> data, LocalDate date) {
        this.date = LocalDateTime.of(date, LocalTime.parse(data.get(0).toString(), DateTimeFormatter.ofPattern("H:mm")));
        this.venue = data.get(1).toString();
        this.hospital = data.get(2).toString();
        this.type = data.get(3).toString();
        this.peopleCount = Integer.parseInt(data.get(4).toString());
    }

    public static boolean checkSchema(List<Object> currentSchema) {
        if (currentSchema.size() != savedSchema.size()) return false;
        return Objects.equals(currentSchema, savedSchema);
    }

    public LocalDate getLocalDate() {
        return date.toLocalDate();
    }

    public boolean hasPlace() {
        return users.size() < peopleCount;
    }

    @Override
    public int compareTo(HospitalRecord o) {
        return COMPARATOR.compare(this, o);
    }

    public String getDateTimeForButton() {
        return date.format(DATE_AND_TIME);
    }

    public boolean addUser(long userId) {
        return users.add(userId);
    }

    public boolean removeUser(long userId) {
        return users.remove(userId);
    }

    private String getTypeToString() {
        return Objects.equals(type, "Обычный")
                ? "" : " - " + type;
    }

    private String getAdminPart() {
        return getTypeToString()
                + " (Место встречи: " + venue + ", Число человек: " + peopleCount + ")";
    }

    public String toAdminString() {
        return toCommonString() + getAdminPart();
    }

    public String toCommonString() {
        return hospital + " - " + date.toLocalTime();
    }

    public String toDate() {
        return "" + date.toLocalTime();
    }


}
