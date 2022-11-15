package cn.knet.businesstask.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 使用restTemplate后台访问URL地址
 */
public class RestTemplateToURL{
	
	static Logger logger = LoggerFactory.getLogger(RestTemplateToURL.class);

	public static String dopost(String url){
		try {
			URL u = new URL(url);
			HttpURLConnection con = (HttpURLConnection) u.openConnection();
			con.setConnectTimeout(2000);
			con.connect();
			return "1";
		} catch (Exception e) {
			return null;
		}
	}
}