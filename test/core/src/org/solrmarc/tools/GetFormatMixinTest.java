package org.solrmarc.tools;

import static org.junit.Assert.fail;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.index.SolrIndexer;


public class GetFormatMixinTest
{
    @Before
    public void setUp()
    {
    	String testDataPath = System.getProperty("test.data.path");
        if (testDataPath == null)
        {
            testDataPath = "test" + File.separator + "core" + File.separator + "data";
            System.setProperty("test.data.path", testDataPath);
        }
    }

    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
    @Test
    public void testGetFormatMixin()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        MarcReader reader = null;
        Properties indexingProps = new Properties();
        indexingProps.setProperty("getformatmixin", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes");
//        indexingProps.setProperty("getformatmixinmapped", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes, getformat_mixin_map.properties");
        indexingProps.setProperty("getformatmixinmapped", "custom(org.solrmarc.index.GetFormatMixin), getContentTypesAndMediaTypes, setup/core/translation_maps/getformat_mixin_map.properties");
        String verboseStr = System.getProperty("marc.verbose");
        boolean verbose = (verboseStr != null && verboseStr.equalsIgnoreCase("true"));
        ErrorHandler errors = new ErrorHandler();
        PrintStream out = null;
        if (verbose)
        {
            try
            {
                out = new PrintStream(System.out, true, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
            } 
        }
//        SolrIndexer testIndexer = SolrIndexer.indexerFromProperties(indexingProps, new String[]{"translation_maps"});
        SolrIndexer testIndexer = SolrIndexer.indexerFromProperties(indexingProps, new String[]{"setup/core/translation_maps"});
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "formatRecs.mrc")), true, true, "MARC8");
            while (reader.hasNext())
            {
                Record record = reader.next();
                Map<String,Object> indexedRecord = testIndexer.map(record, errors);
                String id = record.getControlNumber();
                Object result = indexedRecord.get("getformatmixin");
                showResults(result, "raw   ", verbose, out, id);
                result = indexedRecord.get("getformatmixinmapped");
                showResults(result, "mapped", verbose, out, id);
                if (verbose) 
                {
                    if (errors.hasErrors())
                    {
                        for (Object error : errors.getErrors())
                        {
                            out.println(error.toString());                            
                        }
                    }
                    out.println(record.toString());
                }
                indexedRecord = testIndexer.map(record);
                errors.reset();
            }
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test recordfile  formatTests.mrc");
        }
        System.out.println("Test testRemoteIndexRecord is successful");
    }

    private void showResults(Object result, String label, boolean verbose, PrintStream out, String id)
    {
        if (result instanceof String)
        {
            String format = result.toString();
            if (verbose) out.println(id + "("+label+") = " + format);
        }
        else if (result instanceof Set)
        {
            Set<String> formats = (Set<String>)result;
            for (String format : formats)
            {
                if (verbose) out.println(id + "("+label+") = " + format);
            }
        }
        
    }
}
