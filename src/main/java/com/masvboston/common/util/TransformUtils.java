package com.masvboston.common.util;

import static com.masvboston.common.util.ValidationUtils.checkEmpty;
import static com.masvboston.common.util.ValidationUtils.checkNull;
import static com.masvboston.common.util.ValidationUtils.checkNullOrEmpty;

import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class with methods for performing various useful transformations.
 * 
 * 
 * @author Mark Miller, www.masvboston.com
 * 
 */
public class TransformUtils {
	private static final String ERROR_DATE_NULL = "Date cannot be null";

	private static final String ERROR_BAD_CLASS = "Provided bean class (type) cannot be null";

	private static final String ERROR_BAD_XML = "XML cannot be null or empty";

	private static final String ERROR_BAD_TO_XML = "Error while extractign XML from bean";

	private static final String ERROR_NULL_BEAN = "Bean to convert to XML cannot be null";

	private static final String ERROR_EMPTY_BOOLEAN = "Boolean string cannot be empty";

	private static final String ERROR_NULL_BOOLEAN = "Boolean value cannot be null";

	/**
	 * {@value}
	 */
	private static final String ERROR_ON_COPY_PROPS =
			"Error while populating target with properties";

	/**
	 * {@value}
	 */
	private static final String ERROR_BAD_PROP = "Property name cannot be null or empty";

	/**
	 * {@value}
	 */
	private static final String ERROR_NULL_TIMESTAMP = "Timestamp cannot be null";

	/**
	 * This helper lookup array is used for JSON escaping. It enables fast
	 * lookup of Unicode characters. {@link #jsonEscape}.
	 */
	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F' };


	/**
	 * Constructor is private to prevent instancing.
	 */
	private TransformUtils() {

		// prevent instancing.
	}


	/**
	 * Converts the given value to it's boolean representation if possible.
	 * Handles "Y" and "N" as well as those supported by {@link BooleanUtils}.
	 * 
	 * @param aValue
	 *            The value to convert.
	 * @return True or False if conversion successful, will throw an exception
	 *         if it can't handle the value.
	 */
	public static boolean toBoolean(String value) {

		checkNull(value, ERROR_NULL_BOOLEAN);
		value = value.trim();
		checkEmpty(value, ERROR_EMPTY_BOOLEAN);

		value = value.toUpperCase();

		if (value.startsWith("Y")) {
			return true;
		}

		if (value.startsWith("N")) {
			return false;
		}

		return BooleanUtils.toBoolean(value);
	}


	/**
	 * Marshals a JavaBean to XML.
	 * 
	 * @param javaBean
	 *            The bean to convert.
	 * @return XML String representing the bean. This value is never null.
	 */
	public static <T> String toXml(final T javaBean) {

		checkNull(javaBean, ERROR_NULL_BEAN);

		try {
			JAXBContext jbCtx = JAXBContext.newInstance(javaBean.getClass());
			Marshaller marshaller = jbCtx.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);

			StringWriter writer = new StringWriter();
			marshaller.marshal(javaBean, writer);
			writer.flush();
			writer.close();
			return writer.toString();
		}
		catch (Exception e) {
			throw new IllegalStateException(ERROR_BAD_TO_XML, e);
		}
	}


	/**
	 * Unmarshal a JavaBean from an XML string.
	 * 
	 * @param xml
	 *            The XML description of the JavaBEan. Cannot be null or empty.
	 * @param beanType
	 *            The class of the output bean.
	 * @return An output bean populated with the data from the XML.
	 */
	public static <T> T fromXml(final String xml, final Class<T> beanType) {

		checkNullOrEmpty(xml, ERROR_BAD_XML);
		checkNull(beanType, ERROR_BAD_CLASS);

		try {
			JAXBContext jbCtx = JAXBContext.newInstance(beanType);
			Unmarshaller unmarshaller = jbCtx.createUnmarshaller();
			StringReader reader = new StringReader(xml);
			@SuppressWarnings("unchecked")
			T bean = (T) unmarshaller.unmarshal(reader);
			return bean;
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}


	/**
	 * Takes the given SQL Timestamp and returns a Date object.
	 * 
	 * @param timeStamp
	 *            The SQL timestamp.
	 * @return a Java Date object.
	 */
	public static Date toDate(final Timestamp timeStamp) {

		checkNull(timeStamp, ERROR_NULL_TIMESTAMP);

		long time = timeStamp.getTime();
		return new Date(time);
	}


	/**
	 * Converts a Java Date to a SQL Timestamp
	 * 
	 * @param javaDate
	 *            The java date to convert to a Timestamp.
	 * @return A Timestamp with the given date.
	 */
	public static Timestamp toTimestamp(final Date javaDate) {

		checkNull(javaDate, ERROR_DATE_NULL);
		long time = javaDate.getTime();
		return new Timestamp(time);
	}


	/**
	 * This method is used to escape strings embedded in the JSON response. The
	 * method is based on
	 * org.apache.shindig.common.JsonSerializer.appendString(). The method
	 * escapes the following in order to enable safe parsing of the JSON string:
	 * 1) single and double quotes - ' and " 2) backslash - / 3) HTML brackets -
	 * <> 4) control characters - \n \t \r .. 5) special characters - out of
	 * range unicode characters (formatted to the uxxxx format)
	 * 
	 * @param str
	 *            The original string to escape.
	 * 
	 * @return The escaped string.
	 */
	public static String jsonEscape(final String str) {

		if ((str == null) || (str.length() == 0)) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		char current;
		for (int i = 0, j = str.length(); i < j; ++i) {
			current = str.charAt(i);
			switch (current) {
				case '\'':
					sb.append("\\u0027");
					break;
				case '\"':
					sb.append("\\u0022");
					break;
				case '\\':
					sb.append('\\');
					sb.append(current);
					break;
					// We escape angle brackets in order to prevent content sniffing
					// in user agents like IE.
					// This content sniffing can potentially be used to bypass other
					// security restrictions.
				case '<':
					sb.append("\\u003c");
					break;
				case '>':
					sb.append("\\u003e");
					break;
				default:
					if ((current < ' ') || ((current >= '\u0080') && (current < '\u00a0'))
							|| ((current >= '\u2000') && (current < '\u2100'))) {
						sb.append('\\');
						switch (current) {
							case '\b':
								sb.append('b');
								break;
							case '\t':
								sb.append('t');
								break;
							case '\n':
								sb.append('n');
								break;
							case '\f':
								sb.append('f');
								break;
							case '\r':
								sb.append('r');
								break;
							default:
								// The possible alternative approaches for
								// dealing with unicode characters are
								// as follows:
								// Method 1 (from json.org.JSONObject)
								// 1. Append "000" +
								// Integer.toHexString(current)
								// 2. Truncate this value to 4 digits by using
								// value.substring(value.length() - 4)
								//
								// Method 2 (from net.sf.json.JSONObject)
								// This method is fairly unique because the
								// entire thing uses an intermediate fixed
								// size buffer of 1KB. It's an interesting
								// approach, but overall performs worse than
								// org.json
								// 1. Append "000" +
								// Integer.toHexString(current)
								// 2. Append value.charAt(value.length() - 4)
								// 2. Append value.charAt(value.length() - 3)
								// 2. Append value.charAt(value.length() - 2)
								// 2. Append value.charAt(value.length() - 1)
								//
								// Method 3 (previous experiment)
								// 1. Calculate Integer.hexString(current)
								// 2. for (int i = 0; i < 4 - value.length();
								// ++i) { buf.append('0'); }
								// 3. buf.append(value)
								//
								// Method 4 (Sun conversion from
								// java.util.Properties)
								// 1. Append '\'
								// 2. Append 'u'
								// 3. Append each of 4 octets by indexing into a
								// hex array.
								//
								// Method 5
								// Index into a single lookup table of all
								// relevant lookup values.
								sb.append('u');
								sb.append(HEX_DIGITS[(current >> 12) & 0xF]);
								sb.append(HEX_DIGITS[(current >> 8) & 0xF]);
								sb.append(HEX_DIGITS[(current >> 4) & 0xF]);
								sb.append(HEX_DIGITS[current & 0xF]);
						}
					}
					else {
						sb.append(current);
					}
			}
		}
		return sb.toString();
	}


	/**
	 * Utility method converts underscore separated strings to camelCase.
	 * 
	 * @param propName
	 *            The property name to convert.
	 * @return The string converted to camelCase.
	 */
	public static String toCamelCase(String propName) {

		ValidationUtils.checkNullOrEmpty(propName, ERROR_BAD_PROP);

		// String[] parts = propName.split(COLUMN_NAME_DELIMITER);
		// [ look for the following set
		// ^ look for what is NOT the following:
		// a-z lower case Latin
		// A-Z upper case Latin
		// 0-9 decimal numbers
		// ]+ matching 1 or more in a row
		String[] parts = propName.split("[^a-zA-Z0-9]+");

		if (parts.length > 1) {
			StringBuilder sb = new StringBuilder(StringUtils.lowerCase(parts[0]));
			for (int i = 1, n = parts.length; i < n; i++) {
				sb.append(StringUtils.capitalize(parts[i]));
			}

			propName = sb.toString();
		}
		else {
			propName = StringUtils.lowerCase(propName);
		}

		return propName;
	}


	/**
	 * Utility method that copies the properties and their values in the given
	 * map over to the target object. Properties that exist in the map but do
	 * not exist on the object are ignored. Properties that exist on the target
	 * object but do not exist in the Map remain intact.
	 * 
	 * @param properties
	 *            The Map of property and values to assign. Cannot be null.
	 * @param target
	 *            The object to assign the values to. Cannot be null.
	 * @param ignoreNullValues
	 *            Set to true to have any null values in the properties map
	 *            ignored.
	 * @return The object after having properties assigned to it. This is the
	 *         same object reference as the target parameter and is provided for
	 *         convenience.
	 */
	public static <T> T copyProperties(Map<String, String> properties, final T target,
			final boolean ignoreNullValues) {

		if (ignoreNullValues) {

			HashMap<String, String> m = new HashMap<String, String>();

			String v = null;

			for (Map.Entry<String, String> entry : properties.entrySet()) {

				v = entry.getValue();

				if (null != v) {
					m.put(entry.getKey(), v);
				}
			}

			properties = m;

		}

		try {
			BeanUtils.copyProperties(target, properties);
		}
		catch (Exception e) {
			throw new IllegalStateException(ERROR_ON_COPY_PROPS, e);
		}

		return target;
	}


	/**
	 * Utility method that changes a bean and it's properties to a map of
	 * property names and values as Strings. If an error occurs during
	 * inspection of the bean the error message becomes the value of the
	 * property.
	 * 
	 * @param bean
	 *            The bean to extract properties from. This value cannot be
	 *            null.
	 * 
	 * @param propertyMap
	 *            The map of properties and their values. This cannot be null
	 *            and it will be cleared before use.
	 * @return True if there were errors, false otherwise.
	 * @see #describe(Object, boolean, Map)
	 */
	public static boolean describe(final Object bean, final Map<String, String> propertyMap) {

		boolean thereWereErrors = 0 < describe(bean, false, propertyMap).size();

		return thereWereErrors;

	}


	/**
	 * Utility method that changes a bean and it's properties to a map of
	 * property names and values as Strings. If an error occurs during
	 * inspection of the bean the error message becomes the value of the
	 * property.
	 * 
	 * @param bean
	 *            The bean to extract properties from. This value cannot be
	 *            null.
	 * 
	 * @param ignoreErrors
	 *            Set to true to have errors NOT appear as the value of the
	 *            property in the returned property map, otherwise an error
	 *            message indicating a failure to read data will appear as the
	 *            property value.
	 * @param propertyMap
	 *            The map of properties and their values. This cannot be null
	 *            and it will be cleared before use.
	 * @param propsToOmit
	 *            List of zero or more properties to omit from the final result.
	 *            If the property does not exist, it will be ignored.
	 * @return A list of any errors, as messages, encountered during property
	 *         read. If exception encountered does not provide an error message
	 *         the the exceptions class name is provided instead.
	 */
	public static List<String> describe(final Object bean, final boolean ignoreErrors,
			final Map<String, String> propertyMap, final String... propsToOmit) {

		ValidationUtils.checkNull(bean, "Input bean cannot be null");
		ValidationUtils.checkNull(propertyMap, "Property map cannot be null");

		propertyMap.clear();

		DynaBean dbean = (bean instanceof DynaBean) ? (DynaBean) bean : new WrapDynaBean(bean);

		DynaProperty[] descriptors = dbean.getDynaClass().getDynaProperties();
		Object value = null;
		String errorMsg = "";
		ArrayList<String> errorMsgs = new ArrayList<String>();

		for (DynaProperty descriptor : descriptors) {

			String name = descriptor.getName();

			try {
				value = dbean.get(name);
				value = null == value ? null : value.toString();
				propertyMap.put(name, (String) value);
			}
			catch (Exception e) {
				errorMsg = e.getMessage();

				if (null == errorMsg) {
					errorMsg =
							"ERROR while reading property " + name + " no error message class is "
									+ e.getClass().getName();
				}
				else {
					errorMsg = "ERROR while reading property  " + name + " message is " + errorMsg;
				}

				errorMsgs.add(errorMsg);

				if (!ignoreErrors) {
					propertyMap.put(name,
							"LOG ERROR, EXCEPTION ON PROPERTY READ MSG IS: " + e.getMessage());
				}
			}
		}

		// Get rid of the "class" property.
		propertyMap.remove("class");

		for (String propName : propsToOmit) {
			propertyMap.remove(propName);
		}

		return errorMsgs;

	}


	/**
	 * This is a variation of the {@link #describe(Object, Map)} method that
	 * will accept beans values and returns a map of property names and values.
	 * If a null bean is given this method returns an empty map. If there are
	 * any errors during the reading of properties, those errors appear as the
	 * properties value.
	 * 
	 * @param bean
	 *            The bean to describe
	 * @return A map of all the properties and values, if any, never null.
	 * @see #describe(Object, Map)
	 */
	public static Map<String, String> describe(final Object bean) {

		Map<String, String> description = new HashMap<String, String>();

		if (bean == null) {
			return description;
		}

		describe(bean, description);

		return description;

	}

}
