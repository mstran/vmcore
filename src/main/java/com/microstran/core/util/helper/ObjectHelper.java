/*
 * Copyright(c) 2022 Microstran Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microstran.core.util.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The ObjectHelper is a utility helper class that provides static methods 
 * to dynamically build an object and/or dynamically execute methods on 
 * objects.
 * <p>
 * Methods in this class will catch many different error codes for logging purposes but
 * will only declare a type of <code>Exception</code>
 *
 * @author stranm
 * 
 **/
@SuppressWarnings("unchecked")
public class ObjectHelper
{
	
    /**
     * building an object using its no-argument constructor whose class is
     * provided as an argument.
     *
     * @param className The fully qualified name of the class to be
     *                  instantiated.
     *
     * @return The newly constructed object.
     *
     * @throws RuntimeException if an object of the specified class
     *          cannot be instantiated.
     */
    @SuppressWarnings("rawtypes")
	public static Object build( String className )
    	throws Exception
    {
    	Object [ ] params = null;
		Class [ ] paramTypes = null;
        return build( className, params, paramTypes, true );
    }

    /**
     * Build an object of class className using the constructor with
     * the provided arguments.
     *
     * @param className The fully qualified name of the class
     *                   
     * @param params    An array of parameters to be provided to class's
     *                   constructor
     *
     * @return The intantiated object.
     *
     * @throws PluginHelperException if an object of the specified class cannot
     *          be instantiated and constructed using a constructor with a
     *          signature matching the types of the provided paramters.
     */
    @SuppressWarnings("rawtypes")
	public static Object build( String className, Object[] params )
    	throws Exception
    {
    	Object    obj    = null;
		Class[ ] paramTypes = buildParameterClassArray( params );
		obj = build( className, params, paramTypes, true );
        return obj;
    }

    /**
    **  Build an object of the provided class using the appropriate
    **  constructor for the arguments and types specified.  This method is
    **  needed rather than the more generic instantiate( String, Object[] )
    **  when one or more of the parameters are subclasses of the actual
    **  constructor parameter types.  If the generic method is used, the
    **  Class.getDeclaredConstructor method will fail because it is looking
    **  for an exact match on the parameter types.
    **
    **  @param className Fully qualified name of the class to be instantiated.
    **  @param params Array of parameters to be provided to the class's
    **  constructor when it is instantiated.
    **  @param paramTypes Array of parameter class matching the parameters
    **  expected in the constructor.
    **  @return New instance of the object represented by the class name
    **  parameter.
    **
    **  @throws RuntimeException if an object of the specified class
    **  can not be instantiated or constructed using the constructor with
    **  the provided parameters and parameter types.
    */
    @SuppressWarnings("rawtypes")
	public static Object build(String className,Object[] params, Class[] paramTypes)
    throws Exception
    {
		return build( className, params, paramTypes, true );
	}

    /**
    **  Build an object of the provided class using the appropriate
    **  constructor for the arguments and types specified.  This method is
    **  needed rather than the more generic instantiate( String, Object[] )
    **  when one or more of the parameters are subclasses of the actual
    **  constructor parameter types.  If the generic method is used, the
    **  Class.getDeclaredConstructor method will fail because it is looking
    **  for an exact match on the parameter types.
    **
    **  @param className Fully qualified name of the class to be instantiated.
    **  @param params Array of parameters to be provided to the class's
    **  constructor when it is instantiated.
    **  @param paramTypes Array of parameter class matching the parameters
    **  expected in the constructor.
    **  @param logArgValuesFlag Flag indicating if the arguments are supposed to
    **  be logged if debug level logging is enabled.
    **  @return New instance of the object represented by the class name
    **  parameter.
    **
    **  @throws RuntimeException if an object of the specified class
    **  can not be instantiated or constructed using the constructor with
    **  the provided parameters and parameter types.
    */
    @SuppressWarnings("rawtypes")
	public static Object build(String className, Object[] params,	Class[]	paramTypes,	boolean	logArgValuesFlag)
    throws Exception
    {
        Object obj = null;

        try
        {
            Class   clazz  = Class.forName( className );

            if ( ( paramTypes != null ) && ( paramTypes.length > 0 ) )
            {
                Constructor constructor = clazz.getDeclaredConstructor( paramTypes );
                obj = constructor.newInstance( params );
            }
            else
            {
                obj = clazz.newInstance( );
            }
        }
        catch (ClassNotFoundException e)
        {
			String data = "Class: " + className + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        catch (InstantiationException ie)
        {
			String data = "Class: " + className + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        catch (ExceptionInInitializerError eie)
        {
			String data = "Class: " + className + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        return obj;
    }

    /**
     * Performs checks to determine if it is at all possible for an object of
     * the provided class name can be instantiated using its no-argument
     * constructor.
     * <br>
     * Note that even if this method returns true this does not
     * absolutely guarantee that an object of the provided class actually will
     * be constructed if a build( ) method is subsequently
     * called.  This method checks to see that the provided class can be found
     * in the classpath and that it has no argument constructor.  
     *
     * @param className The fully qualified name of the class to be checked.
     *
     * @return true  if an object of the provided class can be instantiated
     *                using its no-argument constructor, else false if an
     * 				  object of the provided class can not be instantiated
     *                using its no-argument constructor.
     */
    public static boolean canConstruct( String className )
    {
        return canConstruct(className, null, null);
    }

    /**
     * Performs checks to determine if it is at all possible for an object of
     * the provided class name can be instantiated using its no-argument
     * constructor.
     * <br>
     * Note that even if this method returns true this does not
     * absolutely guarantee that an object of the provided class actually will
     * be constructed if a build( ) method is subsequently
     * called.  This method checks to see that the provided class can be found
     * in the classpath and that it has no argument constructor.  
     *
     * @param className The fully qualified name of the class to be checked.
     * @param params    An array of parameters that would be provided to the
     *                   class's constructor if it were to be instantiated.
     *
     * @return true  if an object of the provided class can be instantiated
     *                using a constructor with a signature matching the
     *                provided arguments. Returns false if an object of the
     * 				  provided class can not be instantiated using a
     *                constructor with a signature matching the provided
     *                arguments.
     */
    @SuppressWarnings("rawtypes")
	public static boolean canConstruct( String className, Object[] params )
    {
		Class[] paramTypes = buildParameterClassArray(params);
		return canConstruct(className, params, paramTypes);
	}

    /**
     * Performs checks to determine if it is at all possible for an object of
     * the provided class name can be instantiated using its no-argument
     * constructor.
     * <br>
     * Note that even if this method returns true this does not
     * absolutely guarantee that an object of the provided class actually will
     * be constructed if a build( ) method is subsequently
     * called.  This method checks to see that the provided class can be found
     * in the classpath and that it has no argument constructor.  
     *
     * @param className The fully qualified name of the class to be checked.
     * @param params    An array of parameters that would be provided to the
     *                   class's constructor if it were to be instantiated.
     * @param paramTypes Classes defining the required argument types.
     *
     * @return true  if an object of the provided class can be instantiated
     *                using a constructor with a signature matching the
     *                provided arguments. Returns false if an object of the
     * 				  provided class can not be instantiated using a
     *                constructor with a signature matching the provided
     *                arguments.
     */
    @SuppressWarnings("rawtypes")
	 public static boolean canConstruct(String	className, Object[]	params,	Class[]	paramTypes)
    {
        boolean canConstruct = true;
  		try
        {
            Class   clazz  = Class.forName(className);
            clazz.getDeclaredConstructor(paramTypes);
        }
        catch (Exception e)
        {
            canConstruct = false;
        }
        return canConstruct;
    }

    /**
     * Invoke the method with the given name.  The method must be one that takes
     * no arguments.
     *
     * @param obj  Object on which the method is to be invoked.
     * @param name The name of the method to invoke
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method will return null.
     *
     * @throws RuntimeException if the method with the provided name
     *          cannot be invoked on the provided object.
     */
    public static Object invokeMethod( Object obj, String name )
    	throws Exception
    {
        Object[] empty = new Object[0];
        return internalInvokeMethod(obj.getClass( ), obj, name, empty, null, true);
    }

    /**
     * Invoke the method with the given name.  The method must be one that takes
     * exactly one argument.
     *
     * @param obj   Object on which the method is to be invoked.
     * @param name  The name of the method to invoke
     * @param param A single parameter to be passed to the method thtat is
     *               invoked.
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method wil return null.
     *
     * @throws RuntimeException if the method with the provided name
     *         and signature matching the type of the provided parameter
     *         cannot be invoked on the provided object.
     */
    public static Object invokeMethod( Object obj, String name, Object param )
    	throws Exception
    {
        Object[] params = new Object[1];
        params[0] = param;
        return invokeMethod(obj, name, params);
    }

    /**
     * Invoke the method with the given name, arguments, and parameter types
     * on the provided object.
     *
     * @param obj    Object on which the method is to be invoked.
     * @param name   The name of the method to invoke.
     * @param params An array of parameters to be passed to the method that is
     *               called.
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method wil return null.
     *
     * @throws RuntimeException if the method with the provided name and
     *         signature matching the types of the provided parameters cannot
     *         be invoked on the provided object.
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeMethod( Object obj, String name, Object[] params )
    	throws Exception
    {
		Class[] paramTypes = buildParameterClassArray(params);
		return internalInvokeMethod(obj.getClass(), obj, name, params, paramTypes, true);
	}

    /**
     * Invoke the method with the given name, arguments, and parameter types
     * on the provided object.  This method is required if the classes of the
     * parameters do not exactly match the method signature.  For example if
     * the desired method requires an interface as a parameter.
     *
     * @param obj    Object on which the method is to be invoked.
     * @param name   The name of the method to invoke.
     * @param params An array of parameters to be passed to the method that is
     *               called.
     * @param paramTypes An array of classes defining the parameter types needed
     *				by the method.
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method wil return null.
     *
     * @throws RuntimeException if the method with the provided name and
     *         signature matching the types of the provided parameters cannot
     *         be invoked on the provided object.
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object obj, String name, Object[]	params,	Class[]	paramTypes)
    throws Exception
    {
		return internalInvokeMethod(obj.getClass( ), obj, name, params, paramTypes, true);
	}


    /**
     * Invoke the method with the given name, arguments, and parameter types
     * on the provided object.  This method is required if the classes of the
     * parameters do not exactly match the method signature.  For example if
     * the desired method requires an interface as a parameter.
     *
     * @param obj    Object on which the method is to be invoked.
     * @param name   The name of the method to invoke.
     * @param params An array of parameters to be passed to the method that is
     *               called.
     * @param paramTypes An array of classes defining the parameter types needed
     *				by the method.
     * @param logArgValuesFlag Flag indicating if the argument values should
     *				be logged if tracing is enabled.
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method wil return null.
     *
     * @throws RuntimeException if the method with the provided name and
     *         signature matching the types of the provided parameters cannot
     *         be invoked on the provided object.
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeMethod(Object obj, String name, Object[] params, Class[] paramTypes,	boolean	logArgValuesFlag)
    	throws Exception
    {
		return internalInvokeMethod(obj.getClass( ), obj, name, params, paramTypes, logArgValuesFlag);
	}

    /**
     * Invoke the method with the given name.  The method must be one that takes
     * no arguments.
     *
     * @param className - the neame of the class to invoke
     * @param methodName The name of the method to invoke
     *
     * @return Whatever value, if any, is returned by the method that is
     *         invoked.  If the invoked method does not return any value (i.e.
     *         returns void) this method will return null.
     *
     * @throws RuntimeException if the method with the provided name
     *          cannot be invoked on the provided object.
     */
    public static Object invokeStaticMethod(String className, String methodName)
    	throws Exception
    {
        Object[] emptyObj = new Object[0];
       // Class[] emptyClass = new Class[0];
        return invokeStaticMethod(className, methodName, emptyObj, null);
    }
    
	/**
	**  Invoke a static method of a class using the specified class name,
	**  method name, and provided arguments.
	**
	**  @param className Name of the class to execute the method for.
	**  @param methodName Name of the method to execute.
	**  @param params Array of parameters to pass to the method to execute.
	**  @return Whatever value, if any, is returned by the called method.
	**  Note that void methods will return null.
	**
	**  @throws RuntimeException if the method with the provided
	**  name and signature can not be invoked for the specified class.
	*/
    @SuppressWarnings("rawtypes")
	public static Object invokeStaticMethod(String	className, String methodName, Object[] params)
		throws Exception
	{
		Class[] paramTypes = buildParameterClassArray(params);
		return invokeStaticMethod(className, methodName, params, paramTypes);
	}

	/**
	**  Invoke a static method of a class using the specified class name,
	**  method name, provided arguments, and designated types.  This method
	**  must be used when the class of the argument does not exactly match
	**  the method signature as would be the case when the method parameter is an
	**  interface or the parameter is a subtype of the method signature type.
	**
	**  @param className Name of the class to execute the method for.
	**  @param methodName Name of the method to execute.
	**  @param params Array of parameters to pass to the method to execute.
	**  @param paramTypes Array of classes specifying the method signature.
	**  @return Whatever value, if any, is returned by the called method.
	**  Note that void methods will return null.
	**
	**  @throws RuntimeException if the method with the provided
	**  name and signature can not be invoked for the specified class.
	*/
    @SuppressWarnings("rawtypes")
	public static Object invokeStaticMethod(String className, String methodName, Object[] params, Class[]paramTypes)
	throws Exception
	{
		Class reqClass = findClassForName(className);
		return internalInvokeMethod(reqClass, null, methodName, params, paramTypes, true);
	}

	/**
	**  Invoke a static method of a class using the specified class name,
	**  method name, provided arguments, and designated types.  This method
	**  must be used when the class of the argument does not exactly match
	**  the method signature as would be the case when the method parameter is an
	**  interface or the parameter is a subtype of the method signature type.
	**
	**  @param tgtMethod Method to invoke.  This method must have been
	**  previously retrieved via reflection.
	**  @param params Array of parameters to pass to the method to execute.
	**  @param paramTypes Array of classes specifying the method signature.
	**  @return Whatever value, if any, is returned by the called method.
	**  Note that void methods will return null.
	**
	**  @throws RuntimeException if the method with the provided
	**  name and signature can not be invoked for the specified class.
	*/
    @SuppressWarnings("rawtypes")
	public static Object
	invokeStaticMethod(Method tgtMethod, Object[] params, Class[] paramTypes)
	throws Exception
	{
		return internalInvokeMethod( tgtMethod.getDeclaringClass( ), null, tgtMethod, params, paramTypes, true );
	}

	/**
	**  Find a class for the specified name.
	**
	**  @param className Name of the class to resolve.
	**  @return Class object associated with the name.
	**
	**  @throws RuntimeException on error.
	*/
	 @SuppressWarnings("rawtypes")
	public static Class	findClassForName(String className)
	throws Exception
	{
		Class rtnClass = null;
    	try
		{
			rtnClass = Class.forName( className );
		}
		catch (ClassNotFoundException e)
		{
			String data = "Class: " + className;
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
		}
		return rtnClass;
	}

	/**
	**  Retrieve the specified method for the designated class.
	**
	**  @param className Name of the class to retrieve a method for.
	**  @param methodName Name of the method within the class.
	**  @param paramTypes Types of parameters expected by the method.
	**  @return Invokable method object associated with the specifications.
	**
	**  @throws RuntimeException on error retrieving the method.
	*/
	 @SuppressWarnings("rawtypes")
	public static Method getDeclaredClassMethod(String className, String methodName, Class[] paramTypes)
	throws Exception
	{
		Class tgtClass = findClassForName( className );
		return getDeclaredClassMethod( tgtClass, methodName, paramTypes );
	}

	/**
	**  Retrieve the specified method for the designated class.
	**
	**  @param reqClass Class object to retrieve a method for.
	**  @param methodName Name of the method within the class.
	**  @param paramTypes Types of parameters expected by the method.
	**  @return Invokable method object associated with the specifications.
	**
	**  @throws RuntimeException on error retrieving the method.
	*/
	 @SuppressWarnings("rawtypes")
	public static Method getDeclaredClassMethod(Class reqClass, String	methodName,	Class[]	paramTypes)
		throws Exception
	{
		Method rtnMethod = null;
    	try
		{
			rtnMethod = reqClass.getMethod( methodName, paramTypes );
		}
		catch (NoSuchMethodException e)
		{
			String data = "Class Name: " + reqClass.getName( ) + " Method Name: " + methodName + " Parameter types: " +  buildTypeTrace(paramTypes);
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
		}
		return rtnMethod;
	}


	/**
	**  Actually invoke the method against the class and object specified.
	**
	**  @param reqClass Class object designating the method to perform.
	**  @param tgtObject Object to invoke the method against.  Will be null for
	**  static methods.
	**  @param methodName Name of the method to invoke.
	**  @param params Parameters to provide to the method.
	**  @param paramTypes Types of the parameters specifed by the method signature.
	**  @param logArgValuesFlag Flag indicating if the argument values should be logged.
	**  This flag is useful in suppressing logging of security related information.
	**  @return Return value from the invoked method.
	**
	**  @throws RuntimeException on error invoking the requested method.
	*/
	 @SuppressWarnings("rawtypes")
	private static Object internalInvokeMethod(	Class reqClass, Object tgtObject, String methodName, Object[] params, 
			Class[] paramTypes,	boolean	logArgValuesFlag)
		throws Exception
	{
		Method tgtMethod = getDeclaredClassMethod(reqClass, methodName, paramTypes);
		return internalInvokeMethod( reqClass, tgtObject, tgtMethod, params, paramTypes, true );
	}


	/**
	**  Actually invoke the method against the class and object specified.
	**
	**  @param reqClass Class object designating the method to perform.
	**  @param tgtObject Object to invoke the method against.  Will be null for
	**  static methods.
	**  @param tgtMethod Method to invoke.
	**  @param params Parameters to provide to the method.
	**  @param paramTypes Types of the parameters specifed by the method signature.
	**  @param logArgValuesFlag Flag indicating if the argument values should be logged.
	**  This flag is useful in suppressing logging of security related information.
	**  @return Return value from the invoked method.
	**
	**  @throws RuntimeException on error invoking the requested method.
	*/
	 @SuppressWarnings("rawtypes")
	private static Object internalInvokeMethod(Class reqClass, Object tgtObject, Method	tgtMethod, Object[]	params,	
			Class[]	paramTypes, boolean	logArgValuesFlag)
		throws Exception
	{
		Object retObj = null;
        try
        {
           retObj = tgtMethod.invoke( tgtObject, params );
        }
        catch (IllegalAccessException ia)
        {
        	String data = "Class: " + reqClass + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        catch (InvocationTargetException ite)
        {
        	String data = "Class: " + reqClass + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        catch (ExceptionInInitializerError eie)
        {
			String data = "Class: " + reqClass + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        catch (Exception e)
        {
			String data = "Class: " + reqClass + " Parameters: " + buildParameterTrace( params ) +  " Parameter types: " + buildTypeTrace( paramTypes );
			throw new RuntimeException(ObjectHelper.class.getName()  + " : Exception Data: " + data);
        }
        return retObj;
    }

    /**
     * This method will determine if the object in question has a specific
     * method.  If it does, then a value of true is returned.  If it does not,
     * then a value of false is returned.
     * @param obj The object to test and see if a method exists for it.
     * @param methodName The name of the method to determine if it exists.
     * @return boolean
     */
	 @SuppressWarnings("rawtypes")
	public static boolean hasMethod(Object obj, String methodName) 
    {
		Class[] notUsed = null;
    	return hasMethod(obj, methodName, notUsed );
    }

    /**
     * This method will determine if the object in question has a specific
     * method.
     * <br>
     * This is an alternative form of the method in which the method must have
     * matching parameters based on the supplied parameters.
     * <br>
     * NOTE: The actual parameter values are not supplied to the method, only
     * the class types of the paramters are used to determine if a method
     * exists with the same classes as parameters.
     * @param obj The object to test and see if a method exists for it.
     * @param methodName The name of the method to determine if it exists.
     * @param params The parameters supplied to the method in question.
     * @return true if the method is defined, else false.
     */
	 @SuppressWarnings("rawtypes")
	public static boolean hasMethod(Object obj, String methodName, Object[] params)
    {
		Class [ ] paramTypes = buildParameterClassArray( params );
		return hasMethod( obj, methodName, paramTypes );
	}


    /**
     * This method will determine if the object in question has a specific
     * method.
     * <br>
     * This is an alternative form of the method in which the method must have
     * matching parameters and types based on the supplied parameters types.
     * <br>
     * NOTE: The actual parameter values are not supplied to the method, only
     * the class types of the paramters are used to determine if a method
     * exists with the same classes as parameters.
     * @param obj The object to test and see if a method exists for it.
     * @param methodName The name of the method to determine if it exists.
     * @param paramTypes The parameter types to be supplied to the method
     * in question.
     * @return true if the method exists, else false.
     */
	 @SuppressWarnings("rawtypes")
	public static boolean hasMethod(Object obj, String methodName, Class[] paramTypes)
    {
		boolean rtnValue = false;
    	if ( ( obj != null ) && ( methodName != null ) && ( methodName.length( ) > 0 ) )
    	{
			try
			{
				if ( obj.getClass().getMethod(methodName, paramTypes) != null )
					rtnValue = true;
			}
			catch (Exception e)
			{
				// We don't consider this a failure, just an absense of the method, just return false.
				return false;
			}
		}
		return rtnValue;
    }

    /**
     * Invoke the setter for a property of an object for a given property name.
     * <br>
     * Note: This method assumes a single method of a given name exists for
     * target object.
     *
     * @param obj      Object on which the setter for the given property name
     *                 is invoked.
     * @param property The name of property to be set.  It is assumed that the
     *                 provided object has a setX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 setFoo( ) will be called on the object for property
     *                 named "foo".
     * @param param    A single parameter to be passed to the setter method
     *                 that is called.
     *
     * @throws RuntimeException if the setter corresponding to the name
     *         of the property cannot be invoked.
     */
    public static void invokeSetter(Object obj,String property, Object param)
        throws Exception
    {
        Object[] params = {param};
        invokeSetter( obj, property, params );
    }

    /**
     * Invoke the setter for a property of an object for a given property name.
     *
     * @param obj      Object on which the setter for the given property name
     *                 is invoked.
     * @param property The name of property to be set.  It is assumed that the
     *                 provided object has a setX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 setFoo( ) will be called on the object for property
     *                 named "foo".
     * @param params   An array of parameters to be passed to the setter method
     *                 that is called.
     *
     * @throws RuntimeException if it is not possible to invoke the setter.
     */
    @SuppressWarnings("rawtypes")
	public static void invokeSetter(Object obj,String property, Object[] params)
		throws Exception
    {
		Class[] paramTypes = buildParameterClassArray(params);
		invokeSetter(obj, property, params, paramTypes);
	}


    /**
     * Invoke the setter for a property of an object for a given property name.
     *
     * @param obj      Object on which the setter for the given property name
     *                 is invoked.
     * @param property The name of property to be set.  It is assumed that the
     *                 provided object has a setX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 setFoo( ) will be called on the object for property
     *                 named "foo".
     * @param params   An array of parameters to be passed to the setter method
     *                 that is called.
     * @param paramTypes An array of classes specifying the types of parameters
     *					required by the setter method.
     *
     * @throws RuntimeException if it is not possible to invoke the setter.
     */
    @SuppressWarnings("rawtypes")
	public static void invokeSetter(Object obj,	String property, Object[] params, Class[] paramTypes)
		throws Exception
    {
        StringBuffer name = new StringBuffer( 3 + property.length( ) );
        name.append( "set" );
        name.append( property.substring( 0, 1 ).toUpperCase( ) );
        name.append( property.substring( 1, property.length( ) ) );
        invokeMethod( obj, name.toString( ), params, paramTypes, true );
    }


    /**
     * Invoke the getter for a property of an object for a given property name.
     *
     * @param obj      Object on which the getter for the given property name
     *                 is invoked.
     * @param property The name of property to be "get".  It is assumed that the
     *                 provided object has a getX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 getVal( ) will be called on the object for property
     *                 named "val".
     *
     * @return Whatever value is returned by the getter corresponding  to the
     *         provided property name that is invoked.
     *
     * @throws RuntimeException if the getter cannot be invoked.

     */
    @SuppressWarnings("rawtypes")
	public static Object invokeGetter( Object obj, String property )
    	throws Exception
    {
		Object[] noParams = null;
		Class[] noTypes = null;
		return invokeGetter( obj, property, noParams, noTypes );
	}


    /**
     * Invoke the getter for a property of an object for a given property name.
     *
     * @param obj      Object on which the getter for the given property name
     *                 is invoked.
     * @param property The name of property to be "get".  It is assumed that the
     *                 provided object has a getX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 getVal( ) will be called on the object for property
     *                 named "Val".
     * @param param    A single parameter to be passed to the getter method
     *                 that is called.
     *
     * @return Whatever value is returned by the getter corresponding  to the
     *         provided property name that is invoked.
     *
     * @throws RuntimeException if the getter cannot be invoked.
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeGetter( Object obj, String property, Object param )
    	throws Exception
    {
		Class paramType = param.getClass( );
		return invokeGetter( obj, property, param, paramType );
	}

    /**
     * Invoke the getter for a property of an object for a given property name.
     *
     * @param obj      Object on which the getter for the given property name
     *                 is invoked.
     * @param property The name of property to be "get".  It is assumed that the
     *                 provided object has a getX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 getVal( ) will be called on the object for property
     *                 named "val".
     * @param param    A single parameter to be passed to the getter method
     *                 that is called.
     * @param paramType Class required by the getter method.
     *
     * @return Whatever value is returned by the getter corresponding  to the
     *         provided property name that is invoked.
     *
     * @throws RuntimeException if the getter cannot be invoked.
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeGetter(Object obj, String property,	Object	param, Class paramType)
    	throws Exception
    {
		Object[] params = { param };
		Class[] paramTypes = { paramType };
		return invokeGetter( obj, property, params, paramTypes );
	}

    /**
     * Invoke the getter for a property of an object for a given property name.
     *
     * @param obj      Object on which the getter for the given property name
     *                 is invoked.
     * @param property The name of property to be "get".  It is assumed that the
     *                 provided object has a getX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 getVal( ) will be called on the object for property
     *                 named "val".
     * @param params   An array of parameters to be passed to the getter method
     *                 that is called.
     *
     * @return Whatever value is returned by the getter corresponding  to the
     *         provided property name that is invoked.
     *
     * @throws RuntimeException
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeGetter(Object obj, String property, Object[] params)
    	throws Exception
    {
		Class[] paramTypes = buildParameterClassArray( params );
		return invokeGetter( obj, property, params, paramTypes );
	}


    /**
     * Invoke the getter for a property of an object for a given property name.
     *
     * @param obj      Object on which the getter for the given property name
     *                 is invoked.
     * @param property The name of property to be "get".  It is assumed that the
     *                 provided object has a getX( ) method where X is the
     *                 provided property name.  For example, the method named
     *                 getVal( ) will be called on the object for property
     *                 named "val".
     * @param params   An array of parameters to be passed to the getter method
     *                 that is called.
     * @param paramTypes Array of classes specifying the types required by the
     *					designated getter method.
     *
     * @return Whatever value is returned by the getter corresponding  to the
     *         provided property name that is invoked.
     *
     * @throws RuntimeException
     */
    @SuppressWarnings("rawtypes")
	public static Object invokeGetter(Object obj, String property, Object[]	params,	Class[]	paramTypes)
		throws Exception
    {
        StringBuffer name = new StringBuffer( 3 + property.length( ) );
        name.append( "get" );
        name.append( property.substring( 0, 1 ).toUpperCase( ) );
        name.append( property.substring( 1, property.length( ) ) );
        return invokeMethod( obj, name.toString( ), params, paramTypes, true );
    }


     /**
     * This method can be used to check to see if the provided object implements
     * a given interface.
     *
     * @param obj     The object to check.
     * @param ifcName The fully qualified name of the interface to check to see
     *                if the object implements.
     *
     * @return true if the provided object implements the provided interface
     *         name or false if it does not.
     */
    @SuppressWarnings("rawtypes")
	public static boolean implementsInterface( Object obj, String ifcName )
    {
		boolean found = false;
		Class   clazz = obj.getClass( );
		do
		{
	        Class[] interfaces = clazz.getInterfaces( );
	        for ( int i = 0; i < interfaces.length; i++ )
	        {
	            if ( interfaces[i].getName( ).equals( ifcName ) )
	            {
	                found = true;
	                break;
	            }
	        }
		}
		while ( ( found == false ) &&
		        ( ( clazz = clazz.getSuperclass( ) ) != null ) );
        return found;
    }

    /**
    **  Convert an array of parameters into a useful
    **  data set for trace logging purposes.
    **
    **  @param params Array of parameters to trace.
    **  @return String representation of the parameters comma separated.
    */
    protected static String buildParameterTrace(Object[] params)
	{
		StringBuffer tmpBuff = null;
		String		 rtnText = "<none>";
		String		 sepText = "";

		if ((params != null) && (params.length > 0))
		{
			tmpBuff = new StringBuffer(32); //guesstimate...
			tmpBuff.append( "{ " );	//  Matching }

			for ( int idx = 0; idx < params.length; idx++ )
			{
				tmpBuff.append( sepText );
				sepText = ", ";
				tmpBuff.append( params[ idx ].toString( ) );
			}
			//  Matching {
			tmpBuff.append( " }" );
			rtnText = tmpBuff.toString( );
		}
		return rtnText;
	}

	/**
	**  Convert an array of parameter types into something useful for logging.
	**
	**  @param types Array of classes used for a method.
	**  @return String representation of the array.
	*/
    @SuppressWarnings("rawtypes")
	protected static String buildTypeTrace(Class[] types)
	{
		StringBuffer	tmpBuff = null;
		String			rtnText = "<none>";
		String			sepText = "";

		if ( ( types != null ) && ( types.length > 0 ) )
		{
			tmpBuff = new StringBuffer( 32 );
			tmpBuff.append( "{ " );	//  Matching }
			for ( int idx = 0; idx < types.length; idx++ )
			{
				tmpBuff.append( sepText );
				sepText = ", ";
				tmpBuff.append( types[ idx ].getName( ) );
			}
			//  Matching {
			tmpBuff.append( " }" );
			rtnText = tmpBuff.toString( );
		}
		return rtnText;
	}

	/**
	**  Build an array of classes from the provided parameters as a
	**  helper method to all the above methods only accepting an
	**  array of parameters and not the array of classes.
	**
	**  @param paramArray Array of parameters required for the method
	**  or constructor to invoke.
	**  @return An array of the classes for each provided parameter.
	*/
    @SuppressWarnings("rawtypes")
	protected static Class[] buildParameterClassArray(Object[] paramArray)
	{
		Class[]	rtnClassArray = null;
		if ( ( paramArray != null ) && ( paramArray.length > 0 ) )
		{
			rtnClassArray = new Class[ paramArray.length ];
			for ( int idx = 0; idx < paramArray.length; idx++ )
			{
				rtnClassArray[ idx ] = paramArray[ idx ].getClass( );
			}
		}
		return rtnClassArray;
	}

}
