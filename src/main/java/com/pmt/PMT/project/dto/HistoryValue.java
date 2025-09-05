package com.pmt.PMT.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryValue {
    private final String text;
    private final UserSummary user;

    private HistoryValue(String text, UserSummary user) {
        this.text = text;
        this.user = user;
    }

    public static HistoryValue ofText(String text) {
        return new HistoryValue(text, null);
    }

    public static HistoryValue ofUser(UserSummary user) {
        return new HistoryValue(null, user);
    }

    public boolean isUser() {
        return user != null;
    }

    public String getText() {
        return text;
    }

    public UserSummary getUser() {
        return user;
    }
}
