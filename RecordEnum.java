/*
 * @(#)Audit.java 2013-8-14
 *
 * Copyright 2011 北龙中网（北京）科技有限责任公司. All rights reserved.
 */
package cn.knet.suggest.enums;

import cn.knet.domain.enums.ToJson;
import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 企业记录类型
 */
public enum RecordEnum {
	SHORT("SHORT","企业简称"),
	TRADE("TRADE","企业商标"),
	DOMAIN("DOMAIN","企业网站"),
	WEBSITE("WEBSITE","官网"),
	PRODUCT("PRODUCT","企业产品");

	private static final Map<String, String> MAPPING = new LinkedHashMap<String, String>();

	private static final Map<String, String> INVERSE_MAPPING = new LinkedHashMap<String, String>();

	@ToJson
	private String value;

	@ToJson
	private String text;

	static {
		for (RecordEnum em : RecordEnum.values()) {
			MAPPING.put(em.getText(), em.getValue());
			INVERSE_MAPPING.put(em.getValue(), em.getText());
		}
	}

	/**
	 *
	 * @param value
	 * @param text
	 */
	RecordEnum(final String value, final String text) {
		this.value = value;
		this.text = text;
	}

	public String getValue() {
		return value;
	}

	
	public String getText() {
		return text;
	}
	
	
	public static RecordEnum get(String enumValue) {
		for (RecordEnum em : RecordEnum.values()) {
			if (em.getValue().equals(enumValue)) {
				return em;
			}
		}
		throw new IllegalArgumentException("Can't get enum with this enumValue.");
	}

	/**
	 * 
	 * @return
	 */
	public static Map<String, String> mapping() {
		return MAPPING;
	}

	/**
	 * 
	 * @return
	 */
	public static Map<String, String> inverseMapping() {
		return INVERSE_MAPPING;
	}
	@JsonValue
	public Map<String, Object> jsonValue() throws IllegalArgumentException, IllegalAccessException {
		Map<String, Object> map  = new HashMap<String, Object>();
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			ToJson toJson = f.getAnnotation(ToJson.class);
			if (toJson != null) {
				f.setAccessible(true);
				Object v = f.get(this);
				map.put(f.getName(), v);	
			}
		}
		return map;
	}
}
