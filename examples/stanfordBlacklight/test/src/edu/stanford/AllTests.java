package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberTests.class,
        CallNumLaneJacksonTests.class,
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
        CallNumLongestComnPfxTests.class,
        CallNumLoppingUnitTests.class,
        CallNumTopFacetTests.class,
        CallNumUtilsUnitTests.class,
        DiacriticTests.class,
        FormatTests.class,
        IncrementalUpdateTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemObjectTests.class,
        ItemSkippedTests.class,
        ItemUtilsUnitTests.class,
        LanguageTests.class,
        MiscellaneousFieldTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        StandardNumberTests.class,
        SubjectTests.class,
        TableOfContentsTests.class,
        TitleSearchTests.class,
        TitleSearchVernTests.class,
        TitleTests.class,
        UrlTests.class,
        VernFieldsTests.class
        })

        
public class AllTests 
{
}
