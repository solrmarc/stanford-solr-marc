package org.solrmarc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.solrmarc.marc.*;
import org.solrmarc.tools.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumberUnitTests.class,
        CombineMultBibsMhldsReaderTest.class,
        CommandLineUtilTests.class,
        GetFormatMixinTest.class,
        HathiJsonReaderTest.class,
//        IndexSmokeTest.class, // duplicated in RemoteServerTest
        MergeSummaryHoldingsTests.class,
        MarcCombiningReaderTests.class,
        RecordReaderTest.class,
        RemoteServerTest.class,
        SolrUtilTests.class,
        SolrUpdateTest.class,
        StringNaturalCompareTest.class,
        UtilUnitTests.class
        })

public class AllTests
{
}
