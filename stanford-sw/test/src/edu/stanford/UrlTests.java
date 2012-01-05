package edu.stanford;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.*;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
//import org.solrmarc.solr.DocumentProxy;
import org.xml.sax.SAXException;


/**
 * junit4 tests for Stanford University's url fields
 * @author Naomi Dushay
 */
public class UrlTests extends AbstractStanfordTest {

	private final String testDataFname = "onlineFormat.mrc";

@Before
	public final void setup() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		createFreshIx(testDataFname);
	}

	/**
	 * test url_sfx_display field
	 */
@Test
	public final void testSFXUrls() 
			throws ParserConfigurationException, IOException, SAXException
	{
		String fldName = "url_sfx";
			
		// 956 SFX fields
		assertDocHasFieldValue("mult856and956", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?superLongURL"); 
		assertDocHasFieldValue("7117119", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?url_ver=Z39.88-2004&ctx_ver=Z39.88-2004&ctx_enc=info:ofi/enc:UTF-8&rfr_id=info:sid/sfxit.com:opac_856&url_ctx_fmt=info:ofi/fmt:kev:mtx:ctx&sfx.ignore_date_threshold=1&rft.object_id=110978984448763&svc_val_fmt=info:ofi/fmt:kev:mtx:sch_svc&"); 
		assertDocHasFieldValue("newSfx", fldName, "http://library.stanford.edu/sfx?reallyLongLotsOfArgs"); 
		
		// 956 non-SFX fields (a representative few of them)
		assertDocHasNoField("956BlankIndicators", fldName);
		assertDocHasNoField("956ind2is0", fldName);
	}
	
	
	
	/**
	 * Test method for {@link edu.stanford.StanfordIndexer#getFullTextUrl(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testFullTextUrls() throws IOException, ParserConfigurationException, SAXException 
	{
		String fldName = "url_fulltext";
	
		// fulltext url(s) in docs 
		assertDocHasFieldValue("856ind2is0", fldName, "http://www.netLibrary.com/urlapi.asp?action=summary&v=1&bookid=122436"); 
		assertDocHasFieldValue("856ind2is0Again", fldName, "http://www.url856.com/fulltext/ind2_0"); 
		assertDocHasFieldValue("856ind2is1NotToc", fldName, "http://www.url856.com/fulltext/ind2_1/not_toc"); 
		assertDocHasFieldValue("856ind2isBlankFulltext", fldName, "http://www.url856.com/fulltext/ind2_blank/not_toc"); 
		assertDocHasFieldValue("956BlankIndicators", fldName, "http://www.url956.com/fulltext/blankIndicators"); 
		assertDocHasFieldValue("956ind2is0", fldName, "http://www.url956.com/fulltext/ind2_is_0"); 
		assertDocHasFieldValue("956and856TOC", fldName, "http://www.url956.com/fulltext/ind2_is_blank"); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.sciencemag.org/"); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.jstor.org/journals/00368075.html"); 
		assertDocHasFieldValue("mult856and956", fldName, "http://www.sciencemag.org/archive/"); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url956.com/fulltext/ind2_is_blank"); 
		
		// SFX url
		assertDocHasNoFieldValue("mult856and956", fldName, "http://caslon.stanford.edu:3210/sfxlcl3?superLongURL"); 
	
		// docs with no fulltext url in bib rec
		assertDocHasNoField("856ind2is1TocSubz", fldName); 
		assertDocHasNoField("856ind2is1TocSub3", fldName); 
		assertDocHasNoField("856ind2is2suppl", fldName); 
		assertDocHasNoField("856ind2isBlankTocSubZ", fldName); 
		assertDocHasNoField("856ind2isBlankTocSub3", fldName); 
		assertDocHasNoField("856tocAnd856SupplNoFulltext", fldName);
		
		// don't get jackson forms for off-site paging requests
		assertDocHasNoField("123http", fldName);
		assertDocHasNoField("124http", fldName);
	}
	

	/**
	 * Test url_suppl_display field contents
	 * Test method for {@link org.solrmarc.tools.MarcUtils#getSupplUrls(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testSupplmentaryUrls() 
			throws ParserConfigurationException, IOException, SAXException 
	{
		String fldName = "url_suppl";
			
		// book
		assertDocHasFieldValue("856ind2is1TocSubz", fldName, "http://www.url856.com/ind2_1/toc_subz"); 
		assertDocHasFieldValue("856ind2is1TocSub3", fldName, "http://www.url856.com/ind2_1/toc_sub3"); 
		assertDocHasFieldValue("856ind2is2suppl", fldName, "http://www.url856.com/ind2_2/supplementaryMaterial"); 
		assertDocHasFieldValue("856ind2isBlankTocSubZ", fldName, "http://www.url856.com/ind2_blank/toc_subz"); 
		assertDocHasFieldValue("856ind2isBlankTocSub3", fldName, "http://www.url856.com/ind2_blank/toc_sub3"); 
		assertDocHasFieldValue("956and856TOC", fldName, "http://www.url856.com/toc"); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url856.com/ind2_2/supplMaterial"); 
		assertDocHasFieldValue("956and856TOCand856suppl", fldName, "http://www.url856.com/toc"); 
		assertDocHasFieldValue("856tocAnd856SupplNoFulltext", fldName, "http://www.url856.com/toc"); 
		assertDocHasFieldValue("856tocAnd856SupplNoFulltext", fldName, "http://www.url856.com/ind2_2/supplMaterial"); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/samples/prin031/2001032103.html"); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/toc/prin031/2001032103.html"); 
		assertDocHasFieldValue("7423084", fldName, "http://www.loc.gov/catdir/description/prin022/2001032103.html"); 
	
		// docs with no urlSuppl_store in bib rec
		assertDocHasNoField("856ind2is0", fldName); 
		assertDocHasNoField("856ind2is0Again", fldName); 
		assertDocHasNoField("856ind2is1NotToc", fldName); 
		assertDocHasNoField("856ind2isBlankFulltext", fldName); 
		assertDocHasNoField("956BlankIndicators", fldName); 
		assertDocHasNoField("956ind2is0", fldName); 
		assertDocHasNoField("mult856and956", fldName); 
	}



	/**
	 * Test url_restricted field contents - should contain only full text urls
	 *  for restricted content
	 * Test method for {@link edu.stanford.StanfordIndexer#getRestrictedUrls(org.marc4j.marc.Record)}.
	 */
@Test
	public final void testRestrictedUrls() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException
	{
		createFreshIx("restrictedUrlTests.mrc");
		String fldName = "url_restricted";
			
		assertDocHasFieldValue("restrictedUrl1", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("restrictedUrl2", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("fulltextAndRestricted1", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("fulltextAndRestricted2", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("supplAndRestricted1", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("supplAndRestricted2", fldName, "http://restricted.org"); 
		assertDocHasFieldValue("restrictedFullTextAndSuppl", fldName, "http://restricted.org"); 
		
		assertDocHasNoField("fulltextUrl", fldName); 
		assertDocHasNoField("supplUrl", fldName); 
		assertDocHasNoField("supplUrlRestricted", fldName); 

		// retain unrestricted fulltext urls
		String fullTextFldName = "url_fulltext";
		assertDocHasFieldValue("fulltextUrl", fullTextFldName, "http://www.fulltext.org/"); 
		assertDocHasFieldValue("fulltextAndRestricted1", fullTextFldName, "http://www.fulltext.org/"); 
		assertDocHasFieldValue("fulltextAndRestricted2", fullTextFldName, "http://www.fulltext.org/"); 

		// retain any sort of suppl urls (do not included restriced supplemental urls in url_restricted field.
		String supplFldName = "url_suppl";
		assertDocHasFieldValue("supplUrl", supplFldName, "http://www.suppl.com"); 
		assertDocHasFieldValue("supplUrlRestricted", supplFldName, "http://www.suppl.com/restricted"); 
		assertDocHasFieldValue("supplAndRestricted1", supplFldName, "http://www.suppl.com"); 
		assertDocHasFieldValue("supplAndRestricted2", supplFldName, "http://www.suppl.com"); 
		assertDocHasFieldValue("restrictedFullTextAndSuppl", supplFldName, "http://www.suppl.com/restricted"); 
	}


	/**
	 * test preservation of field ordering from marc21 input to marc21 stored in record
	 */
@Test
	public final void testFieldOrdering() 
			throws ParserConfigurationException, IOException, SAXException, SolrServerException 
	{
		createFreshIx("urlOrderingTests.mrc");
//		int solrDocNum = getSingleDocNum(docIDfname, "fulltextOnly");
//		DocumentProxy doc = getSearcherProxy().getDocumentProxyBySolrDocNum(solrDocNum);
		SolrDocument doc = getDocument("fulltextOnly");
//		assertFieldOrder(doc.getValuesForField("url_fulltext"));
		assertFieldOrder(doc.getFieldValues("url_fulltext"));
		
		doc = getDocument("fulltextAndRestricted1");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));

		doc = getDocument("fulltextAndRestricted2");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));

		doc = getDocument("fulltextAndRestricted3");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));

		doc = getDocument("fulltextAndRestricted4");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));
		
		doc = getDocument("fulltextAndSuppl1");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));
		assertFieldOrder(doc.getFieldValues("url_suppl"));

		doc = getDocument("fulltextAndSuppl2");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));
		assertFieldOrder(doc.getFieldValues("url_suppl"));

		doc = getDocument("fulltextAndSuppl3");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));
		assertFieldOrder(doc.getFieldValues("url_suppl"));

		doc = getDocument("fulltextAndSuppl4");
		assertFieldOrder(doc.getFieldValues("url_restricted"));
		assertFulltextUrlFieldOrder(doc.getFieldValues("url_fulltext"));
		assertFieldOrder(doc.getFieldValues("url_suppl"));
	}

	private void assertFieldOrder(String[] urls) {
		assertTrue("urls are NOT in the original order: " + urls[0] + " should be first url.", urls[0].indexOf("first") > 1);
		assertTrue("urls are NOT in the original order: " + urls[1] + " should be second url.", urls[1].indexOf("second") > 1);
	}

	private void assertFieldOrder(Collection<Object> urlObjs) {
		String[] urls = urlObjs.toArray(new String[urlObjs.size()]);
		assertTrue("urls are NOT in the original order: " + urls[0] + " should be first url.", urls[0].indexOf("first") > 1);
		assertTrue("urls are NOT in the original order: " + urls[1] + " should be second url.", urls[1].indexOf("second") > 1);
	}

//	private void assertFulltextUrlFieldOrder(String[] urls) {
	private void assertFulltextUrlFieldOrder(Collection<Object> urlObjs) {
		String[] urls = urlObjs.toArray(new String[urlObjs.size()]);
		String firstUrl = urls[0];
		String secondUrl = urls[1];
		if (urls.length == 2) {
			assertTrue("urls are NOT in the original order: " + firstUrl + " should be first url.", firstUrl.indexOf("first") > 1);
			assertTrue("urls are NOT in the original order: " + secondUrl + " should be second url.", secondUrl.indexOf("second") > 1);
		}
		if (urls.length == 4) {
			assertTrue("urls are NOT in the original order: " + firstUrl + " should be first url.", firstUrl.indexOf("first") > 1);
			String lastUrl = urls[3];
			assertTrue("urls are NOT in the original order: " + lastUrl + " should be a second url.", lastUrl.indexOf("second") > 1);
		}
	}
}
