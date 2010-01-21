package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        CallNumberTests.class,
        CallNumLaneJacksonTests.class,
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
        CallNumLongestComnPfxTests.class,
        CallNumLoppingUnitTests.class,
        CallNumTopFacetTests.class,
        CallNumUtilsUnitTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemMissingTests.class,
        ItemObjectTests.class,
        ItemUtilsUnitTests.class
//        org.solrmarc.index.CallNumberUnitTests.class
        })

        
public class AllCallnumTests 
{
}
