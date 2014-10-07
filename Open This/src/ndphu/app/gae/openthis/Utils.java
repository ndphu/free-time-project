package ndphu.app.gae.openthis;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	public static JSONObject toJSONObject(Object base)
			throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, JSONException {
		JSONObject json = new JSONObject();
		List<Field> fields = getFields(base.getClass());
		for (Field field : fields) {
			Object property = PropertyUtils.getProperty(base, field.getName());
			json.put(field.getName(),
					property == null ? "" : property);
		}
		return json;
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

	public static Object parseJSONObject(JSONObject jsonObj, Class clazz)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, JSONException {
		Object entity = clazz.newInstance();
		List<Field> fields = Utils.getFields(clazz);
		for (Field field : fields) {
			if (jsonObj.has(field.getName())) {
				PropertyUtils.setProperty(entity, field.getName(),
						jsonObj.get(field.getName()));
			}
		}
		return entity;
	}
}
