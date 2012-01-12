package org.solrmarc.tools;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.*;


public class HathiJsonReaderTest
{
//    @Before
    public void setUp()
    {
    	String testDataPath = System.getProperty("test.data.path");
        if (testDataPath == null)
        {
            testDataPath = "core" + File.separator + "test" + File.separator + "data";
            System.setProperty("test.data.path", testDataPath);
        }
    }

    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
@Test
    public void testHathiJsonRecordReader()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        HathiJsonToMarc hathiReader = null;
        MarcReader reader = null;
        try
        {
            hathiReader = new HathiJsonToMarc(new FileInputStream(new File(testDataParentPath, "009888737.json")), true);
            reader = new MarcPermissiveStreamReader(new FileInputStream(new File(testDataParentPath, "009888737.mrc")), true, true, "UTF8");
            
            Record hathiRec = null;
            if (hathiReader.hasNext()) hathiRec = hathiReader.next();
            Record rec = null;
            if (reader.hasNext())  rec = reader.next();
            assertRecordsEquals("record read via HathiReader different from record read via Permissive reader", rec, hathiRec);

        }
        catch (FileNotFoundException e)
        {
            fail("unable to read test record  009888737.json  or  009888737.mrc");
        }
    }

    /**
     * unit test for org.solrmarc.marc.RawRecordReader and org.solrmarc.tools.RawRecord
     */
@Test
    public void testHathiPlunderer()
    {
        String testDataParentPath = System.getProperty("test.data.path");
        if (testDataParentPath == null)
            fail("property test.data.path must be defined for the tests to run");
        BufferedReader hathiRecNumList = HathiPlunderer.initReader(new String[]{ testDataParentPath+"/hathi_upd_list.txt", testDataParentPath+"/hathi_upd_20110911.txt", testDataParentPath+"/hathi_upd_20110904.txt.gz"} );
        
        HathiPlunderer hathiPlunderer = new HathiPlunderer(hathiRecNumList, 15, 15, 6);
        MarcReader hathiReader = new HathiJsonToMarc(hathiPlunderer, true);
        int cnt = 0;
        while (hathiReader.hasNext())
        {
            Record record = hathiReader.next();
            if (cnt == 0)  assertTrue("Error: ID of first record actually read should be 009706555 it actually is "+ record.getControlNumber() , record.getControlNumber().equals("009706555"));
            if (cnt == 5)  assertTrue("Error: ID of 6th record actually read should be 001446104 it actually is "+ record.getControlNumber() , record.getControlNumber().equals("001446104"));
            if (cnt == 9)  assertTrue("Error: ID of 10th record actually read should be 010157869 it actually is "+ record.getControlNumber() , record.getControlNumber().equals("010157869"));
            if (cnt == 13) assertTrue("Error: ID of 14th record actually read should be 008884485 it actually is "+ record.getControlNumber() , record.getControlNumber().equals("008884485"));
            cnt++;
        }
        assertTrue("Error: Should have read 15 records, actually read: "+ cnt +" record", cnt == 15);
    }

//---------------------- private methods ----------------
    
    private void assertRecordsEquals(String message, Record rec1, Record rec2)
    {
        int result = compareRecords(rec1, rec2);
        String messageMore = null;
        if (result == 1) message = "Control Fields are different between rec1 and rec2";
        else if (result == 2) message = "Subfields are different between rec1 and rec2";
        else if (result == 3) message = "One record has a DataField where another has a ControlField";
        else if (result == -1) message = "Done with one record but not the other";
        assertEquals(message+" "+messageMore, 0, result);
    }
        
    private int compareRecords(Record rec1, Record rec2)
    {
        List<VariableField> fields1 = (List<VariableField>)rec1.getVariableFields();
        List<VariableField> fields2 = (List<VariableField>)rec2.getVariableFields();
        Iterator<VariableField> iter1 = fields1.iterator();
        Iterator<VariableField> iter2 = fields2.iterator();
        while (iter1.hasNext() && iter2.hasNext())
        {
            VariableField f1 = iter1.next();
            VariableField f2 = iter2.next();
            if (f1 instanceof ControlField && f2 instanceof ControlField)
            {
                ControlField cf1 = (ControlField)f1;
                ControlField cf2 = (ControlField)f2;
                if (! cf1.getData().equals(cf2.getData()))  
                	return(1);
            }
            else if (f1 instanceof DataField && f2 instanceof DataField)
            {
                DataField df1 = (DataField)f1;
                DataField df2 = (DataField)f2;
                List<Subfield> sfs1 = (List<Subfield>)df1.getSubfields();
                List<Subfield> sfs2 = (List<Subfield>)df2.getSubfields();
                Iterator<Subfield> iter3 = sfs1.iterator();
                Iterator<Subfield> iter4 = sfs2.iterator();
                while (iter3.hasNext() && iter4.hasNext())
                {
                    Subfield sf1 = iter3.next();
                    Subfield sf2 = iter4.next();
                    if (! sf1.getData().equals(sf2.getData()))  
                        return(2);
                }
            }
            else 
                return(3);
        }
        // if done with one record but not the other
        if (iter1.hasNext() || iter2.hasNext())
            return(-1);

        return(0);
    }
}
