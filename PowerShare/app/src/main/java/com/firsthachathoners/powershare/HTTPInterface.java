package com.firsthachathoners.powershare;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by safa on 17.02.2018.
 */

public interface HTTPInterface {
    @FormUrlEncoded
    @POST("/searchRecords")
    Call<List<JSONData>> getAllRecords(@Field("longitude") float longitude, @Field("latitude") float latitude, @Field("range") int range);

}
