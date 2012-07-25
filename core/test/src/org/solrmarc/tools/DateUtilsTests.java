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
}
