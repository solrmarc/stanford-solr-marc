package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumUtilsUnitTests.class,
        CallNumberTests.class,
        CallNumLoppingUnitTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemUtilsUnitTests.class,
        org.solrmarc.index.CallNumberUnitTests.class
        })

        
public class AllCallnumTests 
{
}
