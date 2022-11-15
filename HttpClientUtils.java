package cn.knet.businesstask.util;

import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClientUtils {
    public static final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

    public static String getResponse(String url, String imgUrl) throws IOException {

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), getFile(imgUrl));

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("media", "image", fileBody)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {

            return response.body().string();
        } else {

            throw new RuntimeException(response.body().string());
        }
    }

    private static byte[] getFile(String imgUrl) throws IOException {

        Request request = new Request.Builder()
                .url(imgUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().bytes();
        } else {
            throw new RuntimeException(String.valueOf(response));
        }
    }
}
