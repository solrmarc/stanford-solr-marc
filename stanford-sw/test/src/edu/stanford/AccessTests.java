package edu.stanford;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.*;
import org.marc4j.marc.*;
import org.solrmarc.testUtils.IndexTest;
import org.xml.sax.SAXException;

import edu.stanford.enumValues.*;

/**
 * junit4 tests for Stanford University access_facet field
 * @author Naomi Dushay
 */
public class AccessTests extends AbstractStanfordTest {

	private String fldName = "access_facet";
    private final String onlineFldVal = Access.ONLINE.toString();
    private final String atLibraryFldVal = Access.AT_LIBRARY.toString();

@Before
	public void setup()
	{
		setTestLoggingLevels();
		mappingTestInit();
	}

	/**
	 * test the field in the context of the index
	 */
@Test
	public void testAccessFldInIx()
		throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("onlineFormat.mrc");
		assertEquals("accessMethod string incorrect: ", "Online", onlineFldVal);
		assertEquals("accessMethod string incorrect: ", "At the Library", atLibraryFldVal);
		IndexTest.stopTestJetty();
	}

// NOTE: can have multiple access types

	/**
	 * test accessMethod_facet value of "online" based on fulltext URLs in bib
	 */
@Test
	public void testAccessFromFulltextURL()
	{
    	String testFilePath = testDataParentPath + File.separator + "onlineFormat.mrc";
	    solrFldMapTest.assertSolrFldValue(testFilePath, "856ind2is0", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "856ind2is0Again", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "856ind2is1NotToc", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "856ind2isBlankFulltext", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "956BlankIndicators", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "956ind2is0", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "956and856TOC", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "mult856and956", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "956and856TOCand856suppl", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, onlineFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "newSfx", fldName, onlineFldVal);
	}

	/**
	 * test accessMethod_facet value of "online" based on sfx URLs in bib
	 */
@Test
	public void testAccessFromSfxURL()
	{
        String testFilePath = testDataParentPath + File.separator + "formatTests.mrc";

		// has SFX url in 956
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, onlineFldVal);
	}


	/**
	 * test accessMethod_facet value when the url is that of a GSB request
	 *  form for offsite books.
	 */
@Test
	public void testGSBRequestUrl()
	{
		String testFilePath = testDataParentPath + File.separator + "onlineFormat.mrc";

		solrFldMapTest.assertSolrFldValue(testFilePath, "123http", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "124http", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "1234https", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7423084", fldName, atLibraryFldVal);

		String urlFldName = "url";
		solrFldMapTest.assertNoSolrFld(testFilePath, "123http", urlFldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "124http", urlFldName);
		solrFldMapTest.assertNoSolrFld(testFilePath, "1234https", urlFldName);
	}


	/**
	 * test accessMethod_facet values from item library and location fields in
	 *  bib rec 999
	 */
@Test
	public void testAccessFrom999()
	{
    	String testFilePath = testDataParentPath + File.separator + "buildingTests.mrc";

	 	// "Online"
		// has SFX url in 956
		solrFldMapTest.assertSolrFldValue(testFilePath, "7117119", fldName, onlineFldVal);

	 	// "At the Library"
	 	// formerly "On campus"
		solrFldMapTest.assertSolrFldValue(testFilePath, "115472", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2442876", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "3142611", fldName, atLibraryFldVal);
	 	// formerly "Upon request"
	 	// SAL1 & 2
		solrFldMapTest.assertSolrFldValue(testFilePath, "1033119", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "1962398", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2328381", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2913114", fldName, atLibraryFldVal);
	 	// SAL3
		solrFldMapTest.assertSolrFldValue(testFilePath, "690002", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "3941911", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "7651581", fldName, atLibraryFldVal);
		solrFldMapTest.assertSolrFldValue(testFilePath, "2214009", fldName, atLibraryFldVal);
	 	// SAL-NEWARK
		solrFldMapTest.assertSolrFldValue(testFilePath, "804724", fldName, atLibraryFldVal);
	}


	private final MarcFactory factory = MarcFactory.newInstance();
	private ControlField cf008 = factory.newControlField("008");
	private DataField df999atLibrary = factory.newDataField("999", ' ', ' ');
	private DataField df999online = factory.newDataField("999", ' ', ' ');
	{
		df999online.addSubfield(factory.newSubfield('a', "INTERNET RESOURCE"));
		df999online.addSubfield(factory.newSubfield('w', "ASIS"));
		df999online.addSubfield(factory.newSubfield('i', "2475606-5001"));
		df999online.addSubfield(factory.newSubfield('l', "INTERNET"));
		df999online.addSubfield(factory.newSubfield('m', "SUL"));

		df999atLibrary.addSubfield(factory.newSubfield('a', "F152 .A28"));
		df999atLibrary.addSubfield(factory.newSubfield('w', "LC"));
		df999atLibrary.addSubfield(factory.newSubfield('i', "36105018746623"));
		df999atLibrary.addSubfield(factory.newSubfield('l', "HAS-DIGIT"));
		df999atLibrary.addSubfield(factory.newSubfield('m', "GREEN"));
	}

@Test
	public void testUpdatingLooseleaf()
	{
		// based on 9335774
		Leader ldr = factory.newLeader("01426cas a2200385Ia 4500");
		Record record = factory.newRecord();
		record.setLeader(ldr);
		cf008.setData("110912c20119999mnuar l       0    0eng  ");
		record.addVariableField(cf008);
		record.addVariableField(df999atLibrary);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, atLibraryFldVal);
		// not if online only
		record.removeVariableField(df999atLibrary);
		record.addVariableField(df999online);
		solrFldMapTest.assertSolrFldHasNumValues(record, fldName, 1);
		solrFldMapTest.assertSolrFldValue(record, fldName, onlineFldVal);
	}

}
