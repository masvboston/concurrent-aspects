package com.masvboston.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class with methods for various value and range checks. This class provides a
 * number of very useful validation routines that occur often in code. The
 * majority of these methods throw exceptions. These use the naming convention
 * of "check..." preceding the method name.
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class ValidationUtils {
	/**
	 * {@value}
	 */
	private static final String ERROR_EXCEPTION_NULL = "Must provide an exception to search";

	/**
	 * {@value}
	 */
	private static final String ERROR_PROPS_EMPTY = "Property names cannot be empty";

	/**
	 * {@value}
	 */
	private static final String ERROR_PROP_NULL = "Property names cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_PROP_MISSING = "The bean does not have the property: ";

	/**
	 * {@value}
	 */
	private static final String ERROR_PROP_DESC_BLANK = "Property description is blank for ";

	/**
	 * {@value}
	 */
	private static final String ERROR_PROP_BLANK = "Property specified is blank";

	/**
	 * {@value}
	 */
	private static final String ERROR_BEAN_PROP_MISSING =
			"Bean is missing values for properties | ";

	/**
	 * {@value}
	 */
	private static final String ERROR_COLLECTION_EMPTY = "Collection is empty";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_MSG = "Error message cannot be null or empty";

	/**
	 * {@value}
	 */
	public static final String ERROR_DUPES = "Duplicates found";

	/**
	 * {@value}
	 */
	public static final String ERROR_EXCEPTION_ARG_NULL = "Error argument cannot be null";

	/**
	 * {@value}
	 */
	public static final String ERROR_STR_NULL = "String cannot be null";

	/**
	 * {@value}
	 */
	public static final String ERROR_ARY_EMPTY = "Array is empty";

	/**
	 * {@value}
	 */
	public static final String ERROR_STR_EMPTY = "String is empty";

	/**
	 * {@value}
	 */
	public static final String ERROR_OUT_OF_RANGE =
			"Value of {0} is not in the range of min of {1} and a max of {2}";

	/**
	 * {@value}
	 */
	public static final String ERROR_VALUE_NULL = "Value is null";

	/**
	 * 
	 * @see #checkPropertiesForValues(Object, String, String...)
	 */
	private static final String PROPERTY_ATTR_DELIMITER = "|";

	/**
	 * {@value}
	 */
	private static final String PROPERTY_DELIM_REGEX = "\\|";


	/**
	 * Checks the given collection to see if it is empty.
	 * 
	 * @param value
	 *            Collection to check. Does not check for null.
	 * @param errorMessage
	 *            Optional error message to show.
	 * @throws IllegalArgumentException
	 *             The given collection is empty.
	 */
	public static void checkEmpty(final Collection<?> value) {

		checkEmpty(value, ERROR_COLLECTION_EMPTY);
	}


	/**
	 * Checks the given collection to see if it is empty.
	 * 
	 * @param value
	 *            Collection to check. Does not check for null.
	 * @param errorMessage
	 *            Optional error message to show.
	 * @throws IllegalArgumentException
	 *             The given collection is empty.
	 */
	public static void checkEmpty(final Collection<?> value, final String errorMessage) {

		checkNullOrEmpty(errorMessage, ERROR_BAD_MSG);

		if (isEmpty(value)) {
			throw new IllegalArgumentException(errorMessage);
		}
	}


	/**
	 * Checks the given array to see if it is empty.
	 * 
	 * @param value
	 *            Array to check. Does not check for null.
	 * @throws IllegalArgumentException
	 *             The given array length is zero.
	 */
	public static void checkEmpty(final Object[] value) {

		checkEmpty(Arrays.asList(value), ERROR_ARY_EMPTY);
	}


	/**
	 * Checks the given array to see if it is empty.
	 * 
	 * @param value
	 *            Array to check. Does not check for null.
	 * @param errorMessage
	 *            Optional error message to show.
	 * @throws IllegalArgumentException
	 *             The given array length is zero.
	 */
	public static void checkEmpty(final Object[] value, final String errorMessage) {

		checkEmpty(Arrays.asList(value), errorMessage);
	}


	/**
	 * Checks the given String to see if it is empty.
	 * 
	 * @param value
	 *            String to check. Does not check for null.
	 */
	public static void checkEmpty(final String value) {
		checkEmpty(value, ERROR_STR_EMPTY);
	}


	/**
	 * Checks the given String to see if it is empty.
	 * 
	 * @param value
	 *            String to check. Does not check for null.
	 * @param errorMessage
	 *            Error message to show. Cannot be null or empty.
	 */
	public static void checkEmpty(final String value, final String errorMessage) {

		if (isNullOrEmpty(errorMessage)) {
			throw new IllegalArgumentException(ERROR_BAD_MSG);
		}

		if (isEmpty(value)) {
			throw new IllegalArgumentException(errorMessage);
		}
	}


	/**
	 * Checks given collection for duplicates and throws an exception if
	 * duplicates are found. The error message identifies the duplicates. The
	 * duplicate values are appended to the given error message if supplied or
	 * the default error message, otherwise.
	 * 
	 * @param values
	 *            The values to check. Given set cannot be null nor can any of
	 *            the values. This routine does not check the given collection
	 *            to see if it is null or has any null values. use
	 *            {@link #checkNull(Collection)} and
	 *            {@link #checkEmpty(Collection)} to assurance before calling
	 *            this method.
	 * @param aMsg
	 *            The error message to use. Cannot be null or empty.
	 */
	public static void checkForDuplicates(final Collection<?> values, final String errorMessage) {

		if (isNullOrEmpty(errorMessage)) {
			throw new IllegalArgumentException(ERROR_BAD_MSG);
		}

		/*
		 * Copy the contents into a collection we can control because we
		 * shouldn't modify the given collection.
		 */
		ArrayList<Object> source = new ArrayList<Object>(values);

		HashSet<Object> set = new HashSet<Object>();
		set.addAll(source);

		if (source.size() > set.size()) {

			source.removeAll(set);

			String items = source.toString();

			throw new IllegalArgumentException(errorMessage + ": " + items);
		}

	}


	/**
	 * Checks given array for duplicates and throws an exception if duplicates
	 * are found. The error message identifies the duplicates. The duplicate
	 * values are appended to the given error message if supplied or the default
	 * error message, otherwise.
	 * 
	 * @param values
	 *            The values to check. Given set cannot be null nor can any of
	 *            the values. This routine does not check the given collection
	 *            to see if it is null or has any null values. use
	 *            {@link #checkNull(Collection)} and
	 *            {@link #checkEmpty(Collection)} to assurance before calling
	 *            this method.
	 * 
	 * @param aMsg
	 *            An optional error message to use instead of the default of
	 *            {@value #ERROR_DUPES}
	 */
	public static void checkForDuplicates(final Object[] values, final String... errorMessage) {

		checkForDuplicates(Arrays.asList(values), ERROR_DUPES);
	}


	/**
	 * Checks the given collection to see if it or any of it's values are null.
	 * 
	 * @param values
	 *            collection to check.
	 * 
	 * @throws IllegalArgumentException
	 *             The given collection is null or there are null values.
	 */
	public static void checkNull(final Collection<?> values) {
		checkNull(values, ERROR_VALUE_NULL);
	}


	/**
	 * Checks the given collection to see if it or any of it's values are null.
	 * 
	 * @param values
	 *            collection to check.
	 * @param errorMessage
	 *            The error message to use. The index of the null is appended to
	 *            the message.
	 * @throws IllegalArgumentException
	 *             The given collection is null or there are null values.
	 */
	public static void checkNull(final Collection<?> values, String errorMessage) {

		if (isNullOrEmpty(errorMessage)) {
			throw new IllegalArgumentException(ERROR_BAD_MSG);
		}

		if (null == values) {
			throw new IllegalArgumentException(errorMessage);
		}

		errorMessage += " at index ";

		int i = 0;

		for (Object value : values) {
			if (null == value) {
				throw new IllegalArgumentException(errorMessage + i);
			}
			i++;
		}
	}


	/**
	 * Checks the given object and throws and exception if it is null.
	 * 
	 * @param value
	 *            the value to check.
	 * 
	 * @throws IllegalArgumentException
	 *             The value was null.
	 */
	public static void checkNull(final Object value) {
		checkNull(value, ERROR_VALUE_NULL);
	}


	/**
	 * Checks the given object and throws and exception if it is null.
	 * 
	 * @param value
	 *            the value to check.
	 * @param errorMessage
	 *            Message to use . Cannot be null or empty.
	 * @throws IllegalArgumentException
	 *             The value was null.
	 */
	public static void checkNull(final Object value, final String errorMessage) {

		if (isNullOrEmpty(errorMessage)) {
			throw new IllegalArgumentException(ERROR_BAD_MSG);
		}

		if (null == value) {
			throw new IllegalArgumentException(errorMessage);
		}
	}


	/**
	 * Checks the given array to see if it or any of it's values are null.
	 * 
	 * @param values
	 *            Array to check.
	 * 
	 * @throws IllegalArgumentException
	 *             The given array is null or there are null values.
	 */
	public static void checkNull(final Object[] values) {
		checkNull(values, ERROR_VALUE_NULL);
	}


	/**
	 * Checks the given array to see if it or any of it's values are null.
	 * 
	 * @param values
	 *            Array to check.
	 * @param errorMessage
	 *            The error message to use. The index of the null is appended to
	 *            the message.
	 * @throws IllegalArgumentException
	 *             The given array is null or the length is zero.
	 */
	public static void checkNull(final Object[] values, final String errorMessage) {

		if (isNullOrEmpty(errorMessage)) {
			throw new IllegalArgumentException(ERROR_BAD_MSG);
		}

		if (null == values) {
			throw new IllegalArgumentException(errorMessage);
		}

		checkNull(Arrays.asList(values), errorMessage);
	}


	/**
	 * Convenience method to check if a String is null or empty.
	 * 
	 * @param value
	 *            The String to check.
	 * 
	 * @throws IllegalArgumentException
	 *             The given collection is null or there are null values.
	 */
	public static void checkNullOrEmpty(final String value) {

		checkNull(value);
		checkEmpty(value);
	}


	/**
	 * Convenience method to check if a String is null or empty. T
	 * 
	 * @param value
	 *            The String to check.
	 * @param errorMessage
	 *            The error message to throw. Cannot be null or empty.
	 * @throws IllegalArgumentException
	 *             The given collection is null or there are null values.
	 */
	public static void checkNullOrEmpty(final String value, final String errorMessage) {

		checkNull(value, ERROR_BAD_MSG);

		checkEmpty(errorMessage, ERROR_BAD_MSG);

		checkNull(value, errorMessage);

		checkEmpty(value, errorMessage);
	}


	/**
	 * Utility method to check for the existence of property values in a given
	 * Java bean. Also checks to ensure the given bean has the property being
	 * checked.
	 * 
	 * @param inputBean
	 *            The Java bean to check.
	 * @param propertyName
	 *            A property to check for a value.
	 * @param propertyNames
	 *            1 or more additional property names to check to ensure they
	 *            have a value. Property names can optionally specify a property
	 *            description using a {@link #PROPERTY_ATTR_DELIMITER}
	 *            delimiter. If provided, the description is the value that will
	 *            appear in the error message otherwise the property name is
	 *            used. This means you can us the property description for
	 *            property name substitution in the outbound error message. For
	 *            example: aknUserName | attributeUserName
	 * 
	 */
	public static void checkPropertiesForValues(final Object inputBean,
			final String... propertyNames) {
		checkPropertiesForValues(ERROR_BEAN_PROP_MISSING, inputBean, propertyNames);
	}


	/**
	 * Utility method to check for the existence of property values in a given
	 * Java bean. Also checks to ensure the given bean has the property being
	 * checked.
	 * 
	 * @param errorMsg
	 *            The error message to throw in addition to the list of
	 *            properties that are missing values. A <strong>null</strong>
	 *            value will have the method use the default, an empty String
	 *            will cause the default message to <strong>not</strong> be
	 *            used. *
	 * @param inputBean
	 *            The Java bean to check.
	 * 
	 * @param propertyName
	 *            A property name to check.
	 * @param propertyNames
	 *            1 or more additional property names to check to ensure they
	 *            have a value. Property names can optionally specify a property
	 *            description using a {@link #PROPERTY_ATTR_DELIMITER}
	 *            delimiter. If provided, the description is the value that will
	 *            appear in the error message otherwise the property name is
	 *            used. This means you can us the property description for
	 *            property name substitution in the outbound error message. For
	 *            example: aknUserName | attributeUserName
	 * 
	 */
	public static void checkPropertiesForValues(final String errorMsg, final Object inputBean,
			final String... propertyNames) {

		/*
		 * Check all the input parameters.
		 */
		checkNull(propertyNames, ERROR_PROP_NULL);
		checkEmpty(propertyNames, ERROR_PROPS_EMPTY);
		checkNullOrEmpty(errorMsg, ERROR_BAD_MSG);

		for (String property : propertyNames) {
			checkNullOrEmpty(property, "Property name cannot be null or empty");
		}

		/*
		 * Get all the properties and their values and check that the property
		 * actually has the value.
		 */
		HashMap<String, String> properties = new HashMap<String, String>();
		TransformUtils.describe(inputBean,true, properties);
		StringBuilder errorMessage = new StringBuilder();

		String value = null;
		String propertyDesc = null;
		String[] propertyAttributes = null;

		for (String property : propertyNames) {

			if (-1 == property.indexOf(PROPERTY_ATTR_DELIMITER)) {

				/*
				 * Set the property description to be the same as the property
				 * name.
				 */
				propertyDesc = property.trim();
			}
			else {

				propertyAttributes = property.split(PROPERTY_DELIM_REGEX);
				property = propertyAttributes[0].trim();
				propertyDesc = propertyAttributes[1].trim();

				checkEmpty(property, ERROR_PROP_BLANK);
				checkEmpty(propertyDesc, ERROR_PROP_DESC_BLANK + property);

			}

			if (!properties.containsKey(property)) {
				throw new IllegalArgumentException(ERROR_PROP_MISSING + propertyDesc);
			}

			value = properties.get(property);

			if ((null == value) || (0 == value.trim().length())) {
				errorMessage.append(propertyDesc).append("\n");
			}
		}

		if (0 < errorMessage.length()) {
			throw new IllegalArgumentException(errorMsg + errorMessage.toString());
		}

	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * 
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final double value, final Double min, final Double max) {

		checkRange(value, min, max, ERROR_OUT_OF_RANGE);
	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Cannot be null or empty.
	 * 
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final double value, Double min, Double max,
			final String errorMessage) {

		checkNullOrEmpty(errorMessage, ERROR_BAD_MSG);

		min = (null == min) ? Double.MIN_NORMAL : min;
		max = (null == max) ? Double.MAX_VALUE : max;

		if ((value < min) || (value > max)) {
			String msg = MessageFormat.format(errorMessage, value, min, max);
			throw new IllegalArgumentException(msg);
		}
	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * 
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final int value, Integer min, Integer max) {

		min = (null == min) ? Integer.MIN_VALUE : min;
		max = (null == max) ? Integer.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max));

	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Cannot be null or empty.
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final int value, Integer min, Integer max,
			final String errorMessage) {

		min = (null == min) ? Integer.MIN_VALUE : min;
		max = (null == max) ? Integer.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max), errorMessage);

	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final long value, Long min, Long max) {

		min = (null == min) ? Long.MIN_VALUE : min;
		max = (null == max) ? Long.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max));

	}


	/**
	 * Check the given value to see if it falls within the given min and max,
	 * inclusive, of the given values.
	 * 
	 * @param value
	 *            The value to check.
	 * @param min
	 *            The minimum value, inclusive for the value, pass null for no
	 *            min.
	 * @param max
	 *            The maximum value , inclusive for the value, pass null for no
	 *            max.
	 * @param errorMessage
	 *            The error message to throw if the value is out of the range.
	 *            Cannot be null or empty.
	 * 
	 * @throws IllegalArgumentException
	 *             The value falls outside the given range.
	 */
	public static void checkRange(final long value, Long min, Long max, final String errorMessage) {

		min = (null == min) ? Long.MIN_VALUE : min;
		max = (null == max) ? Long.MAX_VALUE : max;

		checkRange(Double.valueOf(value), Double.valueOf(min), Double.valueOf(max), errorMessage);

	}


	/**
	 * Given an exception, this method retrieves the original exception that
	 * caused the error in the first place. This is not the same as
	 * Exception.getCause, which only returns the exception that lead to the one
	 * you have in hand.
	 * 
	 * @param aException
	 *            The exception to search. Cannot be null.
	 * @return The originating exception.
	 */
	public static Throwable extractOriginalException(final Throwable aException) {

		checkNull(aException, ERROR_EXCEPTION_NULL);

		Throwable cause = aException.getCause();

		/*
		 * This method uses recursion.
		 */

		return null == cause ? aException : extractOriginalException(cause);
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test. Does not check to see if it is null.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final Collection<?> value) {

		return 0 == value.size();
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test. Does not check to see if it is null.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final Object[] value) {

		return 0 == value.length;
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test. Does not check to see if it is null.
	 * @return True if empty.
	 */
	public static boolean isEmpty(final String value) {

		return 0 == value.length();
	}


	/**
	 * Method to check for null. This is here just for completeness.
	 * 
	 * @param arg
	 *            Any object.
	 * @return True if null false if not.
	 */
	public static boolean isNull(final Object arg) {

		return null == arg;
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final Collection<?> value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final Object[] value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Tests given value to see if it is empty.
	 * 
	 * @param value
	 *            Value to test.
	 * @return True if empty.
	 */
	public static boolean isNullOrEmpty(final String value) {

		return isNull(value) || isEmpty(value);
	}


	/**
	 * Utility routine that searches an exception chain for the first instance
	 * of the given exception. If the given exception matches the given type
	 * then this method will return the given exception.
	 * 
	 * @param aException
	 *            The exception to search through. Cannot be null.
	 * @param aExceptionClazz
	 *            The exception class to find. Cannot be null.
	 * @return The instance of the exception class or null if none found.
	 */
	public static <E extends Throwable> Throwable searchForException(final Throwable aException,
			final Class<E> aExceptionClazz) {

		checkNull(aException);
		checkNull(aExceptionClazz);

		if (aException.getClass() == aExceptionClazz) {
			return aException;
		}

		Throwable cause = aException.getCause();

		/*
		 * This method uses recursion.
		 */

		return null == cause ? null : searchForException(cause, aExceptionClazz);

	}


	/**
	 * Extracts the given errors stack trace as a string.
	 * 
	 * @param aError
	 *            The error to process.
	 * @return A string form of the stack trace.
	 */
	public static String stackTraceAsString(final Throwable aError) {

		checkNull(aError, ERROR_EXCEPTION_ARG_NULL);
		StringWriter sw = new StringWriter();
		PrintWriter w = new PrintWriter(sw);

		aError.printStackTrace(w);
		w.flush();
		w.close();

		return sw.toString();
	}

}
