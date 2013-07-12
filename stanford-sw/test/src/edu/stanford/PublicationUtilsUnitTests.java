/*
 * Copyright (c) 2012.  The Board of Trustees of the Leland Stanford Junior University. All rights reserved.
 *
 * Redistribution and use of this distribution in source and binary forms, with or without modification, are permitted provided that: The above copyright notice and this permission notice appear in all copies and supporting documentation; The name, identifiers, and trademarks of The Board of Trustees of the Leland Stanford Junior University are not used in advertising or publicity without the express prior written permission of The Board of Trustees of the Leland Stanford Junior University; Recipients acknowledge that this distribution is made available as a research courtesy, "as is", potentially with defects, without any obligation on the part of The Board of Trustees of the Leland Stanford Junior University to provide support, services, or repair;
 *
 * THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package edu.stanford;

import static edu.stanford.PublicationUtils.*;
import static org.junit.Assert.*;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;

import org.marc4j.marc.*;
import org.solrmarc.testUtils.LoggerAppender4Testing;

/**
 * unit tests for methods in PublicationUtils
 * @author Naomi Dushay
 *
 */
public class PublicationUtilsUnitTests
{
	private MarcFactory factory = MarcFactory.newInstance();

	/**
	 * unit tests for getOtherYear
	 */
@Test
	public final void tesGetOtherYear()
	{
		// byte 6 is different than cdeikmpqrstu
		// b
		ControlField cf008 = factory.newControlField("008", "      b1234        ");
	    assertEquals("getOtherYear should return 4 digit year even if 008 byte 6 is 'b'", "1234", getOtherYear(cf008, "b1234", null));
		cf008 = factory.newControlField("008", "      b123u        ");
	    assertEquals("getOtherYear should return 3 digit year even if 008 byte 6 is 'b'", "1230", getOtherYear(cf008, "b123u", null));
		// n
		cf008 = factory.newControlField("008", "      n1234        ");
	    assertEquals("getOtherYear should return 4 digit year even if 008 byte 6 is 'n'", "1234", getOtherYear(cf008, "n1234", null));
		cf008 = factory.newControlField("008", "      n123u        ");
	    assertEquals("getOtherYear should return 3 digit year even if 008 byte 6 is 'n'", "1230", getOtherYear(cf008, "n123u", null));
		// |
		cf008 = factory.newControlField("008", "      |1234        ");
	    assertEquals("getOtherYear should return 4 digit year even if 008 byte 6 is '|'", "1234", getOtherYear(cf008, "|1234", null));
		cf008 = factory.newControlField("008", "      |123u        ");
	    assertEquals("getOtherYear should return 3 digit year even if 008 byte 6 is '|'", "1230", getOtherYear(cf008, "|123u", null));
	    // bad code
		cf008 = factory.newControlField("008", "      -1234        ");
	    assertEquals("getOtherYear should return 4 digit year even if 008 byte 6 is '-'", "1234", getOtherYear(cf008, "-1234", null));
		cf008 = factory.newControlField("008", "      -123u        ");
	    assertEquals("getOtherYear should return 3 digit year even if 008 byte 6 is '-'", "1230", getOtherYear(cf008, "-123u", null));

	    // date1 was assigned to another field
		cf008 = factory.newControlField("008", "      e1234        ");
	    assertNull("getOtherYear should not return 4 digit year if there is a valid date1 for byte e", getOtherYear(cf008, "e1234", null));
		cf008 = factory.newControlField("008", "      e123u        ");
	    assertNull("getOtherYear should not return 4 digit year if there is a valid date1 for byte e", getOtherYear(cf008, "e123u", null));
	    // no date1 but date2 ok
		cf008 = factory.newControlField("008", "      p12uu1234        ");
	    assertNull("getOtherYear should not return 4 digit year if there is a valid date2 for byte 6 dikmpart", getOtherYear(cf008, "pdate2", null));

	    // date1 too imprecise
		cf008 = factory.newControlField("008", "      s19uu        ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "s19uu", null));
		cf008 = factory.newControlField("008", "      s1uuu        ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "s1uuu", null));
		cf008 = factory.newControlField("008", "      b12uu        ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "b12uu", null));
		cf008 = factory.newControlField("008", "      b            ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "b    ", null));
		cf008 = factory.newControlField("008", "      |||||        ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "|||||", null));
		cf008 = factory.newControlField("008", "      nuuuu        ");
	    assertNull("getOtherYear should not return 2 digit year", getOtherYear(cf008, "nuuu", null));
	}

	/**
	 * test that warning messages are logged when getOtherYear finds a usable year in 008 date 1
	 */
@Test
	public void testGetOtherYearWarningMessages()
	{
		Logger logger = Logger.getLogger(PublicationUtils.class.getName());
	    LoggerAppender4Testing appender = new LoggerAppender4Testing();
		logger.addAppender(appender);
		ControlField cf008 = factory.newControlField("008", "      b1234        ");
	    assertEquals("getOtherYear should return 4 digit year even if 008 byte 6 is 'b'", "1234", getOtherYear(cf008, "b1234", logger));
        appender.assertLogContains(Level.WARN, "Unexpectedly found usable date1 in 008 for record: b1234:  ");

		cf008 = factory.newControlField("008", "      nuuuu        ");
	    assertNull("getOtherYear should return null when 008 date1 is unusable even if 008 byte 6 is 'n'", getOtherYear(cf008, "nuuuu", logger));
        appender.assertLogDoesNotContain("Unexpectedly found usable date1 in 008 for record: nuuuu:  ");
	}


	/**
	 * we are expecting no results from 008 for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public final void testPubYearSliderNoDatesFrom008()
	{
		ControlField cf008 = factory.newControlField("008", "      s19uu      ");
		assertEquals("getPubDateSliderVals should have no results when 008 date1 is 19uu", 0, getPubDateSliderVals(cf008, null).size());

		cf008 = factory.newControlField("008", "      s1uuu     ");
		assertEquals("getPubDateSliderVals should have no results when 008 date1 is 1uuu", 0, getPubDateSliderVals(cf008, null).size());

		cf008 = factory.newControlField("008", "      b        ");
		assertEquals("getPubDateSliderVals should have no results when 008 date1 is blanks", 0, getPubDateSliderVals(cf008, null).size());

		cf008 = factory.newControlField("008", "      nuuuuuuuu");
		assertEquals("getPubDateSliderVals should have no results when 008 date1 is uuuu", 0, getPubDateSliderVals(cf008, null).size());

		cf008 = factory.newControlField("008", "      |||||||||");
		assertEquals("getPubDateSliderVals should have no results when 008 date1 ||||", 0, getPubDateSliderVals(cf008, null).size());

		cf008 = factory.newControlField("008", "      $unexpected");
		assertEquals("getPubDateSliderVals should have no results when 008 has no viable date and there is no 260", 0, getPubDateSliderVals(cf008, null).size());
	}

	/**
	 * we are expecting a single value from 008 for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public final void testPubYearSliderSingleDateFrom008()
	{
		Set<String> values = getPubDateSliderVals(factory.newControlField("008", "      e19821212"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'e'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'e' and there is a usable date", values.contains("1982"));
		values = getPubDateSliderVals(factory.newControlField("008", "      s197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'e'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'e' and there is a usable date", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      s1970    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 's'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 's' and there is a usable date", values.contains("1970"));
		values = getPubDateSliderVals(factory.newControlField("008", "      s19701980"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 's'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 's' and there is a usable date", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      u1970    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'u'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'u' and there is a usable date", values.contains("1970"));
		values = getPubDateSliderVals(factory.newControlField("008", "      u197019uu"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'u'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'u' and there is a usable date", values.contains("1970"));

		// we have a usable date1 value even tho byte 6 says we shouldn't
		values = getPubDateSliderVals(factory.newControlField("008", "      b1974    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'b' and there is a usable date", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'b' and there is a usable date", values.contains("1974"));
		values = getPubDateSliderVals(factory.newControlField("008", "      n197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'n' and there is a usable date", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'n' and there is a usable date", values.contains("1970"));
		values = getPubDateSliderVals(factory.newControlField("008", "      |19732008"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is '|' and there is a usable date", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is '|' and there is a usable date", values.contains("1973"));
		values = getPubDateSliderVals(factory.newControlField("008", "      $197u2008"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is '$' and there is a usable date", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is '$' and there is a usable date", values.contains("1970"));

		// these are continuing resources with open end dates;  currently we only index date 1
		values = getPubDateSliderVals(factory.newControlField("008", "      c19709999"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'c'", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'c'", values.contains("1970"));
		values = getPubDateSliderVals(factory.newControlField("008", "      m19709999"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'm' and date2 is 9999", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'm' and date2 is 9999", values.contains("1970"));
	}


	/**
	 * we are expecting two values from 008 for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public final void testPubYearSliderTwoDatesFrom008()
	{
		Set<String> values = getPubDateSliderVals(factory.newControlField("008", "      p19821986"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 'p'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'p'", values.containsAll(Arrays.asList("1982", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      p1974198u"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 'p'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'p'", values.containsAll(Arrays.asList("1974", "1989")));
		values = getPubDateSliderVals(factory.newControlField("008", "      p197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'p' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'p' and second date is missing", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      r19821961"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 'r'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'r'", values.containsAll(Arrays.asList("1982", "1961")));
		values = getPubDateSliderVals(factory.newControlField("008", "      r1974196u"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 'r'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'r'", values.containsAll(Arrays.asList("1974", "1969")));
		values = getPubDateSliderVals(factory.newControlField("008", "      r197uuuuu"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'r' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'r' and second date is missing", values.contains("1970"));
		values = getPubDateSliderVals(factory.newControlField("008", "      ruuuu1964"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'r' and first date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 2 when byte 6 is 'r' and first date is missing", values.contains("1964"));

		values = getPubDateSliderVals(factory.newControlField("008", "      t19821986"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 't'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 't'", values.containsAll(Arrays.asList("1982", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      t1974198u"), null);
		assertEquals("getPubDateSliderVals should have two results when 008 byte 6 is 't'", 2, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 't'", values.containsAll(Arrays.asList("1974", "1989")));
		values = getPubDateSliderVals(factory.newControlField("008", "      t197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 't' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 't' and second date is missing", values.contains("1970"));
	}


	/**
	 * we are expecting a closed range of values from 008 for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public final void testPubYearSliderClosedRangeFrom008()
	{
		Set<String> values = getPubDateSliderVals(factory.newControlField("008", "      d19821986"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'd'", 5, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'd'", values.containsAll(Arrays.asList("1982", "1983", "1984", "1985", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      d1974197u"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'd'", 6, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'd'", values.containsAll(Arrays.asList("1974", "1975", "1976", "1977", "1978", "1979")));
		values = getPubDateSliderVals(factory.newControlField("008", "      d197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'd' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'd' and second date is missing", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      i19821986"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'i'", 5, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'i'", values.containsAll(Arrays.asList("1982", "1983", "1984", "1985", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      i1974197u"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'i'", 6, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'i'", values.containsAll(Arrays.asList("1974", "1975", "1976", "1977", "1978", "1979")));
		values = getPubDateSliderVals(factory.newControlField("008", "      i197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'i' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'i' and second date is missing", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      k19821986"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'k'", 5, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'k'", values.containsAll(Arrays.asList("1982", "1983", "1984", "1985", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      k1974197u"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'k'", 6, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'k'", values.containsAll(Arrays.asList("1974", "1975", "1976", "1977", "1978", "1979")));
		values = getPubDateSliderVals(factory.newControlField("008", "      k197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'k' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'k' and second date is missing", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      q19821986"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'q'", 5, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'q'", values.containsAll(Arrays.asList("1982", "1983", "1984", "1985", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      q1974197u"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'q'", 6, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'q'", values.containsAll(Arrays.asList("1974", "1975", "1976", "1977", "1978", "1979")));
		values = getPubDateSliderVals(factory.newControlField("008", "      q197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'q' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'q' and second date is missing", values.contains("1970"));

		values = getPubDateSliderVals(factory.newControlField("008", "      m19821986"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'm'", 5, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'm'", values.containsAll(Arrays.asList("1982", "1983", "1984", "1985", "1986")));
		values = getPubDateSliderVals(factory.newControlField("008", "      m1974197u"), null);
		assertEquals("getPubDateSliderVals should have a range when 008 byte 6 is 'm'", 6, values.size());
		assertTrue("getPubDateSliderVals incorrect when 008 byte 6 is 'm'", values.containsAll(Arrays.asList("1974", "1975", "1976", "1977", "1978", "1979")));
		values = getPubDateSliderVals(factory.newControlField("008", "      muuuu1986"), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'm' and first date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 2 when byte 6 is 'm' and first date is missing", values.contains("1986"));
		values = getPubDateSliderVals(factory.newControlField("008", "      m197u    "), null);
		assertEquals("getPubDateSliderVals should have one result when 008 byte 6 is 'm' and second date is missing", 1, values.size());
		assertTrue("getPubDateSliderVals should return 008 date 1 when byte 6 is 'm' and second date is missing", values.contains("1970"));
	}

	/**
	 * we are expecting no value from 260c for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public void testPubYearSliderNoDatesFrom260or008()
	{
		assertNoPubYearSliderValFrom260c("[19--]");
		assertNoPubYearSliderValFrom260c("[19--?]");
		assertNoPubYearSliderValFrom260c("19  -");
		assertNoPubYearSliderValFrom260c("19--]");
		assertNoPubYearSliderValFrom260c("19--?]");
		assertNoPubYearSliderValFrom260c("[19--]-");
		assertNoPubYearSliderValFrom260c("19??-");
		// angle brackets
		assertNoPubYearSliderValFrom260c("<1973>");
		assertNoPubYearSliderValFrom260c("<1972->");
		assertNoPubYearSliderValFrom260c("<1972>-");
		assertNoPubYearSliderValFrom260c("<-1964>");
		assertNoPubYearSliderValFrom260c("<-1964>");
		assertNoPubYearSliderValFrom260c("-<1964>");
		assertNoPubYearSliderValFrom260c("<1964-1969>");
		assertNoPubYearSliderValFrom260c("<1964>-<1969>");
		// "no date" variants
		assertNoPubYearSliderValFrom260c("[n.d.]");
		assertNoPubYearSliderValFrom260c("n.d.]");
		assertNoPubYearSliderValFrom260c("n.d.].");
		assertNoPubYearSliderValFrom260c("n.d.");
		assertNoPubYearSliderValFrom260c("[n.d.].");
		assertNoPubYearSliderValFrom260c("[nd].");
		assertNoPubYearSliderValFrom260c("nd");
		assertNoPubYearSliderValFrom260c("[date not identified]");
		assertNoPubYearSliderValFrom260c("[n. d.]");
		assertNoPubYearSliderValFrom260c("n. d.");
		assertNoPubYearSliderValFrom260c("s. d.");
		assertNoPubYearSliderValFrom260c("[s.d.]");
		assertNoPubYearSliderValFrom260c("s.d.]");
		assertNoPubYearSliderValFrom260c("s.d.");
	}

	/**
	 * we are expecting a single value from 260c for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public void testPubYearSlider260PlainYear()
	{
		assertSinglePubYearSliderValFrom260c("1973", "1973");
		assertSinglePubYearSliderValFrom260c("1973.", "1973");
		assertSinglePubYearSliderValFrom260c("1973]", "1973");
		assertSinglePubYearSliderValFrom260c("1973?", "1973");
//		assertSinglePubYearSliderValFrom260c("1973?]", "1973");
		assertSinglePubYearSliderValFrom260c("[1973]", "1973");
		assertSinglePubYearSliderValFrom260c("[1973?]", "1973");
		assertSinglePubYearSliderValFrom260c("[1973?].", "1973");
		assertSinglePubYearSliderValFrom260c("[196-?].", "1960");
//		assertSinglePubYearSliderValFrom260c("March 1987.", "1987");

//
//	    // possible other types of values to care about
//	    // Printed in the yeare, 1641.
//	    // [1967, reprinted 1968]
//	    // 1960 [reprinted 1974]
	}


	/**
	 * we are expecting a single value from 260c for getPubDateSliderVals(cf008, df260List)
	 */
@Test
	public void testPubYearSlider260CopyrightYear()
	{
		assertSinglePubYearSliderValFrom260c("c1975.", "1975");
//		assertSinglePubYearSliderValFrom260c("[c1973]", "1973");
		assertSinglePubYearSliderValFrom260c("c1975]", "1975");
	}


	/**
	 * we are expecting a single value from 260c for getPubDateSliderVals(cf008, df260List)
	 */
//@Test
	public void testPubYearSlider260MultSingleYears()
	{
		assertMultiplePubYearSliderValsFromSingle260c("[1974, c1973]", new String[]{"1974", "1973"});
		assertMultiplePubYearSliderValsFromSingle260c("1975, c1974.", new String[]{"1975", "1974"});
		assertMultiplePubYearSliderValsFromSingle260c("1974 [c1973]", new String[]{"1974", "1973"});
		assertMultiplePubYearSliderValsFromSingle260c("[1975] c1974.", new String[]{"1975", "1974"});

		assertMultiplePubYearSliderValsFromSingle260c("1965[c1966]", new String[]{"1965", "1966"});
		assertMultiplePubYearSliderValsFromSingle260c("1967, c1966]", new String[]{"1967", "1966"});
		assertMultiplePubYearSliderValsFromSingle260c("[1974?] c1973..", new String[]{"1974", "1973"});

		// after publication date
		// [dddd, cdddd]
		// dddd [cdddd] (may have comma between the dates)
		// dddd, cdddd
		// [dddd] cdddd (may have comma between the dates)
	}

// 260 ranges
// [1974-
// -1991.

// with corrected date
// dddd [i.e. dddd]  -> spacing and punctuation vary
// [dddd i.e. dddd] => spacing and punctuation vary
// 1973 [i.e. 1974]
// 1971[i.e.1972]
// 1973 [i.e. 1974]
// 1973 [i.e.1974]
// 1972[i.e.1973]
// 1971[i.e.1972]
// 1967 [i. e. 1968]



// open ranges


	/**
	 * assure pub_date field ignores the unknown-ish phrases
	 */
@Test
	public void testGetValidPubDateIgnores264Unknowns()
	{
		List<DataField> f264list = new ArrayList<DataField>();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of publication not identified] :"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of Production not identified]"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "Date of manufacture Not Identified"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "[Date of distribution not identified]"));
	    f264list.add(df);
	    assertNull("getValidPubDateStr should return null when it only finds 264 sub c with ignored values ", getValidPubDateStr(null, null, f264list));
	}


	/**
	 * assure pub dates later than limit are ignored
	 */
@Test
	public void testGetValidPubDateIgnoresTooLate()
	{
		List<DataField> f264list = new ArrayList<DataField>();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "9999"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "6666"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "22nd century"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "8610s"));
	    f264list.add(df);

	    int upperLimit = 2012;
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', Integer.toString(upperLimit + 1)));
	    f264list.add(df);

	    assertNull("getValidPubDateStr should return null when it only finds 264c with vals that are greater than the upper limit", getValidPubDateStr(null, upperLimit, 500, null, f264list));
	}


	/**
	 * assure pub dates of < 500 are ignored
	 */
@Test
	public void testGetValidPubDateIgnoresTooEarly()
	{
		List<DataField> f264list = new ArrayList<DataField>();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0000"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0036"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0197"));
	    f264list.add(df);
	    df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "0204"));
	    f264list.add(df);
	    int lowerLimit = 500;
	    assertNull("getValidPubDateStr should return null when it only finds 264c with vals that are < the lower limit", getValidPubDateStr(null, 2022, lowerLimit, null, f264list));
	}


	/**
	 * test that auto-correction of pub date in 008 by checking value in 260c
	 */
@Test
	public void testGetPubDateAutoCorrectsWith260c()
	{
	    assertEquals("getValidPubDateStr should return its fourth argument when the first argument isn't a valid date", "2009", getValidPubDateStr("0059", "2009.", new ArrayList()));
	    assertEquals("getValidPubDateStr should return its fourth argument when the first argument isn't a valid date", "1970", getValidPubDateStr("0197", "[197?]", new ArrayList()));
	    assertEquals("getValidPubDateStr should return its fourth argument when the first argument isn't a valid date", "2004", getValidPubDateStr("0204", "[2004]", new ArrayList()));
	    assertNull("getValidPubDateStr should return null when it only finds 260c with invalid date", getValidPubDateStr(null, "invalid", new ArrayList()));
	}


@Test
	public void test008DDDDForPubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      b1960    ");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1960");
	}

@Test
	public void test008DDDUForPubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      b196u    ");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1960");

		f008 = factory.newControlField("008", "041202s20uu    mdunnn  s      f    eng d");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "2000");

		f008 = factory.newControlField("008", "780930c00uu9999nyu           000 0 eng d");
		assertEquals("getPubDateSliderVals should have no results if bad 008 and no other options", 0, getPubDateSliderVals(f008, null, null).size());

		f008 = factory.newControlField("008", "780930c0197    nyu           000 0 eng d");
		assertEquals("getPubDateSliderVals should have no results if bad 008 and no other options", 0, getPubDateSliderVals(f008, null, null).size());

		f008 = factory.newControlField("008", "780930c0059    nyu           000 0 eng d");
		assertEquals("getPubDateSliderVals should have no results if bad 008 and no other options", 0, getPubDateSliderVals(f008, null, null).size());
	}
@Test
	public void test008DDUUForPubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      b19uu    ");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1900");
	}

@Test
	public void test008DUUUForPubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      b1uuu    ");
		Set<String> result = getPubDateSliderVals(f008, null, null);
		assertEquals("getPubDateSliderVals should have no results from 008 bytes 7-10 when duuu and no other options", 0, result.size());

		f008 = factory.newControlField("008", "041202s2uuu    mdunnn  s      f    eng d");
		assertEquals("getPubDateSliderVals should have no results if uuu date and no other options", 0, getPubDateSliderVals(f008, null, null).size());
	}

@Test
	public void test008FirstDateBadForPubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      bnope    ");
		Set<String> result = getPubDateSliderVals(f008, null, null);
		assertEquals("getPubDateSliderVals should have no results if bad 008 and no other options", 0, result.size());

		result = getPubDateSliderVals(f008, "1960", new ArrayList<DataField>());
		assertEquals("getPubDateSliderVals should get single result from second arg if 008 has bad value", "1960", result.toArray()[0]);

		List<DataField> f264list = new ArrayList<DataField>();
	    DataField df = factory.newDataField("264", ' ', ' ');
	    df.addSubfield(factory.newSubfield('c', "1970"));
	    f264list.add(df);
		result = getPubDateSliderVals(f008, null, f264list);
		assertEquals("getPubDateSliderVals should get single result from third arg if 008 has bad value and no second arg", "1970", result.toArray()[0]);
	}

@Test
	public void testIndexOneDate4PubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      b19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      c19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      e19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      n19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      s19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      u19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");

		f008 = factory.newControlField("008", "      z19671969");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");
	}

@Test
	public void	testIndexMultipleDates4PubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      p19671969");
		String[] expected = new String[] {"1967", "1969" };
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
		f008 = factory.newControlField("008", "      r19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
		f008 = factory.newControlField("008", "      t19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);

		expected = new String[] {"1967", "1969", "1968" };
		f008 = factory.newControlField("008", "      d19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
		f008 = factory.newControlField("008", "      i19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
		f008 = factory.newControlField("008", "      k19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
		f008 = factory.newControlField("008", "      q19671969");
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
	}

@Test
	public void test008Mdate2is9999PubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      m19679999");
		assertSingleResult(getPubDateSliderVals(f008, null, null), "1967");
	}

@Test
	public void test008Mdate2isNot9999PubDateSlider()
	{
		ControlField f008 = factory.newControlField("008", "      m19671969");
		String[] expected = new String[] {"1967", "1969", "1968" };
		assertMultipleResults(getPubDateSliderVals(f008, null, null), expected);
	}

// -----  private ------

	private void assertSingleResult(Set<String> resultSet, String expectedResult)
	{
		assertEquals("getPubDateSliderVals: expected a single result", 1, resultSet.size());
		assertEquals("getPubDateSliderVals: should get the date " + expectedResult, expectedResult, resultSet.toArray()[0]);
	}

	private void assertMultipleResults(Set<String> resultSet, String[] expectedResults)
	{
		int expNum = expectedResults.length;
		assertEquals("getPubDateSliderVals: expected " + String.valueOf(expNum) + " results", expNum, resultSet.size());
		assertArrayEquals("getPubDateSliderVals should get the dates from 008", expectedResults, resultSet.toArray());
	}

	private void assertSinglePubYearSliderValFrom260c(String df260cVal, String expSliderVal)
	{
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		List<String> df260subcList = new ArrayList<String>();
		df260subcList.add(df260cVal);
		assertSingleResult(getPubDateSliderVals(cf008, df260subcList), expSliderVal);
	}

	private void assertMultiplePubYearSliderValsFromSingle260c(String df260cVal, String[] expectedResults)
	{
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		List<String> df260subcList = new ArrayList<String>();
		df260subcList.add(df260cVal);
		assertMultipleResults(getPubDateSliderVals(cf008, df260subcList), expectedResults);
	}

	private void assertNoPubYearSliderValFrom260c(String df260cVal)
	{
		ControlField cf008 = factory.newControlField("008", "      |||||||||");
		List<String> df260subcList = new ArrayList<String>();
		df260subcList.add(df260cVal);
		assertEquals("getPubDateSliderVals should have no results when 008 has no viable date and neither does any 260c", 0, getPubDateSliderVals(cf008, df260subcList).size());
	}

}
