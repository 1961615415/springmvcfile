package cn.knet.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BdyApiCalledUtil {
    public static void main(String[] args) throws IOException {
        long stime=System.currentTimeMillis();
        String url = "https://businessget.api.bdymkt.com/v2/business/get";
        String AppCode ="f99ff18ec022474b942dc119c0c92d9a";

        Map<String, String> params = new HashMap<>();
        params.put("keyword", "91430381MA4L4GMGX8");

        String result = null;
        try {
            result = get(AppCode, url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long etime=System.currentTimeMillis();
        System.out.println("response: "+result);
        System.out.println("====total time:"+(etime-stime)/1000d+"秒");
    }

    /**
     * 用到的HTTP工具包：okhttp 3.13.1
     * <dependency>
     * <groupId>com.squareup.okhttp3</groupId>
     * <artifactId>okhttp</artifactId>
     * <version>3.13.1</version>
     * </dependency>
     */
    public static String get(String appCode, String url, Map<String, String> params) throws IOException {
        url = url + buildRequestUrl(params);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).readTimeout(1, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(url).addHeader("X-Bce-Signature", "AppCode/" + appCode).build();
        Response response = client.newCall(request).execute();
        System.out.println("返回状态码" + response.code() + ",message:" + response.message());
        String result = response.body().string();
        return result;
    }

    private static String buildRequestUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder("?");
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        return url.toString().substring(0, url.length() - 1);
    }
}
