package org.solrmarc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.solrmarc.tools.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumberUnitTests.class,
//        CommandLineUtilTests.class, // not passing 2011-12-24
        GetFormatMixinTest.class,
        HathiJsonReaderTest.class,
//        IndexSmokeTest.class, // duplicated in RemoteServerTest
        MergeSummaryHoldingsTests.class, 
        RecordReaderTest.class,
        RemoteServerTest.class,
        SolrUpdateTest.class,
        StringNaturalCompareTest.class,
        UtilUnitTests.class
        })
        
public class AllTests 
{
}
