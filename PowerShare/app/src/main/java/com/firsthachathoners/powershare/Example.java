package com.firsthachathoners.powershare;

/**
 * Created by safa on 18.02.2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Example implements Serializable {

    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("credit")
    @Expose
    private String credit;
    @SerializedName("session")
    @Expose
    private Session session;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void printInfo(){
        System.out.println(getUsername());
        System.out.println(getCredit());
        System.out.println(getSession());
        System.out.println("\n\n\n\n\n\n\n\n");


    }

}