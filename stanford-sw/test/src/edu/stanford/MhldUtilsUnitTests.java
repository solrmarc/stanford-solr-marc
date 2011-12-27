package edu.stanford;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.marc4j.marc.*;
import org.marc4j.marc.impl.*;

/**
 * unit tests for edu.stanford.MhldDisplayUtil methods
 * @author Naomi Dushay
 */
public class MhldUtilsUnitTests
{

	/*
	 * unit test for org.solrmarc.tools.MarcUtils.combineRecords(record, record, fieldspec)
	 */
//@Test
//	public void testSkip852()
//	{
//		DataField df = new DataFieldImpl("852", ' ', ' ');
//		Subfield subz = new SubfieldImpl('z', "All holdings transferred");
//		df.addSubfield(subz);
//		assertTrue("Expected skip852 to return true", MhldDisplayUtil.skip852(df));
//		
//		df = new DataFieldImpl("852", ' ', ' ');
//		subz = new SubfieldImpl('z', "random comment");
//		df.addSubfield(subz);
//		assertTrue("Expected skip852 to return false", !MhldDisplayUtil.skip852(df));
//	}

    /*
     * unit test for org.solrmarc.tools.MarcUtils.combineRecords(record, record, fieldspec)
     */
//@Test
//    public void testSomething()
//    {
//    	Record skipIt = new NoSortRecordImpl();
//    	DataField dataFld = new DataFieldImpl("852", ' ', ' ');
//    	Subfield subz = new SubfieldImpl('z', "All holdings transferred");
//    	dataFld.addSubfield(subz);
//    	skipIt.addVariableField(dataFld);
//    
//    	Record keepIt = new NoSortRecordImpl();
//    	dataFld = new DataFieldImpl("852", ' ', ' ');
//    	subz = new SubfieldImpl('z', "random comment");
//    	dataFld.addSubfield(subz);
//    	skipIt.addVariableField(dataFld);
//    	
//    	MhldDisplayUtil.skip852(df852);
//    	
//    }
//    
//
//
//	private void random()
//	{
//		Record bibRec1 = createRecordW199a_111a();
//		Record bibRec2 = createRecordW177a_122b_122a();
//
//		// test:  no matching fields in rec2
//		Record resultRec = MarcUtils.combineRecords(bibRec1, bibRec2, "222");
//		// bibRec1 should still have 2 fields
//		assertEquals("Wrong number of fields in record after merge ", 2, bibRec1.getVariableFields().size());
//		// resultRec should have 199 and 111 fields, in that order
//		List<VariableField> vfList = resultRec.getVariableFields();
//		assertEquals("Wrong number of fields in record after merge ", 2, vfList.size());
//		// are first rec fields in order?
//		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
//		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
//		
//		// test:  only merge one field
//		resultRec = MarcUtils.combineRecords(bibRec1, bibRec2, "122");
//		vfList = resultRec.getVariableFields();
//		assertEquals("Wrong number of fields in record after merge ", 4, vfList.size());
//		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
//		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
//		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(2).getTag());
//		assertEquals("Incorrect field or field order after merge ", "122", vfList.get(3).getTag());
//		// are first rec fields in order?
//		assertEquals("Incorrect field or field order after merge ", "199", vfList.get(0).getTag());
//		assertEquals("Incorrect field or field order after merge ", "111", vfList.get(1).getTag());
//		// are 122 fields in right order?
//		List<Subfield> sfList = ((DataField) vfList.get(2)).getSubfields();
//		assertEquals("Incorrect field order for mult occurrences ", "122subb", sfList.get(0).getData());
//		sfList = ((DataField) vfList.get(3)).getSubfields();
//		assertEquals("Incorrect field order for mult occurrences ", "122suba", sfList.get(0).getData());
//	}
//	
//	/**
//	 * @return a Record object with:
//	 *   199 field with subfield a
//	 *   111 field with subfield a
//	 */
//	private Record createRecordW199a_111a()
//	{
//		Record result = new NoSortRecordImpl();
//		DataField dataFld = new DataFieldImpl("199", ' ', ' ');
//		Subfield suba = new SubfieldImpl('a', "199suba");
//		dataFld.addSubfield(suba);
//		result.addVariableField(dataFld);
//		dataFld = new DataFieldImpl("111", ' ', ' ');
//		suba = new SubfieldImpl('a', "111suba");
//		dataFld.addSubfield(suba);
//		result.addVariableField(dataFld);
//		return result;
//	}
		
	
}
