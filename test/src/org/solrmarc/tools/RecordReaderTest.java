package org.solrmarc.tools;

import static org.junit.Assert.fail;

import java.io.*;

import org.junit.Test;
import org.marc4j.*;
import org.marc4j.marc.Record;
import org.solrmarc.marc.MarcCombiningReader;
import org.solrmarc.marc.RawRecordReader;
import org.solrmarc.marcoverride.MarcSplitStreamWriter;
import org.solrmarc.testUtils.RecordTestingUtils;


public class RecordReaderTest
{
    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
    @Test
    public void testRawRecordReader()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        RawRecordReader rawReader = null;
        MarcReader reader = null;
        try
        {
            rawReader = new RawRecordReader(new FileInputStream(new File(testDataParentPath, "u4.mrc")));
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "u4.mrc")), true, true, "MARC8");
            
            RawRecord rawRec = null;
            if (rawReader.hasNext()) rawRec = rawReader.next();
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            Record rec2 = rawRec.getAsRecord(true, true, null, "MARC8");
            RecordTestingUtils.assertRecordsEquals("record read via RawReader different from record read via Permissive reader", rec, rec2);

            rawReader = new RawRecordReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")));
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")), true, true, "MARC8");
            
            rawRec = null;
            if (rawReader.hasNext()) rawRec = rawReader.next();
            rec = null;
            if (reader.hasNext())  rec = reader.next();
            rec2 = rawRec.getAsRecord(true, true, null, "MARC8");
            RecordTestingUtils.assertRecordsEquals("record read via RawReader different from record read via Permissive reader", rec, rec2);
}
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  u4.mrc");
        }
    }
    /**
     * unit test for org.solrmarc.marcoverride.MarcSplitStreamWriter and org.solrmarc.marc.MarcCombiningReader
     */
    @Test
    public void testCombiningReaderAndSplitStreamWriter()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        MarcReader reader = null;
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")), true, true, "MARC8");
            
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MarcSplitStreamWriter writer = new MarcSplitStreamWriter(output, "UTF-8", 70000, "991");
            writer.write(rec);
            writer.close();
            MarcReader reader2 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            MarcReader reader3 = new MarcCombiningReader(reader2, "991", null, null);

            Record rec2 = null;
            if (reader3.hasNext()) rec2 = reader3.next(); 
            RecordTestingUtils.assertRecordsEquals("record read directly is different from record read in written using SplitStreamWriter, and combined again", rec, rec2);
            
            MarcReader reader4 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            Record rec4 = null;
            if (reader4.hasNext()) rec4 = reader4.next();
            RecordTestingUtils.assertRecordIsSubset("record read directly ought to be different from record read in written using SplitStreamWriter, but not combined reader", rec, rec4);

        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  bad_too_long_plus_2.mrc");
        }
    }
    /**
     * unit test for org.solrmarc.marcoverride.MarcSplitStreamWriter and org.solrmarc.marc.RawRecordReader
     */
    @Test
    public void testRawRecordCombiningAndSplitStreamWriter()
    {
        System.setProperty("org.marc4j.marc.MarcFactory", "org.solrmarc.marcoverride.NoSortMarcFactoryImpl");
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        MarcReader reader = null;
        try
        {
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "bad_too_long_plus_2.mrc")), true, true, "MARC8");
            
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MarcSplitStreamWriter writer = new MarcSplitStreamWriter(output, "UTF-8", 70000, "991");
            writer.write(rec);
            writer.close();
            MarcReader reader2 = new MarcPermissiveStreamReader(new ByteArrayInputStream(output.toByteArray()), true, true, "MARC8");
            RawRecordReader reader3 = new RawRecordReader(new ByteArrayInputStream(output.toByteArray()));

            RawRecord rawRec = null;
            if (reader3.hasNext()) rawRec = reader3.next(); 
            Record rec2 = rawRec.getAsRecord(true, true, "991", "MARC8");
            RecordTestingUtils.assertRecordsEquals("record read directly is different from record read in written using SplitStreamWriter, and combined again", rec, rec2);
        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  bad_too_long_plus_2.mrc");
        }
    }
}
