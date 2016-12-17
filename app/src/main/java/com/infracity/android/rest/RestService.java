package com.infracity.android.rest;

import com.infracity.android.model.RoadInfo;
import com.infracity.android.model.Roads;
import com.infracity.android.model.UploadResponse;
import com.infracity.android.model.User;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
    Call<RoadInfo> getInfo(@Path("id") int id);

    @Multipart
    @POST("upload_photo")
    Call<UploadResponse> updatePhoto(@Part("road_id") RequestBody road_id,
                                     @Part("user_id") RequestBody user_id,
                                     @Part("photo") RequestBody photo);

    @FormUrlEncoded
    @POST("ratings")
    Call<Object> updateInfo(@Field("road_id") int road_id,
                            @Field("user_id") int user_id,
                            @Field("encroachment") int encroachment,
                            @Field("safety") int safety,
                            @Field("platform_usability") int platform_usability,
                            @Field("road_quality") int road_quality);

}
