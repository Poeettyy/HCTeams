package net.frozenorb.foxtrot.teamactiontracker.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TeamActionType {

    GENERAL("general"),
    KILLS("kills"),
    CONNECTIONS("connections"),
    TEAM_CHAT("teamChat"),
    ALLY_CHAT("allyChat");

    @Getter private String name;

}