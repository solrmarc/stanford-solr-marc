package org.solrmarc.tools;

import static org.junit.Assert.*;
import static org.solrmarc.tools.DateUtils.*;

import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.Test;

/**
 * Unit tests for DateUtils class
 * @author Naomi Dushay
 *
 */
public class DateUtilsTests
{

	/**
	 * unit test for org.solrmarc.tools.DateUtils.cleanDate
	 */
@Test
	public void testCleanDate()
	{
		// at most 1 year ahead of current year
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
		String thisYear = dateFormat.format(calendar.getTime());
		int nextYear = Integer.parseInt(thisYear) + 1;
		String nextYearStr = String.valueOf(nextYear);

		// patterns that should be recognized  (no earlier than 15xx)
		for (String validYear : Arrays.asList(nextYearStr, "2002", "1980", "1577", "1293", "1020", "0965", "0500"))
		{
			assertEquals(validYear, getYearFromString(validYear));
			// patterns the method copes with
			assertEquals(validYear, getYearFromString('[' + validYear + ']'));
			assertEquals(validYear, getYearFromString('[' + validYear + " text]"));
			assertEquals(validYear, getYearFromString('[' + validYear + " ?]"));
			assertEquals(validYear, getYearFromString('Ⓟ' + validYear));
			assertEquals(validYear, getYearFromString('p' + validYear));
			assertEquals(validYear, getYearFromString('©' + validYear));
			assertEquals(validYear, getYearFromString('c' + validYear));
			assertEquals(validYear, getYearFromString(validYear + '.'));
		}
		// lowercase L instead of 1
		assertEquals("1957", getYearFromString("l957"));
		// braces around 19
		assertEquals("1957", getYearFromString("[19]57"));
		// last digit is ? or -
		assertEquals("1950", getYearFromString("195?"));
		assertEquals("1950", getYearFromString("195-"));
		// preceded by i.e.
		assertEquals("1957", getYearFromString("i.e. 1957"));


		// unrecognizable dates
		String yearAfterStr = String.valueOf(nextYear + 1);
		for (String invalidYear : Arrays.asList(yearAfterStr, "3047",  "522", "0000", "ab", "19555"))
		{
			String result = getYearFromString(invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('[' + invalidYear + ']');
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('[' + invalidYear + " text]");
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('Ⓟ' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('p' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('©' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = getYearFromString('c' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
		}
		// followed by b.c.
		assertNull(getYearFromString("1957 bc"));
		assertNull(getYearFromString("1957 bc."));
		assertNull(getYearFromString("1957 b.c."));
		assertNull(getYearFromString("1957 BC"));
		assertNull(getYearFromString("1957 BC."));
		assertNull(getYearFromString("1957 B.C."));
		assertNull(getYearFromString("50 bc"));
		assertNull(getYearFromString("50 bc."));
		assertNull(getYearFromString("50 b.c."));
		assertNull(getYearFromString("50 BC"));
		assertNull(getYearFromString("50 BC."));
		assertNull(getYearFromString("50 B.C."));
		assertNull(getYearFromString("20000 bc"));
		assertNull(getYearFromString("20000 bc."));
		assertNull(getYearFromString("20000 b.c."));
		assertNull(getYearFromString("20000 BC"));
		assertNull(getYearFromString("20000 BC."));
		assertNull(getYearFromString("20000 B.C."));
	}


	/**
	 * unit test for org.solrmarc.tools.DateUtils.getCenturyString
	 */
@Test
	public void testGetCenturyString()
	{
		assertEquals("1st century", getCenturyString("00"));
		assertEquals("2nd century", getCenturyString("01"));
		assertEquals("3rd century", getCenturyString("02"));
		assertEquals("4th century", getCenturyString("03"));
		assertEquals("5th century", getCenturyString("04"));
		assertEquals("6th century", getCenturyString("05"));
		assertEquals("7th century", getCenturyString("06"));
		assertEquals("8th century", getCenturyString("07"));
		assertEquals("9th century", getCenturyString("08"));
		assertEquals("10th century", getCenturyString("09"));
		assertEquals("11th century", getCenturyString("10"));
		assertEquals("12th century", getCenturyString("11"));
		assertEquals("13th century", getCenturyString("12"));
		assertEquals("14th century", getCenturyString("13"));
		assertEquals("15th century", getCenturyString("14"));
		assertEquals("16th century", getCenturyString("15"));
		assertEquals("17th century", getCenturyString("16"));
		assertEquals("18th century", getCenturyString("17"));
		assertEquals("19th century", getCenturyString("18"));
		assertEquals("20th century", getCenturyString("19"));
		assertEquals("21st century", getCenturyString("20"));
	}


	/**
	 * unit test for org.solrmarc.tools.DateUtils.getNumberSuffix
	 */
@Test
	public void testGetNumberSuffix()
	{
		assertEquals("st", getNumberSuffix("01"));
		assertEquals("st", getNumberSuffix("1"));
		assertEquals("nd", getNumberSuffix("02"));
		assertEquals("nd", getNumberSuffix("2"));
		assertEquals("rd", getNumberSuffix("03"));
		assertEquals("rd", getNumberSuffix("3"));
		assertEquals("th", getNumberSuffix("04"));
		assertEquals("th", getNumberSuffix("4"));
		assertEquals("th", getNumberSuffix("10"));
		assertEquals("th", getNumberSuffix("11"));
		assertEquals("th", getNumberSuffix("19"));
		assertEquals("th", getNumberSuffix("20"));
		assertEquals("st", getNumberSuffix("21"));
		assertEquals("nd", getNumberSuffix("22"));
		assertEquals("rd", getNumberSuffix("23"));
		assertEquals("th", getNumberSuffix("24"));
	}


	/**
	 * unit test for org.solrmarc.tools.DateUtils.isdddd
	 */
@Test
	public void testIsdddd()
	{
		assertTrue("Unexpectedly got false from isdddd for 1234", isdddd("1234"));
		assertFalse("Unexpectedly got true from isdddd for 123u", isdddd("123u"));
		assertFalse("Unexpectedly got true from isdddd for 12uu", isdddd("12uu"));
		assertFalse("Unexpectedly got true from isdddd for 1uuu", isdddd("1uuu"));
		assertFalse("Unexpectedly got true from isdddd for 123-", isdddd("123-"));
		assertFalse("Unexpectedly got true from isdddd for 12--", isdddd("12--"));
		assertFalse("Unexpectedly got true from isdddd for 1---", isdddd("1---"));
	}

	/**
	 * unit test for org.solrmarc.tools.DateUtils.isdddu
	 */
@Test
	public void testIsdddu()
	{
		assertFalse("Unexpectedly got true from isdddu for 1234", isdddu("1234"));
		assertTrue("Unexpectedly got false from isdddu for 123u", isdddu("123u"));
		assertFalse("Unexpectedly got true from isdddu for 12uu", isdddu("12uu"));
		assertFalse("Unexpectedly got true from isdddu for 1uuu", isdddu("1uuu"));
		assertTrue("Unexpectedly got false from isdddu for 123-", isdddu("123-"));
		assertFalse("Unexpectedly got true from isdddu for 12--", isdddu("12--"));
		assertFalse("Unexpectedly got true from isdddu for 1---", isdddu("1---"));
	}

	/**
	 * unit test for org.solrmarc.tools.DateUtils.isdduu
	 */
@Test
	public void testIsdduu()
	{
		assertFalse("Unexpectedly got true from isdduu for 1234", isdduu("1234"));
		assertFalse("Unexpectedly got true from isdduu for 123u", isdduu("123u"));
		assertTrue("Unexpectedly got false from isdduu for 12uu", isdduu("12uu"));
		assertFalse("Unexpectedly got true from isdduu for 1uuu", isdduu("1uuu"));
		assertFalse("Unexpectedly got true from isdduu for 123-", isdduu("123-"));
		assertTrue("Unexpectedly got false from isdduu for 12--", isdduu("12--"));
		assertFalse("Unexpectedly got true from isdduu for 1---", isdduu("1---"));
	}

	/**
	 * unit test for org.solrmarc.tools.DateUtils.isduuu
	 */
@Test
	public void testIsduuu()
	{
		assertFalse("Unexpectedly got true from isduuu for 1234", isduuu("1234"));
		assertFalse("Unexpectedly got true from isduuu for 123u", isduuu("123u"));
		assertFalse("Unexpectedly got true from isduuu for 12uu", isduuu("12uu"));
		assertTrue("Unexpectedly got false from isduuu for 1uuu", isduuu("1uuu"));
		assertFalse("Unexpectedly got true from isduuu for 123-", isduuu("123-"));
		assertFalse("Unexpectedly got true from isduuu for 12--", isduuu("12--"));
		assertTrue("Unexpectedly got false from isduuu for 1---", isduuu("1---"));
	}
}
