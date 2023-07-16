package com.kexon5.bot.models.hospital;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "BACKUP_HOSPITALS")
public class HospitalBackup {

    @Id
    private ObjectId id;

    @Field("HOSPITAL")
    private Hospital hospital;
    @Field("DATE")
    private LocalDateTime date;

    public HospitalBackup(Hospital hospital) {
        this.hospital = hospital;
        this.date = LocalDateTime.now();
    }

}
