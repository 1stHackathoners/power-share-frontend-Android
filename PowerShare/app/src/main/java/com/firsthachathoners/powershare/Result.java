package com.firsthachathoners.powershare;

/**
 * Created by safa on 17.02.2018.
 */

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("available_pb_num")
    @Expose
    private Integer availablePbNum;
    @SerializedName("available_cp_num")
    @Expose
    private Integer availableCpNum;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("location")
    @Expose
    private List<Double> location = null;
    @SerializedName("__v")
    @Expose
    private Integer v;

    public Integer getAvailablePbNum() {
        return availablePbNum;
    }

    public void setAvailablePbNum(Integer availablePbNum) {
        this.availablePbNum = availablePbNum;
    }

    public Integer getAvailableCpNum() {
        return availableCpNum;
    }

    public void setAvailableCpNum(Integer availableCpNum) {
        this.availableCpNum = availableCpNum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public Integer getV() {
        return v;
    }

    public void setV(Integer v) {
        this.v = v;
    }

}