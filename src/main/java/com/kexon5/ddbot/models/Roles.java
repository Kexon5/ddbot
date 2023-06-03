package com.kexon5.ddbot.models;

import java.util.Map;
import java.util.Set;

public enum Roles {
    DONOR,
    VOLUNTEER,
    HEAD,
    MAIN_HEAD,
    ADMIN;


    public static Set<Roles> donor = Set.of(DONOR);
    public static Set<Roles> volunteer = Set.of(DONOR, VOLUNTEER);
    public static Set<Roles> head = Set.of(DONOR, VOLUNTEER, HEAD);
    public static Set<Roles> mainHead = Set.of(DONOR, VOLUNTEER, HEAD, MAIN_HEAD);
    public static Set<Roles> admin = Set.of(values());


    public static Map<Roles, Set<Roles>> rolesMap = Map.of(
            DONOR, donor,
            VOLUNTEER, volunteer,
            HEAD, head,
            MAIN_HEAD, mainHead,
            ADMIN, admin
    );

}
