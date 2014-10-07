package com.ndphu.app.openthis.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javadz.beanutils.PropertyUtilsBean;

import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	private static PropertyUtilsBean PROPERTY_UTILS_BEAN = new PropertyUtilsBean();

	public static JSONObject toJSONObject(Object base)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, JSONException {
		JSONObject json = new JSONObject();
		List<Field> fields = getFields(base.getClass());
		for (Field field : fields) {
			json.put(field.getName(),
					PROPERTY_UTILS_BEAN.getProperty(base, field.getName()));
		}
		return json;
	}

	public static Object parseJSONObject(JSONObject jsonObj, @SuppressWarnings("rawtypes") Class clazz)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, JSONException {
		Object entity = clazz.newInstance();
		List<Field> fields = Utils.getFields(clazz);
		for (Field field : fields) {
			if (jsonObj.has(field.getName())) {
				PROPERTY_UTILS_BEAN.setProperty(entity, field.getName(),
						jsonObj.get(field.getName()));
			}
		}
		return entity;
	}

	public static List<Field> getFields(Class<?> clazz) {
		List<Field> result = new ArrayList<Field>();
		for (Field field : clazz.getDeclaredFields()) {
			result.add(field);
		}

		Class<?> superclass = clazz.getSuperclass();
		while (superclass != null) {
			for (Field field : superclass.getDeclaredFields()) {
				result.add(field);
			}
			superclass = superclass.getSuperclass();
		}

		return result;
	}
}
