package org.solrmarc.tools;

import static org.solrmarc.tools.MarcUtils.*;

import org.marc4j.marc.*;
import org.marc4j.marc.impl.*;

import static org.junit.Assert.*;
import org.junit.*;

import java.util.*;

/**
 * Unit tests for methods in org.solrmarc.tools.MarcUtils
 * @author naomi
 *
 */
public class MarcUtilsUnitTests {

	/**
	 * unit test for org.solrmarc.tools.MarcUtils.getDataFieldsInRange
	 */
@Test
	public void testGetDataFieldsInRange()
	{
		Record rec = new NoSortRecordImpl();
		
		Subfield sf = new SubfieldImpl('a', "ignored");

		String[] fldTagList = new String[]{"333", "321", "343", "002", "343", "345", "344", "010"};
		for (int i = 0; i < fldTagList.length; i++) {
			String tag = fldTagList[i];
			DataField df = new DataFieldImpl(tag, ' ', ' ');
			df.addSubfield(sf);
			rec.addVariableField(df);
		}
	
		List<DataField> dfResult = getDataFieldsInRange(rec, "320", "344");
		List<String> resultTags = new ArrayList<String>(5);
		for (DataField df : dfResult) {
			resultTags.add(df.getTag());
		}
		
		// bounds
		assertTrue("lower bound field (321) should be included", resultTags.contains("321"));
		assertTrue("upper bound field (344) should be included", resultTags.contains("344"));
		assertTrue("field out of bounds (345) should not be included", !resultTags.contains("345"));
		// with control field numbers
		assertTrue("field out of bounds (002) should not be included", !resultTags.contains("002"));
		
		// fields in order encountered
		assertTrue("fields should be in order encountered - 333 occurs before 321", resultTags.indexOf("333") < resultTags.indexOf("321"));
		
		// repeated fields should be included:  333, 321, 343, 343, 344
		assertTrue("all instances of repeated fields should be included", resultTags.size() == 5);

		// more on control fields
		dfResult = getDataFieldsInRange(rec, "001", "010");
		resultTags.clear();
		for (DataField df : dfResult) {
			resultTags.add(df.getTag());
		}
		assertTrue("010 is not a control field, so it should be included", resultTags.contains("010"));
		assertTrue("no control field tags should be included", resultTags.size() == 1);
		
	}

	/**
	 * unit test for org.solrmarc.tools.MarcUtils.getSubfieldStrings
	 */
@Test
	public void testGetSubfieldStrings()
	{
		Record rec = new NoSortRecordImpl();
		
		String[] fldTagList = new String[]{"650", "651", "653", "650"};
		for (int i = 0; i < fldTagList.length; i++) {
			String tag = fldTagList[i];
			DataField df = new DataFieldImpl(tag, ' ', ' ');
			df.addSubfield(new SubfieldImpl('a', "first " + tag + "a"));
			df.addSubfield(new SubfieldImpl('a', "second " + tag + "a"));
			df.addSubfield(new SubfieldImpl('b', "contents of " + tag + "b"));
			df.addSubfield(new SubfieldImpl('a', "third " + tag + "a"));
			df.addSubfield(new SubfieldImpl('c', tag + "c"));
			rec.addVariableField(df);
		}
	
		List<DataField> dataFields = getDataFieldsInRange(rec, "650", "655");
		List<String> subaList = getSubfieldStrings(dataFields, "a");
		// each instance of single subfield should be on list for mult subfields
		assertTrue("There should be 12 subfield a instances", subaList.size() == 12);
		assertTrue("There should be third subfield a instances for 650", subaList.contains("third 650a"));
		assertTrue("There should be third subfield a instances for 653", subaList.contains("third 653a"));
		assertTrue("No subfield b's should be in list of a's", !subaList.contains("contents of 650b"));
		assertTrue("No subfield c's should be in list of a's", !subaList.contains("650c"));
		
		// duplicate subfield contents should be included
		int count = 0;
		for (String suba : subaList) {
			if (suba.equals("first 650a"))
				count++;
		}
		assertTrue("There should 2 subfield a containing 'first 650a'", count == 2);
		count = 0;
		for (String suba : subaList) {
			if (suba.equals("second 650a"))
				count++;
		}
		assertTrue("There should 2 subfield a containing 'second 650a'", count == 2);
		
		// each instance of subfield should be on list for mult subfields
		List<String> subacList = getSubfieldStrings(dataFields, "ac");
		assertTrue("Subfield c should be in ac list", subacList.contains("651c"));
		assertTrue("There should be third subfield a instances for 653", subacList.contains("third 653a"));
		assertTrue("There should not be any subfield b in ac list", !subacList.contains("contents of 650b"));
	}

}
