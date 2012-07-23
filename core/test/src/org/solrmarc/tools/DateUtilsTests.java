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
		for (String validYear : Arrays.asList(nextYearStr, "2002", "1980", "1577"))
		{
			assertEquals(validYear, cleanDate(validYear));
			// patterns the method copes with
			assertEquals(validYear, cleanDate('[' + validYear + ']'));
			assertEquals(validYear, cleanDate('[' + validYear + " text]"));
			assertEquals(validYear, cleanDate('Ⓟ' + validYear));
			assertEquals(validYear, cleanDate('p' + validYear));
			assertEquals(validYear, cleanDate('©' + validYear));
			assertEquals(validYear, cleanDate('c' + validYear));
		}
		// lowercase L instead of 1
		assertEquals("1957", cleanDate("l957"));
		// braces around 19
		assertEquals("1957", cleanDate("[19]57"));
		// last digit is ? or -
		assertEquals("1950", cleanDate("195?"));
		assertEquals("1950", cleanDate("195-"));
		// preceded by i.e.
		assertEquals("1957", cleanDate("i.e. 1957"));


		// unrecognizable dates
		String yearAfterStr = String.valueOf(nextYear + 1);
		for (String invalidYear : Arrays.asList(yearAfterStr, "3047", "1293", "0965", "522", "0000", "ab", "19555"))
		{
			String result = cleanDate(invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('[' + invalidYear + ']');
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('[' + invalidYear + " text]");
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('Ⓟ' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('p' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('©' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
			result = cleanDate('c' + invalidYear);
			assertNull("Unexpectedly got " + result + " for " + invalidYear, result);
		}
		// followed by b.c.
		assertNull(cleanDate("1957 bc"));
		assertNull(cleanDate("1957 bc."));
		assertNull(cleanDate("1957 b.c."));
		assertNull(cleanDate("1957 BC"));
		assertNull(cleanDate("1957 BC."));
		assertNull(cleanDate("1957 B.C."));
		assertNull(cleanDate("50 bc"));
		assertNull(cleanDate("50 bc."));
		assertNull(cleanDate("50 b.c."));
		assertNull(cleanDate("50 BC"));
		assertNull(cleanDate("50 BC."));
		assertNull(cleanDate("50 B.C."));
		assertNull(cleanDate("20000 bc"));
		assertNull(cleanDate("20000 bc."));
		assertNull(cleanDate("20000 b.c."));
		assertNull(cleanDate("20000 BC"));
		assertNull(cleanDate("20000 BC."));
		assertNull(cleanDate("20000 B.C."));
	}
}
