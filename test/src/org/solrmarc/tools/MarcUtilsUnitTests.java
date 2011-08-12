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


	/**
	 * unit test for org.solrmarc.tools.MarcUtils.isMHLDRecord
	 */
@Test
	public void testIsMhldRecord()
	{
		String sampleLdrStr = "02429nas a2200481 a 4500";
		Record rec = new NoSortRecordImpl();
		Leader leader = new LeaderImpl(sampleLdrStr);
		rec.setLeader(leader);
		assertFalse("Record type a should be not be recognized as MHLD", MarcUtils.isMHLDRecord(rec));

		leader.setTypeOfRecord('u');
		rec.setLeader(leader);
		assertTrue("Record type u should be recognized as MHLD", MarcUtils.isMHLDRecord(rec));
		leader.setTypeOfRecord('v');
		rec.setLeader(leader);
		assertTrue("Record type v should be recognized as MHLD", MarcUtils.isMHLDRecord(rec));
		leader.setTypeOfRecord('x');
		rec.setLeader(leader);
		assertTrue("Record type x should be recognized as MHLD", MarcUtils.isMHLDRecord(rec));
		leader.setTypeOfRecord('y');
		rec.setLeader(leader);
		assertTrue("Record type y should be recognized as MHLD", MarcUtils.isMHLDRecord(rec));
	}


	/**
	 * unit test for org.solrmarc.tools.MarcUtils.getControlFieldData
	 */
@Test
	public void testGetControlFieldData()
	{
		Record rec = new NoSortRecordImpl();
		ControlField cntlFld001 = new ControlFieldImpl("001", "control001");
		rec.addVariableField(cntlFld001);
		assertEquals("Control Field 001 did not have correct data ", "control001", MarcUtils.getControlFieldData(rec, "001"));

		// add another control field
		ControlField cntlFld009 = new ControlFieldImpl("009", "control009");
		rec.addVariableField(cntlFld009);
		assertEquals("Control Field 001 did not have correct data ", "control001", MarcUtils.getControlFieldData(rec, "001"));
		assertEquals("Control Field 009 did not have correct data ", "control009", MarcUtils.getControlFieldData(rec, "009"));

		// add data field as a control field 
		ControlField cntlFld666 = new ControlFieldImpl("666", "control666");
		rec.addVariableField(cntlFld666);
		assertEquals("Control Field 666 did not have correct data ", "control666", MarcUtils.getControlFieldData(rec, "666"));

		// add data field as data field and retrieve as control field
		DataField dataFld777 = new DataFieldImpl("777", ' ', ' ');
		Subfield suba = new SubfieldImpl('a', "777suba");
		Subfield subb = new SubfieldImpl('b', "777subb");
		dataFld777.addSubfield(suba);
		dataFld777.addSubfield(subb);
		rec.addVariableField(dataFld777);
		
		// method comments:  
		// If the field is a DataField, return the contents of the specified subfield, or, if unspecified, of subfield 'a'
		assertEquals("Data Field 777 sub a did not have correct data ", "777suba", MarcUtils.getControlFieldData(rec, "777"));
		assertEquals("Data Field 777 sub a did not have correct data ", "777suba", MarcUtils.getControlFieldData(rec, "777a"));
		assertEquals("Data Field 777 sub b did not have correct data ", "777subb", MarcUtils.getControlFieldData(rec, "777b"));
	}


/*
  	    List<VariableField> recToCopyFromAllFields = recordToCopyFrom.getVariableFields();
	    for (VariableField vf : recToCopyFromAllFields)
	    {
	        if (vf.getTag().matches(fieldsToCopy))
	            resultRecord.addVariableField(vf);
	    }
	    return(resultRecord);

 */
	
	/*
	 * unit test for org.solrmarc.tools.MarcUtils.combineRecords(record, record, fieldspec)
	 */
@Test
	public void testCombineRecords3Args()
	{
		Record bibRec1 = createRecordW199a_111a();
		Record bibRec2 = createRecordW177a_122b_122a();

		// test:  no matching fields in rec2
		Record resultRec = MarcUtils.combineRecords(bibRec1, bibRec2, "222");
		// bibRec1 should still have 2 fields
		assertEquals("Wrong number of fields in record after merge ", 2, bibRec1.getVariableFields().size());
		// resultRec should have 199 and 111 fields, in that order
		List<VariableField> vfList = resultRec.getVariableFields();
		assertEquals("Wrong number of fields in record after merge ", 2, vfList.size());
		// are first rec fields in order?
		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
		
		// test:  only merge one field
		resultRec = MarcUtils.combineRecords(bibRec1, bibRec2, "122");
		vfList = resultRec.getVariableFields();
		assertEquals("Wrong number of fields in record after merge ", 4, vfList.size());
		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(2).getTag());
		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(3).getTag());
		// are first rec fields in order?
		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
		// are 122 fields in right order?
		List<Subfield> sfList = ((DataField) vfList.get(2)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "122subb", sfList.get(0).getData());
		sfList = ((DataField) vfList.get(3)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "122suba", sfList.get(0).getData());
		
		// test:  merge both fields
		bibRec1 = createRecordW199a_111a();
		resultRec = MarcUtils.combineRecords(bibRec1, bibRec2, "122|177");
		vfList = resultRec.getVariableFields();
		assertEquals("Wrong number of fields in record after merge ", 5, vfList.size());
		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
		assertEquals("Incorrect field or field order after merge ", "177", vfList.get(2).getTag());
		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(3).getTag());
		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(4).getTag());
		// are 122 fields in right order?
		sfList = ((DataField) vfList.get(3)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "122subb", sfList.get(0).getData());
		sfList = ((DataField) vfList.get(4)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "122suba", sfList.get(0).getData());
		
		// next rec is an mhld
		bibRec1 = createRecordW199a_111a();
		Record mhldRec = createMhldRecordW133a_111a();
		resultRec = MarcUtils.combineRecords(bibRec1, mhldRec, "133|111");
		vfList = resultRec.getVariableFields();
		assertEquals("Wrong number of fields in record after merge ", 4, vfList.size());
		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
		assertEquals("Incorrect field or field order after merge ", "133", vfList.get(2).getTag());
		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(3).getTag());
		// are 111 fields in right order?
		sfList = ((DataField) vfList.get(1)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "111suba", sfList.get(0).getData());
		sfList = ((DataField) vfList.get(3)).getSubfields();
		assertEquals("Incorrect field order for mult occurrences ", "mhld111suba", sfList.get(0).getData());

		
				
		
		
		// next rec's fields are scattered throughout
		
		// maintains field order of receiving record
		
		// maintains field order of other record
		
		// field spec  has colons
		// field spec has subfields
		// field spec has two char fields
	}

	
	public void testCombineRecords4Args()
	{
		// last field in receiving record
		
		// multiple (last) fields in receiving record
		
		//  middle field in receiving record
		
		//  mult conseq occurrences of middle field in receiving record
		
		// mult non-conseq occurrences of field in receiving record
		
	}
	
	
	/**
	 * @return a Record object with:
	 *   199 field with subfield a
	 *   111 field with subfield a
	 */
	private Record createRecordW199a_111a()
	{
		Record result = new NoSortRecordImpl();
		DataField dataFld = new DataFieldImpl("199", ' ', ' ');
		Subfield suba = new SubfieldImpl('a', "199suba");
		dataFld.addSubfield(suba);
		result.addVariableField(dataFld);
		dataFld = new DataFieldImpl("111", ' ', ' ');
		suba = new SubfieldImpl('a', "111suba");
		dataFld.addSubfield(suba);
		result.addVariableField(dataFld);
		return result;
	}
		
	
	/**
	 * @return a Record object with:
	 *   177 field with subfield a
	 *   122 field with subfield b
	 *   122 field with subfield a
	 */
	private Record createRecordW177a_122b_122a()
	{
		Record result = new NoSortRecordImpl();
		DataField df = new DataFieldImpl("177", ' ', ' ');
		Subfield suba = new SubfieldImpl('a', "177suba");
		df.addSubfield(suba);
		result.addVariableField(df);
		DataField df122b = new DataFieldImpl("122", ' ', ' ');
		suba = new SubfieldImpl('b', "122subb");
		df122b.addSubfield(suba);
		result.addVariableField(df122b);
		DataField df122a = new DataFieldImpl("122", ' ', ' ');
		suba = new SubfieldImpl('a', "122suba");
		df122a.addSubfield(suba);
		result.addVariableField(df122a);
		return result;
	}
		
	
	/**
	 * @return a Record object with:
	 *   177 field with subfield a
	 *   122 field with subfield b
	 *   122 field with subfield a
	 */
	private Record createMhldRecordW133a_111a()
	{
		Record mhldRec = new NoSortRecordImpl();
		String sampleLdrStr = "02429nas a2200481 a 4500";
		Leader leader = new LeaderImpl(sampleLdrStr);
		leader.setTypeOfRecord('u');
		mhldRec.setLeader(leader);
		DataField mhldFld133 = new DataFieldImpl("133", ' ', ' ');
		Subfield suba = new SubfieldImpl('a', "133suba");
		mhldFld133.addSubfield(suba);
		mhldRec.addVariableField(mhldFld133);
		DataField mhldFld111 = new DataFieldImpl("111", ' ', ' ');
		suba = new SubfieldImpl('a', "mhld111suba");
		mhldFld111.addSubfield(suba);
		mhldRec.addVariableField(mhldFld111);
		return mhldRec;
	}
		
	

	


}
