package com.sj.gfodapp.utils;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface ApiInterface {

    //TODO : User Registraion

    @Multipart
    @PATCH("api/users/register")
    Call<ResponseBody> userRegister(@Part MultipartBody.Part avatar,
                                    @Part("nic") RequestBody nic,
                                    @Part("email") RequestBody email,
                                    @Part("password") RequestBody password,
                                    @Part("fullName") RequestBody fullName,
                                    @Part("district") RequestBody district);

    //TODO : Login
    @FormUrlEncoded
    @POST("api/auth")
    Call<JsonObject> userLogin(@Field("username") String email_, @Field("password") String password_);


    //TODO : Users

    //Get all
    //Add new
    @GET("api/users/get")
    Call<JsonObject> getAllUsers();

    //Get account by id
    @GET("api/users/get/{loginId}")
    Call<JsonObject> getUserProfile(@Path("loginId") String loginId);

    //Get UserProfile By ProfileID
    @GET("api/users/getByProfId/{profId}")
    Call<JsonObject> getUserProfileByProfileID(@Path("profId") String profId);


    //Get My Division Id by loginId
    @GET("api/users/getMyDivisionId/{loginId}")
    Call<ResponseBody> getMyDivisionId(@Path("loginId") String loginId);


    //Add New User
    @FormUrlEncoded
    @POST("api/users/makeAccount/{role}")
    Call<JsonObject> addUser(@Path("role") String role, @Field("divisionID") String divisionID, @Field("nic") String nic, @Field("email") String email);

    //Add New Person
    @FormUrlEncoded
    @POST("api/users/add-rfid-tag")
    Call<JsonObject> addRfid_tag( @Field("fullName") String fullName,@Field("city") String city,@Field("status") String status, @Field("divisionId") String divisionId);


    //Add New Person
    @FormUrlEncoded
    @PATCH("api/users/update-rfid-tag/{profileId}")
    Call<JsonObject> updateRfid_tag(@Path("profileId") String profileId, @Field("fullName") String fullName, @Field("city") String city, @Field("status") String status, @Field("divisionId") String divisionId);

    @FormUrlEncoded
    @POST("api/users/reset-password")
    Call<JsonObject> forgetPassword(@Field("email") String email);

    @FormUrlEncoded
    @POST("api/users/update-password")
    Call<JsonObject> updatePassword(@Field("loginId") String loginId, @Field("oldPsw") String oldPsw, @Field("newPsw") String newPsw);


    //Get Users By Role
    @GET("api/users/getByRole/{role}/{voteEli}/{divisionId}")
    Call<JsonObject> getUsersByRole(@Path("role") String role, @Path("voteEli") String voteEli, @Path("divisionId") String divisionId);

    //Get Users & divisions count
    @GET("api/users/count")
    Call<ResponseBody> getCounts();


    //TODO : Division

    //get all division List
    @GET("api/divisions")
    Call<JsonObject> getDivisionList();

    //Get Division by ID
    @GET("api/divisions/{id}")
    Call<JsonObject> getDivisionByID(@Path("id") String id);

    //Add new division
    @FormUrlEncoded
    @POST("api/divisions")
    Call<JsonObject> addDivision(@Field("regNumber") String regNumber,
                                 @Field("name") String name,
                                 @Field("district") String district,
                                 @Field("longitude") String longitude,
                                 @Field("latitude") String latitude);

    //Delete division
    @DELETE("api/divisions/{id}")
    Call<JsonObject> deleteDivision(@Path("id") String id);

    //Update division
    @FormUrlEncoded
    @PUT("api/divisions")
    Call<JsonObject> updateDivision(@Field("id") String id,
                                    @Field("regNumber") String regNumber,
                                    @Field("name") String name,
                                    @Field("district") String district,
                                    @Field("longitude") String longitude,
                                    @Field("latitude") String latitude);



    //TODO : Disaster

    //Get All or by ID and year
    @GET("api/faults/{year}/{divId}")
    Call<JsonObject> getFaults(@Path("year") String year, @Path("divId") String divId);

    //Get by ID
    @GET("api/faults/getById/{faultId}")
    Call<JsonObject> getFaultByID(@Path("faultId") String faultId);


    @Multipart
    @POST("api/faults")
    Call<ResponseBody> addFault(@Part MultipartBody.Part image,
                                   @Part("fault") RequestBody fault,
                                   @Part("description") RequestBody description,
                                   @Part("monthOccured") RequestBody monthOccured,
                                   @Part("yearOccured") RequestBody yearOccured,
                                   @Part("divisionId") RequestBody divisionId,
                                   @Part("longitude") RequestBody longitude,
                                   @Part("latitude") RequestBody latitude);
}
