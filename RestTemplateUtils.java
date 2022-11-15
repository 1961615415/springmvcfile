package cn.knet.businesstask.util;

import cn.knet.domain.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

public class RestTemplateUtils {

	private final static Logger logger = LoggerFactory.getLogger(RestTemplateUtils.class);

	public static Map<String, Object> doPut(String url, MultiValueMap<String, String> var) throws Exception {
		return RestTemplateUtils.doHttp(url, HttpMethod.PUT, var);
	}
	
	public static Map<String, Object> doPost(String url, MultiValueMap<String, String> var) throws Exception {
		return RestTemplateUtils.doHttp(url, HttpMethod.POST, var);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> doHttp(String url, HttpMethod method, MultiValueMap<String, String> var)
			throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		HttpEntity<Object> entity = new HttpEntity<Object>(var, null);
		logger.info("var:" + var);
		logger.info("method:" + method);
		logger.info("url:" + url);
		Map<String, Object> result = restTemplate.exchange(url, method, entity, Map.class).getBody();
		logger.info("result:" + result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> doGet(String url, MultiValueMap<String, String> vars) throws Exception {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		URI uri = UriComponentsBuilder.fromHttpUrl(url).queryParams(vars).build(false).toUri();
		logger.info("method:GET");
		logger.info("uri:" + uri);
		Map<String, Object> result = restTemplate.getForObject(uri, Map.class);
		logger.info("result:" + result);
		return result;
	}
	@SuppressWarnings("unchecked")
	public static Map<String, Object> PostJson(String url, Map<String, String> var) throws Exception {	
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		HttpEntity<String> formEntity = new HttpEntity<String>(JsonUtils.toJson(var), headers);
		Map<String, Object> result = restTemplate.postForObject(url, formEntity, Map.class);
		return result;
	}


	public static String PostJsonTxt(String url, Map<String, Object> var)  {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		MediaType type = MediaType.parseMediaType("text/plain; charset=UTF-8");
		headers.setContentType(type);
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		String str=JsonUtils.toJson(var);
		logger.info("var:" + str);
		logger.info("url:" + url);
		String result=null;
		try {
			HttpEntity<String> formEntity = new HttpEntity<String>(str, headers);
			result= restTemplate.postForObject(url, formEntity, String.class);
			logger.info("result:"+result);
		} catch (Exception e) {
			logger.info("result:"+e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	
	

}
