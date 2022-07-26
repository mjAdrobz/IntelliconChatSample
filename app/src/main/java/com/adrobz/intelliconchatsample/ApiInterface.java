package com.adrobz.intelliconchatsample;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiInterface {


    @Multipart
    @POST
    Call<AttachmentResponse> uploadAttachment(@Url String url, @Part MultipartBody.Part file);
}
