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

import org.junit.*;

import org.marc4j.marc.*;

/**
 * unit tests for methods in PublicationUtils
 * @author Naomi Dushay
 *
 */
public class PublicationUtilsUnitTests
{
	private MarcFactory factory = MarcFactory.newInstance();

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


}
