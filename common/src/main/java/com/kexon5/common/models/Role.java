package com.kexon5.common.models;

import java.util.Map;
import java.util.Set;

public enum Role {
    DONOR,
    VOLUNTEER,
    HEAD,
    MAIN_HEAD,
    ADMIN;


    public static final Set<Role> donor = Set.of(DONOR);
    public static final Set<Role> volunteer = Set.of(DONOR, VOLUNTEER);
    public static final Set<Role> head = Set.of(DONOR, VOLUNTEER, HEAD);
    public static final Set<Role> mainHead = Set.of(DONOR, VOLUNTEER, HEAD, MAIN_HEAD);
    public static final Set<Role> admin = Set.of(values());


    public static final Map<Role, Set<Role>> rolesMap = Map.of(
            DONOR, donor,
            VOLUNTEER, volunteer,
            HEAD, head,
            MAIN_HEAD, mainHead,
            ADMIN, admin
    );

}
