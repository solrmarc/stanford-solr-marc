package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        AuthorTitleMappingTests.class,
        CallNumberTests.class,
        CallNumLaneJacksonTests.class,
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
        CallNumLongestComnPfxTests.class,
        CallNumLoppingUnitTests.class,
        CallNumTopFacetTests.class,
        CallNumUtilsLoppingUnitTests.class,
/*  not yet vetted  2010-10-30		
        DatabaseAZSubjectTests.class,
*/
        DiacriticTests.class,
        FormatTests.class,
/*  not yet vetted  2010-10-30		
        FormatDatabaseTests.class,
*/
        IncrementalUpdateTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
        ItemLACTests.class,
        ItemMissingTests.class,
        ItemNoCallNumberTests.class,
        ItemObjectTests.class,
        ItemOnlineTests.class,
        ItemSkippedTests.class,
        ItemsSplitTests.class,
        ItemUtilsUnitTests.class,
        LanguageTests.class,
        MiscellaneousFieldTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        SeriesTests.class,
        StandardNumberTests.class,
        SubjectSearchTests.class,
        SubjectTests.class,
        TableOfContentsTests.class,
        TitleSearchTests.class,
        TitleSearchVernTests.class,
        TitleTests.class,
        UrlTests.class,
        VernFieldsTests.class,
        WordDelimiterTests.class
        })

        
public class AllTests 
{
}
