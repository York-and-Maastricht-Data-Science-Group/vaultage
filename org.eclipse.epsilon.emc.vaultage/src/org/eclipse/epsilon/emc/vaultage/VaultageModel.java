package org.eclipse.epsilon.emc.vaultage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolEnumerationValueNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelElementTypeNotFoundException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.exceptions.models.EolNotInstantiableModelElementTypeException;
import org.eclipse.epsilon.eol.execute.introspection.IPropertyGetter;
import org.eclipse.epsilon.eol.execute.introspection.IPropertySetter;
import org.eclipse.epsilon.eol.execute.operations.contributors.IOperationContributorProvider;
import org.eclipse.epsilon.eol.execute.operations.contributors.OperationContributor;
import org.eclipse.epsilon.eol.models.Model;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.vaultage.core.Entity;
import org.vaultage.core.RemoteVault;
import org.vaultage.core.Vault;

/***
 * 
 * @author Alfa Yohannis
 *
 */
public class VaultageModel extends Model implements IOperationContributorProvider {

	private Vault localVault;
	private Reflections reflections;
	private Set<Class<?>>types = new HashSet<>();
	private Set<Object> contents = new HashSet<>();
	private VaultageOperationContributor vaultageOperationContributor = new VaultageOperationContributor();
	

	public VaultageModel(Vault localVault, Set<Package> vaultPackages)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		this.localVault = localVault;

		FilterBuilder filter = new FilterBuilder();
		Collection<URL> urls = new HashSet<>();
		for (Package pack : vaultPackages) {
			urls.addAll(ClasspathHelper.forPackage(pack.getName()));
			filter.includePackage(pack.getName());
		}

		ConfigurationBuilder conf = new ConfigurationBuilder().addUrls(urls).setScanners(new SubTypesScanner(false))
				.filterInputsBy(filter);
		reflections = new Reflections(conf);
		
		types.addAll(reflections.getSubTypesOf(Entity.class));
		types.addAll(reflections.getSubTypesOf(Vault.class));
		types.addAll(reflections.getSubTypesOf(RemoteVault.class));

		System.console();
	}

	@Override
	public Collection<?> getAllOfType(String type) throws EolModelElementTypeNotFoundException {
		return getAllOfKind(type);
	}

	@Override
	public Collection<?> getAllOfKind(String kind) throws EolModelElementTypeNotFoundException {
		Collection<Object> collection = new HashSet<>();

		if (this.localVault.getClass().getSimpleName().equals(kind))
			collection.add(this.localVault);
		Method[] methods = this.localVault.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.getModifiers() == Modifier.PUBLIC) {
				Type genericReturnType = method.getGenericReturnType();
				if (genericReturnType instanceof ParameterizedType) {
					ParameterizedType pt = (ParameterizedType) genericReturnType;
					for (Type t : pt.getActualTypeArguments()) {
						Class<?> argumentType = (Class<?>) t;
						if (argumentType.getSimpleName().equals(kind)) {
							Object val = null;
							try {
								val = method.invoke(this.localVault);
							} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								e.printStackTrace();
							}
							if (val instanceof Collection<?>) {
								Collection<Object> col = (Collection) val;
								collection.addAll(col);
								contents.addAll(collection);
							}
						}
					}
				} else {

				}
			}
		}
		return collection;
	}

	@Override
	public Object createInstance(String type)
			throws EolModelElementTypeNotFoundException, EolNotInstantiableModelElementTypeException {
		for (Class<?> cls : types) {
			if (cls.getSimpleName().equals(type)) {
				try {
					Object value = cls.getConstructor().newInstance();
					contents.add(value);
					return value;
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}

		}
		return null;
	}

	@Override
	public boolean hasType(String typeName) {
		Class<?> typeClass = types.stream().filter(t -> t.getSimpleName().equals(typeName)).findFirst().orElse(null);
		if (typeClass != null)
			return true;
		else
			return false;
	}

	@Override
	public boolean owns(Object instance) {
		if (contents.contains(instance))
			return true;
		else
			return false;
	}

	@Override
	public IPropertyGetter getPropertyGetter() {
		return new VaultageModelPropertyGetter();
	}

	@Override
	public IPropertySetter getPropertySetter() {
		return new VaultageModelPropertySetter();
	}

	@Override
	public void load() throws EolModelLoadingException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getEnumerationValue(String enumeration, String label) throws EolEnumerationValueNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<?> allContents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTypeNameOf(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getElementById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getElementId(Object instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setElementId(Object instance, String newId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteElement(Object instance) throws EolRuntimeException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInstantiable(String type) {
		return true;
	}

	@Override
	public boolean store(String location) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean store() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OperationContributor getOperationContributor() {
		return vaultageOperationContributor;
	}
	
}
