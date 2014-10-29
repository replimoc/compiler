package compiler.utils;

import java.lang.reflect.Method;

/**
 * This class can be used for testing private methods of a class. The methods are called using reflections, which isn't effected by the private
 * modifier. Therefore this class allows unit testing of internal methods without making them publicly available.
 * 
 * @author Andreas Eberle
 *
 */
public class PrivateMethodCaller {

	private Class<?> classObject;

	public PrivateMethodCaller(Class<?> classObject) {
		this.classObject = classObject;
	}

	/**
	 * This method can call any method, regardless if it is private or not. In order to call a static method set baseObject to null.
	 * 
	 * NOTE: This method cannot be used to call methods with primitive types in the parameter list. In order to do so, use
	 * {@link #call(String, Object, Class[], Object[])}.
	 * 
	 * @param methodName
	 *            Name of the method to be called.
	 * @param baseObject
	 *            Base object to be used to call the method. If it is a static method, this parameter will be ignored and should therefore be set to
	 *            null.
	 * @param args
	 *            The arguments given to the method in the correct order.
	 * 
	 * @return Result of the called method.
	 * 
	 * @throws NullPointerException
	 *             In case the specified method is non static, but no baseObject has been supplied.
	 * @throws RuntimeException
	 *             A {@link RuntimeException} is thrown in any case of an error.
	 */
	public <T> T call(String methodName, Object baseObject, Object... args) {
		Class<?> parameterTypes[] = new Class<?>[args.length];
		int i = 0;
		for (Object curr : args) {
			parameterTypes[i] = curr.getClass();
			i++;
		}

		return call(methodName, baseObject, parameterTypes, args);
	}

	/**
	 * This method can call any method, regardless if it is private or not. In order to call a static method set baseObject to null.
	 * 
	 * NOTE: This method can also call methods with primitive parameter types. In order to specify a primitive type like for example int, use
	 * int.class to get the correct class object.
	 * 
	 * @param methodName
	 *            Name of the method to be called.
	 * @param baseObject
	 *            Base object to be used to call the method. If it is a static method, this parameter will be ignored and should therefore be set to
	 *            null.
	 * @param parameterTypes
	 *            This array specifies the types of the given parameters and is used to find the correct method. In order to use primitive types use
	 *            e.g. int.class.
	 * @param args
	 *            The arguments given to the method in the correct order.
	 * 
	 * @return Result of the called method.
	 * 
	 * @throws NullPointerException
	 *             In case the specified method is non static, but no baseObject has been supplied.
	 * @throws RuntimeException
	 *             A {@link RuntimeException} is thrown in any case of an error. throws
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T call(String methodName, Object baseObject, Class<?>[] parameterTypes, Object[] args) {
		if (parameterTypes.length != args.length) {
			throw new IllegalArgumentException("The parameterTypes and args arrays need to be of same length!");
		}

		try {
			Method method = classObject.getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return (T) method.invoke(baseObject, args);
		} catch (NullPointerException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
