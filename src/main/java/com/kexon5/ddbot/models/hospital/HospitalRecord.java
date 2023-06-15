package com.kexon5.ddbot.models.hospital;

import com.kexon5.ddbot.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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


    public enum RecordState {
        READY,
        OPEN,
        CLOSED,
        OUTDATED
    }

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
    @EqualsAndHashCode.Exclude
    private Set<ObjectId> users = new HashSet<>();
    @Field("STATE")
    @EqualsAndHashCode.Exclude
    private RecordState state;
    @Field("RECORD_HASH")
    @EqualsAndHashCode.Exclude
    private int recordHash;

    public HospitalRecord(List<Object> data, LocalDate date) {
        if (data.size() > 2) {
            this.date = LocalDateTime.of(date, LocalTime.parse(data.get(0).toString(), DateTimeFormatter.ofPattern("H:mm")));
            this.venue = data.get(1).toString();
            this.hospital = data.get(2).toString();
            this.type = data.get(3).toString();
            this.peopleCount = Integer.parseInt(data.get(4).toString());
        } else {
            this.date = LocalDateTime.of(date, LocalTime.parse(data.get(0).toString(), DateTimeFormatter.ofPattern("H:mm")));
            this.venue = "Студклуб";
            this.hospital = "Студклуб";
            this.type = "Обычный"; // TODO: 10.06.2023
            this.peopleCount = Integer.parseInt(data.get(1).toString());
        }

        this.state = RecordState.READY;
        this.recordHash = hashCode();
    }

    public HospitalRecord(List<Object> data) {
        LocalDate date = LocalDate.parse(data.get(0).toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalTime time = LocalTime.parse(data.get(1).toString(), DateTimeFormatter.ofPattern("H:mm"));
        this.date = LocalDateTime.of(date, time);
        this.venue = data.get(2).toString();
        this.hospital = data.get(3).toString();
        this.type = data.get(4).toString();
        this.peopleCount = Integer.parseInt(data.get(5).toString());

        this.state = RecordState.READY;
        this.recordHash = hashCode();
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

    public boolean addUser(User user) {
        user.setActiveRecord(id);
        return users.add(user.getId());
    }

    public boolean removeUser(User user) {
        user.setActiveRecord(null);
        return users.remove(user.getId());
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
        return hospital + " - " + getDateTimeForButton();
    }

    public String toDate() {
        return "" + date.toLocalTime();
    }

}
