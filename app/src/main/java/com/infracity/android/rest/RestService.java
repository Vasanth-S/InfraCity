package com.infracity.android.rest;

import com.infracity.android.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by pragadeesh on 17/12/16.
 */
public interface RestService {
    @FormUrlEncoded
    @POST("register")
    Call<User> signIn(@Field("email") String email, @Field("display_name") String displayName);
}
