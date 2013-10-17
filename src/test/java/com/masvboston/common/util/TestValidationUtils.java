package com.masvboston.common.util;

import static com.masvboston.common.util.ValidationUtils.checkEmpty;
import static com.masvboston.common.util.ValidationUtils.checkForDuplicates;
import static com.masvboston.common.util.ValidationUtils.checkNull;
import static com.masvboston.common.util.ValidationUtils.checkPropertiesForValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class TestValidationUtils {

	private static final String MSG_FAILED = "failed";


	@Test
	public void testCheckEmptyCollectionOfQ() {

		Collection<String> c = new ArrayList<String>();

		c.add("A");

		checkEmpty(c);

		c.clear();

		try {
			checkEmpty(c);
			fail();
		}
		catch (IllegalArgumentException e) {
			// pass
		}
	}


	@Test
	public void testCheckEmptyCollectionOfQString() {

		Collection<String> c = new ArrayList<String>();

		c.add("A");

		checkEmpty(c, "Failed");

		c.clear();

		try {
			checkEmpty(c, "Failed");
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());
		}
	}


	@Test
	public void testCheckEmptyObjectArray() {

		String[] a = { "a" };
		String[] b = {};

		checkEmpty(a);

		try {
			checkEmpty(b);
			fail();
		}
		catch (IllegalArgumentException e) {
			// Pass
		}
	}


	@Test
	public void testCheckEmptyObjectArrayString() {
		String[] a = { "a" };
		String[] b = {};

		checkEmpty(a, MSG_FAILED);

		try {
			checkEmpty(b, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());

		}
	}


	@Test
	public void testCheckEmptyString() {
		checkEmpty("a");

		try {
			checkEmpty("");
			fail();
		}
		catch (IllegalArgumentException e) {
			// Pass
		}
	}


	@Test
	public void testCheckEmptyStringString() {
		checkEmpty("a", MSG_FAILED);

		try {
			checkEmpty("", MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());
		}
	}


	@Test
	public void testCheckForDuplicatesCollectionOfQString() {
		Collection<String> c = new ArrayList<String>();
		c.add("a");
		c.add("b");

		checkForDuplicates(c, MSG_FAILED);

		try {
			c.add("a");
			checkForDuplicates(c, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());
		}
	}


	@Test
	public void testCheckForDuplicatesObjectArrayStringArray() {

		String[] a = { "a", "b" };
		String[] b = { "a", "b", "a" };

		checkForDuplicates(a, MSG_FAILED);

		try {
			checkForDuplicates(b, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());
		}
	}


	@Test
	public void testCheckNullCollectionOfQ() {

		Collection<String> c = new ArrayList<String>();
		c.add("a");
		c.add("b");

		checkNull(c);

		try {
			c.add(null);
			checkNull(c);
			fail();
		}
		catch (IllegalArgumentException e) {
			// pass
		}

		try {
			c = null;
			checkNull(c);
			fail();
		}
		catch (IllegalArgumentException e) {

			// pass
		}

	}


	@Test
	public void testCheckNullCollectionOfQString() {
		Collection<String> c = new ArrayList<String>();
		c.add("a");
		c.add("b");

		checkNull(c, MSG_FAILED);

		try {
			c.add(null);
			checkNull(c, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith(MSG_FAILED));
		}

		try {
			c = null;
			checkNull(c, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith(MSG_FAILED));
		}
	}


	@Test
	public void testCheckNullObject() {

		Object o = "a";

		checkNull(o);

		try {
			o = null;
			checkNull(o);
			fail();

		}
		catch (IllegalArgumentException e) {

			// pass
		}

	}


	@Test
	public void testCheckNullObjectString() {
		Object o = "a";

		checkNull(o, MSG_FAILED);

		try {
			o = null;
			checkNull(o, MSG_FAILED);
			fail();

		}
		catch (IllegalArgumentException e) {
			assertEquals(MSG_FAILED, e.getMessage());
		}
	}


	@Test
	public void testCheckNullObjectArray() {
		Object[] o = { "a", "b" };

		checkNull(o);

		try {
			o[1] = null;
			checkNull(o);
			fail();

		}
		catch (IllegalArgumentException e) {
			// pass
		}

		try {
			o = null;
			checkNull(o);
			fail();
		}
		catch (IllegalArgumentException e) {
			// pass
		}

	}


	@Test
	public void testCheckNullObjectArrayString() {
		Object[] o = { "a", "b" };

		checkNull(o);

		try {
			o[1] = null;
			checkNull(o, MSG_FAILED);
			fail();

		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith(MSG_FAILED));
		}

		try {
			o = null;
			checkNull(o, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().startsWith(MSG_FAILED));
		}
	}


	@Test
	public void testCheckNullOrEmptyString() {

		String a = "a";
		String n = null;
		String e = "";

		checkNull(a);

		try {
			checkNull(n);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// pass
		}

		try {
			checkNull(e);
			fail();
		}
		catch (IllegalArgumentException ex) {
			// pass
		}
	}


	@Test
	public void testCheckNullOrEmptyStringString() {
		String a = "a";
		String n = null;
		String e = "";

		checkNull(a, MSG_FAILED);

		try {
			checkNull(n, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException ex) {
			assertEquals(MSG_FAILED, ex.getMessage());
		}

		try {
			checkNull(e, MSG_FAILED);
			fail();
		}
		catch (IllegalArgumentException ex) {
			assertEquals(MSG_FAILED, ex.getMessage());
		}
	}

	/**
	 * Have to make class public otherwise reflection does not work.
	 * 
	 */
	public static class TestClass {
		public String getValue() {
			return "a";
		};


		public String getBadValue() {
			return null;
		}


		public void setValue(final String s) {
			// do nothing.
		}
	};


	@Test
	public void testCheckPropertiesForValuesObjectStringArray() {

		TestClass o = new TestClass();

		checkPropertiesForValues(o, "value");

		try {
			checkPropertiesForValues(o, "value", "badValue");
			fail();
		}
		catch (IllegalArgumentException ex) {
			assertEquals("Bean is missing values for properties | badValue", ex.getMessage().trim());
		}


		try {
			checkPropertiesForValues(o, "value", "bogus");
			fail();
		}
		catch (IllegalArgumentException ex) {
			assertEquals("The bean does not have the property: bogus", ex.getMessage().trim());
		}

	}


	@Test
	public void testCheckPropertiesForValuesObjectStringStringArray() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeDoubleDoubleDouble() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeDoubleDoubleDoubleString() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeIntIntegerInteger() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeIntIntegerIntegerString() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeLongLongLong() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testCheckRangeLongLongLongString() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testExtractOriginalException() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsEmptyCollectionOfQ() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsEmptyObjectArray() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsEmptyString() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsNull() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsNullOrEmptyCollectionOfQ() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsNullOrEmptyObjectArray() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testIsNullOrEmptyString() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testSearchForException() {
		fail("Not yet implemented"); // TODO
	}


	@Test
	public void testStackTraceAsString() {
		fail("Not yet implemented"); // TODO
	}

}
