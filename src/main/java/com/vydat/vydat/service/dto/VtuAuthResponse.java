package com.vydat.vydat.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VtuAuthResponse {

    private String token;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("user_nicename")
    private String userNicename;

    @JsonProperty("user_display_name")
    private String userDisplayName;

    // getters & setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserNicename() { return userNicename; }
    public void setUserNicename(String userNicename) { this.userNicename = userNicename; }

    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }
}
