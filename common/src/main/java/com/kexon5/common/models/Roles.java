package com.kexon5.common.models;

import java.util.Map;
import java.util.Set;

public enum Roles {
    DONOR,
    VOLUNTEER,
    HEAD,
    MAIN_HEAD,
    ADMIN;


    public static final Set<Roles> donor = Set.of(DONOR);
    public static final Set<Roles> volunteer = Set.of(DONOR, VOLUNTEER);
    public static final Set<Roles> head = Set.of(DONOR, VOLUNTEER, HEAD);
    public static final Set<Roles> mainHead = Set.of(DONOR, VOLUNTEER, HEAD, MAIN_HEAD);
    public static final Set<Roles> admin = Set.of(values());


    public static final Map<Roles, Set<Roles>> rolesMap = Map.of(
            DONOR, donor,
            VOLUNTEER, volunteer,
            HEAD, head,
            MAIN_HEAD, mainHead,
            ADMIN, admin
    );

}
