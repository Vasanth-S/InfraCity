package com.infracity.android.rest;

import com.infracity.android.model.RoadInfo;
import com.infracity.android.model.Roads;
import com.infracity.android.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by pragadeesh on 17/12/16.
 */
public interface RestService {
    @FormUrlEncoded
    @POST("register")
    Call<User> signIn(@Field("email") String email, @Field("display_name") String displayName);

    @GET("dashboard")
    Call<Roads> getRoads();

    @GET("roads/{id}/info")
    Call<RoadInfo> getInfo(@Path("id") String id);
}
