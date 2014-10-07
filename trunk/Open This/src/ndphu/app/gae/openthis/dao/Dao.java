package ndphu.app.gae.openthis.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import ndphu.app.gae.openthis.model.BasicEntity;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.SortDirection;

public interface Dao<T extends BasicEntity> {
	public T save(T entity) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException;
	
	public T get(Long id) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, EntityNotFoundException;
	

	public List<T> getAll() throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException;

	public List<T> getAll(Filter filter) throws InstantiationException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException;
	
	public List<T> getAll(Filter filter, Map<String, SortDirection> sort) throws InstantiationException,
	IllegalAccessException, InvocationTargetException,
	NoSuchMethodException;

	public T update(T entity) throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, EntityNotFoundException;
	
	public void delete(Long id);
}
