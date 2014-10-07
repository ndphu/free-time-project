package ndphu.app.gae.openthis.dao.gae;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ndphu.app.gae.openthis.Utils;
import ndphu.app.gae.openthis.dao.Dao;
import ndphu.app.gae.openthis.model.BasicEntity;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.SortDirection;

public abstract class AbstractDao<T extends BasicEntity> implements Dao<T> {
	private Class<T> persistentClass;
	private DatastoreService datastore;
	private Key ancestorKey;

	public AbstractDao(Class<T> persistentClass) {
		this.setPersistentClass(persistentClass);
		datastore = DatastoreServiceFactory.getDatastoreService();
		ancestorKey = new KeyFactory.Builder(persistentClass.getSimpleName(),
				"ancestorKey").getKey();
	}

	@Override
	public T get(Long id) throws InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, EntityNotFoundException {
		Key key = ancestorKey.getChild(persistentClass.getSimpleName(), id);
		Entity gaeEntity = datastore.get(key);
		return getEntityFromGAEEntity(gaeEntity);
	}

	@Override
	public T save(T entity) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		Entity gaeEntity = new Entity(persistentClass.getSimpleName(),
				ancestorKey);
		List<Field> fields = Utils.getFields(persistentClass);
		for (Field field : fields) {
			if (field.getName().equals("id")) {
				continue;
			}
			gaeEntity.setProperty(field.getName(),
					PropertyUtils.getProperty(entity, field.getName()));
		}
		datastore.put(gaeEntity);
		return entity;
	}

	@Override
	public T update(T entity) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, EntityNotFoundException {
		Key key = ancestorKey.getChild(persistentClass.getSimpleName(),
				entity.getId());
		Entity gaeEntity = datastore.get(key);
		List<Field> fields = Utils.getFields(persistentClass);
		for (Field field : fields) {
			if (field.getName().equals("id")) {
				continue;
			}
			gaeEntity.setProperty(field.getName(),
					PropertyUtils.getProperty(entity, field.getName()));
		}
		datastore.put(gaeEntity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		Key key = ancestorKey.getChild(persistentClass.getSimpleName(), id);
		datastore.delete(key);
	}

	@Override
	public List<T> getAll() throws InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return getAll(null);
	}

	@Override
	public List<T> getAll(Filter filter) throws InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return getAll(filter, null);
	}

	@Override
	public List<T> getAll(Filter filter, Map<String, SortDirection> sort)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		List<T> result = new ArrayList<T>();
		Query q = new Query(persistentClass.getSimpleName());
		if (sort != null) {
			for (Entry<String, SortDirection> sortEntry : sort.entrySet()) {
				System.out.println(sortEntry.getKey());
				q.addSort(sortEntry.getKey(), sortEntry.getValue());
			}
		}
		if (filter != null) {
			q.setFilter(filter);
		}
		q.setAncestor(ancestorKey);
		PreparedQuery pq = datastore.prepare(q);
		for (Entity gaeEntity : pq.asIterable()) {
			T entity = getEntityFromGAEEntity(gaeEntity);
			result.add(entity);
		}
		return result;
	}

	private T getEntityFromGAEEntity(Entity gaeEntity)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		T entity = persistentClass.newInstance();
		entity.setId(gaeEntity.getKey().getId());
		List<Field> fields = Utils.getFields(persistentClass);
		for (Field field : fields) {
			if (field.getName().equals("id")) {
				continue;
			}
			PropertyUtils.setProperty(entity, field.getName(),
					gaeEntity.getProperty(field.getName()));
		}
		return entity;
	}

	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	public void setPersistentClass(Class<T> persistentClass) {
		this.persistentClass = persistentClass;
	}
}
