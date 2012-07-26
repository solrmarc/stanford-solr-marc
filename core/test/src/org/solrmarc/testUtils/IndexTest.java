package org.solrmarc.testUtils;

import static org.junit.Assert.*;
//import static org.solrmarc.testUtils.IndexTest.logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.*;
import org.apache.solr.common.params.CommonParams;
import org.junit.AfterClass;
import org.solrmarc.marc.MarcImporter;
import org.solrmarc.solr.*;
import org.solrmarc.tools.Utils;
import org.xml.sax.SAXException;

public abstract class IndexTest
{

	protected static MarcImporter importer;
	protected static SolrProxy solrProxy;
	protected static SolrServer solrJSolrServer;
	protected static SolrJettyProcess solrJettyProcess = null;

	protected static String docIDfname = "id";

	static Logger logger = Logger.getLogger(IndexTest.class.getName());

	// initialize these so they can be used for single argument indexing writing methods
	protected String testDataParentPath = System.getProperty("test.data.path");
	protected String testConfigFname = System.getProperty("test.config.file");
	protected String testSolrUrl = System.getProperty("test.solr.url");

	protected boolean useBinaryRequestHandler = Boolean.valueOf(System.getProperty("core.test.use_binary_request_handler", "true"));
	protected boolean useStreamingProxy = Boolean.valueOf(System.getProperty("core.test.use_streaming_proxy", "true"));
	protected static String testSolrLogLevel = System.getProperty("test.solr.log.level");
	protected static String testSolrMarcLogLevel = System.getProperty("test.solrmarc.log.level");

	/**
	 * Creates a pristine Solr index from the indicated test file of marc
	 * records. Uses a bunch of class instance variables.  Sends commit
	 *
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	protected void createFreshTestIxOverHTTP(String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testSolrUrl = getRequiredSystemProperty("test.solr.url");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

		createFreshTestIxOverHTTP(testConfigFname, testSolrUrl,	useBinaryRequestHandler, useStreamingProxy,
									testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Creates a pristine Solr index from the indicated test file of marc
	 * records. Uses a bunch of class instance variables.  Sends commit.
	 *
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	protected void createFreshTestIxOverHTTP(String testSolrUrl, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

		createFreshTestIxOverHTTP(testConfigFname, testSolrUrl,	useBinaryRequestHandler, useStreamingProxy,
									testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Creates a pristine Solr index from the indicated test file of marc
	 * records. Uses some class instance variables.  Sends commit.
	 *
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	protected void createFreshTestIxOverHTTP(String testSolrUrl, String marcTestDataFname,
											boolean useBinaryRequestHandler, boolean useStreamingProxy)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

		createFreshTestIxOverHTTP(testConfigFname, testSolrUrl,	useBinaryRequestHandler, useStreamingProxy,
									testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Create a pristine Solr index from the marc file, and send a commit.
	 *
	 * @param configPropFilename - name of config.properties file
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	public void createFreshTestIxOverHTTP(String configPropFilename, String testSolrUrl,
										boolean useBinaryRequestHandler, boolean useStreamingProxy,
										String testDataParentPath, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshTestIxOverHTTPNoCommit(configPropFilename, testSolrUrl, useBinaryRequestHandler, useStreamingProxy,
											testDataParentPath,	marcTestDataFname);
		solrProxy.commit(false); // don't optimize
	}

	/**
	 * Creates a pristine Solr index from the indicated test file of marc
	 * records, but doesn't commit. Uses a bunch of class instance variables.
	 *
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	protected void createFreshTestIxOverHTTPNoCommit(String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testSolrUrl = getRequiredSystemProperty("test.solr.url");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

		createFreshTestIxOverHTTPNoCommit(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy,
											testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Create a pristine Solr index from the marc file, but don't send commit.
	 *
	 * @param configPropFilename - name of config.properties file
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 * @throws SolrServerException when can't delete all docs before writing new docs
	 */
	public void createFreshTestIxOverHTTPNoCommit(String configPropFilename, String testSolrUrl,
												boolean useBinaryRequestHandler, boolean useStreamingProxy,
												String testDataParentPath, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		prepareToWriteToIndex(useBinaryRequestHandler, useStreamingProxy, testSolrUrl);

//		solrProxy.deleteAllDocs();
//		solrProxy.commit(false); // don't optimize
		solrJSolrServer.deleteByQuery("*:*");
		logger.debug("just deleted all docs known to the solrProxy");

		runMarcImporter(configPropFilename, testDataParentPath, marcTestDataFname);
	}

	/**
	 * Updates the Solr index from the indicated test file of marc records, and
	 * initializes necessary variables. Uses a bunch of class instance variables
	 *
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 */
	protected void updateTestIxOverHTTP(String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException
	{
		updateTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy,
							testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Updates the Solr index from the indicated test file of marc records, and
	 * initializes necessary variables. Uses a bunch of class instance variables
	 *
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 */
	protected void updateTestIxOverHTTP(String testSolrUrl, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException
	{
		testConfigFname = getRequiredSystemProperty("test.config.file");
		testDataParentPath = getRequiredSystemProperty("test.data.path");

		updateTestIxOverHTTP(testConfigFname, testSolrUrl, useBinaryRequestHandler, useStreamingProxy,
							testDataParentPath,	marcTestDataFname);
	}

	/**
	 * Updates the Solr index from the marc file.
	 *
	 * @param configPropFilename - name of config.properties file
	 * @param testSolrUrl - url for test solr instance, as a string
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 */
	public void updateTestIxOverHTTP(String configPropFilename,	String testSolrUrl,
									boolean useBinaryRequestHandler, boolean useStreamingProxy,
									String testDataParentPath, String marcTestDataFname)
			throws ParserConfigurationException, IOException, SAXException
	{
		prepareToWriteToIndex(useBinaryRequestHandler, useStreamingProxy, testSolrUrl);
		runMarcImporter(configPropFilename, testDataParentPath, marcTestDataFname);
		solrProxy.commit(false); // don't optimize
	}

	/**
	 * Check required properties; if needed, assign solrProxy and solrJSolrServer
	 * @param useBinaryRequestHandler - true to use the binary request handler
	 * @param useStreamingProxy - true to use streaming proxy (multiple records added at a time)
	 * @param testSolrUrl - url for test solr instance, as a string
	 */
	private void prepareToWriteToIndex(boolean useBinaryRequestHandler, boolean useStreamingProxy, String testSolrUrl)
	{
		if (solrJettyProcess == null)
			startTestJetty();

		solrProxy = SolrCoreLoader.loadRemoteSolrServer(testSolrUrl + "/update", useBinaryRequestHandler, useStreamingProxy);
		logger.debug("just set solrProxy to remote server at "	+ testSolrUrl + " - " + solrProxy.toString());
		solrJSolrServer = ((SolrServerProxy) solrProxy).getSolrServer();
	}

	/**
	 * set up MarcImporter and import the records in the file
	 * @param configPropFilename - name of config.properties file
	 * @param testDataParentPath - directory containing the test data file
	 * @param marcTestDataFname - file of marc records to be indexed. should end in ".mrc", "marc" or ".xml"
	 */
	private void runMarcImporter(String configPropFilename, String testDataParentPath, String marcTestDataFname)
			throws FileNotFoundException
	{
		if (marcTestDataFname != null)
		{
			importer = new MarcImporter(solrProxy);
			importer.init(new String[] { configPropFilename, testDataParentPath + File.separator + marcTestDataFname });
			importer.importRecords();
		}
	}


	/**
	 * delete the records from the test index via HTTP
	 *
	 * @param testSolrUrl - url for test solr instance, as a string
	 */
	public void deleteAllRecordsFromTestIx(String testSolrUrl)
			throws ParserConfigurationException, IOException, SAXException
	{
		if (solrJettyProcess == null)
			startTestJetty();

		solrJSolrServer = ((SolrServerProxy) solrProxy).getSolrServer();
		try
		{
			solrJSolrServer.deleteByQuery("*:*");
			solrJSolrServer.commit();
		}
		catch (SolrServerException e)
		{
			e.printStackTrace();
		}

		logger.debug("just deleted all docs known to the solrProxy");
	}


	/**
	 * delete the records specified  from the test index via HTTP
	 * Given the paths to a marc file to be indexed, the solr directory, and the
	 * path for the solr index, delete the records from the index.
	 *
	 * @param deletedIdsFilename  file containing record ids to be deleted (including parent path)
	 * @param testSolrUrl - url for test solr instance, as a string; used if solrProxy isn't initialized
	 * @param configPropFilename  name of config.properties file; used if MarcImporter isn't initialized
	 */
	public void deleteRecordsFromTestIx(String deletedIdsFilename, String testSolrUrl, String configPropFilename)
			throws ParserConfigurationException, IOException, SAXException
	{
		if (solrJettyProcess == null)
			startTestJetty();

		if (solrProxy == null)
		{
			solrProxy = SolrCoreLoader.loadRemoteSolrServer(testSolrUrl + "/update", useBinaryRequestHandler, useStreamingProxy);
			logger.debug("just set solrProxy to remote server at "	+ testSolrUrl + " - " + solrProxy.toString());
		}

		if (importer == null)
			importer = new MarcImporter(solrProxy);

		importer.init(new String[] { configPropFilename });
		importer.setDeleteRecordListFilename(deletedIdsFilename);
		importer.deleteRecords();
		solrProxy.commit(false); // don't optimize
		logger.debug("just deleted Solr docs per deleted ids file");
	}


	/**
	 * close and set solrProxy to null
	 */
// @After
	public static void closeSolrProxy()
	{
		// avoid "already closed" exception
		logger.debug("IndexTest closing solrProxy and setting to null");
		if (solrProxy != null)
		{
			logger.info("Closing solrProxy");
			solrProxy.close();
			solrProxy = null;
		}
	}


//************************** Assertion Methods *********************************

	/**
	 * assert there is a single doc in the index with the value indicated
	 *
	 * @param docId - the identifier of the SOLR/Lucene document
	 * @param fldName - the field to be searched
	 * @param fldVal - field value to be found
	 */
	public final void assertSingleResult(String docId, String fldName, String fldVal)
			throws ParserConfigurationException, SAXException, IOException
	{
		SolrDocumentList sdl = getDocList(fldName, fldVal);
		if (sdl.size() == 1)
		{
			SolrDocument doc = sdl.get(0);
			Object field = doc.getFieldValue(docIDfname);
			if (field.toString().equals(docId))
				return;
			fail("There is a single doc with " + fldName + " of " + fldVal + " but it is not doc \"" + docId + "\"");
		}
		if (sdl.size() == 0)
			fail("There is no doc with " + fldName + " of " + fldVal);
		if (sdl.size() > 1)
			fail("There is more than 1 doc with " + fldName + " of " + fldVal);
	}

	public final void assertZeroResults(String fldName, String fldVal)
	{
		assertResultSize(fldName, fldVal, 0);
	}

	/**
	 * asserts that the document is present in the index
	 */
	public final void assertDocPresent(String doc_id)
	{
		SolrDocument doc = getDocument(doc_id);
		assertTrue("Found no document with id \"" + doc_id + "\"", doc != null);
	}

    /**
	 * asserts that the document is NOT present in the index
	 */
	public final void assertDocNotPresent(String doc_id)
	{
		SolrDocument doc = getDocument(doc_id);
		assertTrue("Unexpectedly found document with id \"" + doc_id + "\"", doc == null);
	}

	public final void assertDocHasFieldValue(String doc_id, String fldName,	String fldVal)
	{
		SolrDocument doc = getDocument(doc_id);
		if (doc != null)
		{
			Collection<Object> valObjs = doc.getFieldValues(fldName);
			if (valObjs != null)
				for (Object valObj : valObjs)
				{
					if (valObj.toString().equals(fldVal))
						// found field with desired value
						return;
				}
			fail("Field " + fldName + " did not contain value \"" + fldVal + "\" in doc " + doc_id);
		}
		fail("Document " + doc_id + " was not found");
	}


	public final void assertDocHasNoFieldValue(String doc_id, String fldName, String expFldVal)
	{
		SolrDocument doc = getDocument(doc_id);
		if (doc != null)
		{
			Collection<Object> valObjects = doc.getFieldValues(fldName);
			if (valObjects != null && valObjects.size() > 0)
			{
				for (Object valObj : valObjects)
				{
					if (valObj.toString().equals(expFldVal))
						fail("Field " + fldName + " contained value \"" + expFldVal + "\" in doc " + doc_id);
				}
				return;
			}
			return;
		}
		fail("Document " + doc_id + " was not found");
	}

	@SuppressWarnings("unchecked")
	public final void assertDocHasNoField(String doc_id, String fldName)
	{
		SolrDocument doc = getDocument(doc_id);
		if (doc == null)
			fail("Document " + doc_id + " was not found");
		else
		{
			Collection<Object> valObjects = doc.getFieldValues(fldName);
			if (valObjects == null || valObjects.size() == 0)
				// Document has no field by that name. yay.
				return;
			fail("Field " + fldName + " found in doc \"" + doc_id + "\"");
		}
	}

	/**
	 * Do a search for the implied term query and assert the search results have
	 * docIds that are an exact match of the set of docIds passed in
	 *
	 * @param fldName - name of the field to be searched
	 * @param fldVal - value of the field to be searched
	 * @param docIds - Set of doc ids expected to be in the results
	 */
	public final void assertSearchResults(String fldName, String fldVal, Set<String> docIds)
	{
		SolrDocumentList sdl = getDocList(fldName, fldVal);

		assertTrue("Expected " + docIds.size() + " documents for " + fldName + " search \"" + fldVal + "\" but got " + sdl.size(),
					docIds.size() == sdl.size());

		String msg = fldName + " search \"" + fldVal + "\": ";
		for (SolrDocument doc : sdl)
		{
			assertDocInSet(doc, docIds, msg);
		}
	}

	public final void assertDocInSet(SolrDocument doc, Set<String> docIds, String msgPrefix)
	{
		String id = doc.getFieldValue(docIDfname).toString();
		if (docIds.contains(id))
			return;
		fail(msgPrefix + "doc \"" + id + "\" missing from list");
	}

	/**
	 * ensure that the value(s) for the two fields in the document are the same
	 *
	 * @param docId - the id of the document
	 * @param fldName1 - the first field to match
	 * @param fldName2 - the second field to match
	 */
	public final void assertFieldValuesEqual(String docId, String fldName1, String fldName2)
			throws ParserConfigurationException, SAXException, IOException
	{
		SolrDocument doc = getDocument(docId);
		if (doc != null)
		{
			Collection<Object> fld1ValObjects = doc.getFieldValues(fldName1);
			int numValsFld1 = fld1ValObjects.size();
			Collection<Object> fld2ValObjects = doc.getFieldValues(fldName1);
			int numValsFld2 = fld2ValObjects.size();

			String errmsg = "fields " + fldName1 + ", " + fldName2	+ " have different numbers of values";
			assertEquals(errmsg, numValsFld1, numValsFld2);

			List<String> fld2ValList = Arrays.asList(fld2ValObjects.toArray(new String[numValsFld2]));
			for (Object fld1ValObj : fld1ValObjects)
			{
				if (!fld2ValList.contains(fld1ValObj.toString()))
				{
					errmsg = "In doc " + docId + ", field " + fldName1 + " has value not in " + fldName2 + ": ";
					fail(errmsg + fld1ValObj.toString());
				}
			}
		}
		fail("Document " + docId + " was not found");
	}

	/**
	 * Assert the number of documents matching the implied term search equals
	 * the expected number
	 *
	 * @param fldName - the field to be searched
	 * @param fldVal - field value to be found
	 * @param numExp - the number of documents expected
	 */
	public final void assertResultSize(String fldName, String fldVal, int numExp)
	{
		int numActual = getNumMatchingDocs(fldName, fldVal);
		assertTrue("Expected " + numExp + " documents for " + fldName + " search \"" + fldVal + "\" but got " + numActual,
					numActual == numExp);
	}


//*********************** Additional Methods **********************************

	/**
	 * return the number of docs that match the implied term query
	 *
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 */
	public int getNumMatchingDocs(String fld, String value)
	{
		return (getDocList(fld, value).size());
	}

	/**
	 * Given an index field name and value, return a list of Documents that
	 * match the term query sent to the index, sorted in ascending order per the
	 * sort fld
	 *
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted (ascending)
	 * @return org.apache.solr.common.SolrDocumentList
	 */
	public final SolrDocumentList getAscSortDocs(String fld, String value, String sortfld)
	{
		return getSortedDocs(fld, value, sortfld, SolrQuery.ORDER.asc);
	}

	/**
	 * Given an index field name and value, return a list of Documents that
	 * match the term query sent to the index, sorted in descending order per
	 * the sort fld
	 *
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted (descending)
	 * @return org.apache.solr.common.SolrDocumentList
	 */
	public final SolrDocumentList getDescSortDocs(String fld, String value,	String sortfld)
	{
		return getSortedDocs(fld, value, sortfld, SolrQuery.ORDER.desc);
	}

	/**
	 * Given an index field name and value, return a list of Documents that
	 * match the term query sent to the index, sorted in descending order per
	 * the sort fld
	 *
	 * @param fld - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @param sortfld - name of the field by which results should be sorted (descending)
	 * @param sortOrder = SolrQuery.ORDER.asc  or  SolrQuery.ORDER.desc
	 * @return org.apache.solr.common.SolrDocumentList
	 */
	public final SolrDocumentList getSortedDocs(String fld, String value, String sortfld, SolrQuery.ORDER sortOrder)
	{
		SolrQuery query = new SolrQuery(fld + ":" + value);
		query.setQueryType("standard");
		query.setFacet(false);
		query.setSortField(sortfld, sortOrder);
		query.setRows(75);
		try
		{
			QueryResponse response = solrJSolrServer.query(query);
			return (response.getResults());
		}
		catch (SolrServerException e)
		{
			e.printStackTrace();
			return (new SolrDocumentList());
		}
	}

	/**
	 * Get the Lucene document with the given id from the solr index at the solrDataDir
	 *
	 * @param doc_id - the unique id of the lucene document in the index
	 * @return SolrDocument matching the given id
	 */
	public final SolrDocument getDocument(String doc_id)
	{
		SolrDocumentList sdl = getDocList(docIDfname, doc_id);
		switch (sdl.size())
		{
			case 0:
				return null;
			case 1:
				return sdl.get(0);
			default:
				assertTrue("Got multiple docs with id " + doc_id, sdl.size() < 2);
		}
		return null;
	}

	/**
	 * Get the List of Solr Documents with the given value for the given field
	 *
	 * @param field - the name of the field to be searched in the lucene index
	 * @param value - the string to be searched in the given field
	 * @return org.apache.solr.common.SolrDocumentList
	 */
	public final SolrDocumentList getDocList(String field, String value)
	{
		SolrQuery query = new SolrQuery(field + ":" + value);
		query.setQueryType("standard");
		query.setFields("*");
		query.setFacet(false);
		query.setRows(35);
		try
		{
			QueryResponse response = solrJSolrServer.query(query);
			return (response.getResults());
		} catch (SolrServerException e)
		{
			e.getCause().printStackTrace();
			fail("caught exception while searching for value " + value + " in field " + field);
		}
		return (new SolrDocumentList());
	}

	/**
	 * Request record by id from Solr as JSON, and return the raw value of the
	 *  field (note that XML response does not preserve the raw value of the field.)
	 *  If the record doesn't exist id or the record doesn't contain that field return null
	 *
	 *  NOTE:  does NOT work for retrieving binary values
	 *
	 *  @param desiredFld - the field from which we want the value
	 */
	public String getFirstFieldValViaJSON(String id, String desiredFld)
	{
		SolrDocument doc = null;

		SolrQuery query = new SolrQuery(docIDfname + ":" + id);
		query.setQueryType("standard");
		query.setFacet(false);
		query.setParam(CommonParams.WT, "json");
		try
		{
			QueryResponse response = ((CommonsHttpSolrServer) solrJSolrServer).query(query);
			SolrDocumentList docList = response.getResults();
			for (SolrDocument d : docList)
				doc = d;
		}
		catch (SolrServerException e)
		{
			e.getCause().printStackTrace();
			// e.printStackTrace();
		}

		if (doc == null)
			return null;
		Object fieldValObj = doc.getFieldValue(desiredFld);
		if (fieldValObj.getClass() == java.lang.String.class)
			return (String) fieldValObj;

		return null;
	}

	/**
	 * getFldValPreserveBinary - Request record by id from Solr, and return the raw
	 *  value of the field. If the record doesn't exist id or the record
	 * doesn't contain that field return null
	 *  @param desiredFld - the field from which we want the value
	 */
	public String getFldValPreserveBinary(String id, String desiredFld)
	{
		String fieldValue = null;
		String selectStr = "select/?q=id%3A" + id + "&fl=" + desiredFld + "&rows=1&wt=json&qt=standard&facet=false";
		try
		{
			InputStream is = new URL(testSolrUrl + "/" + selectStr).openStream();
			String solrResultStr = Utils.readStreamIntoString(is);
			String fieldLabel = "\"" + desiredFld + "\":";
			int valStartIx = solrResultStr.indexOf(fieldLabel);
			int valEndIx = solrResultStr.indexOf("\"}]");
			if (valStartIx != -1 && valEndIx != -1)
				fieldValue = solrResultStr.substring(valStartIx + fieldLabel.length(), valEndIx);
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return (fieldValue);
	}

	public static void setTestLoggingLevels()
	{
		Utils.setLoggingLevels(testSolrLogLevel, testSolrMarcLogLevel);
	}


	/**
	 * @param propertyName
	 *            the name of the System property
	 * @return the value of the property; fail if there is no value
	 */
	protected static String getRequiredSystemProperty(String propertyName)
	{
		String propVal = System.getProperty(propertyName);
		if (propVal == null)
			fail("property " + propertyName	+ " must be defined for the tests to run");
		return propVal;
	}

	/**
	 * Start a Jetty driven solr server running in a separate JVM at port
	 * jetty.test.port
	 */
	public static void startTestJetty()
	{
		String testSolrHomeDir = getRequiredSystemProperty("test.solr.path");
		String jettyDir = getRequiredSystemProperty("test.jetty.dir");
		String jettyTestPortStr = getJettyPort();

		solrJettyProcess = new SolrJettyProcess(testSolrHomeDir, jettyDir, jettyTestPortStr);
		boolean serverIsUp = false;
		try
		{
			serverIsUp = solrJettyProcess.startProcessWaitUntilSolrIsReady();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			stopTestJetty();
			fail("Jetty server did not become available");
		}
		if (!serverIsUp)
		{
			stopTestJetty();
			fail("Jetty server did not become available");
		}

		// If you need to see the output of the solr server after the server is up and running, call
		// solrJettyProcess.outputReset() here to empty the buffer so the later output is visible in the Eclipse variable viewer
		// solrJettyProcess.outputReset();
		logger.info("Server is up and running at " + jettyDir + ", port " + solrJettyProcess.getJettyPort());
	}

	/**
	 * Look for a value for system property test.jetty.port; if none, use 0.
	 *
	 * @return the port to use for the jetty server.
	 */
	private static String getJettyPort()
	{
		String jettyTestPortStr;
		jettyTestPortStr = System.getProperty("test.jetty.port");
		// Specify port 0 to select any available port
		if (jettyTestPortStr == null)
			jettyTestPortStr = "0";
		return jettyTestPortStr;
	}

	/**
	 * stop the Jetty server if it is running
	 */
	public static void stopTestJetty()
	{
		if (solrJettyProcess != null && solrJettyProcess.isServerRunning())
			solrJettyProcess.stopServer();
		solrJettyProcess = null;
	}

	/**
	 * stop the jetty server if it is running and release solrProxy
	 */
@AfterClass
	public static void afterClassDefault()
	{
    	stopTestJetty();
    	closeSolrProxy();
	}

}