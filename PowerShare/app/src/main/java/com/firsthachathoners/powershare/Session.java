package com.firsthachathoners.powershare;

/**
 * Created by safa on 18.02.2018.
 */


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Session implements Serializable {

    @SerializedName("session_start")
    @Expose
    private String sessionStart;
    @SerializedName("session_end")
    @Expose
    private String sessionEnd;
    @SerializedName("isOn")
    @Expose
    private String isOn;

    public String getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(String sessionStart) {
        this.sessionStart = sessionStart;
    }

    public String getSessionEnd() {
        return sessionEnd;
    }

    public void setSessionEnd(String sessionEnd) {
        this.sessionEnd = sessionEnd;
    }

    public String getIsOn() {
        return isOn;
    }

    public void setIsOn(String isOn) {
        this.isOn = isOn;
    }

}