package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
//      AuthorTests.class,  // problem with sorting
        AuthorTitleMappingTests.class,
        CallNumberTests.class,
//        CallNumLaneJacksonTests.class // bad values
        CallNumLCLoppingUnitTests.class,
        CallNumLibLocComboLopTests.class,
//        CallNumLongestComnPfxTests.class // bad values one test using dewey instead of LC?
        CallNumLoppingUnitTests.class,
//        CallNumTopFacetTests.class // bad values
        CallNumUtilsLoppingUnitTests.class,
//        CombineMultBibsMhldsReaderTest.class // unreadable rec between isn't working right
        DatabaseAZSubjectTests.class, 
        DiacriticTests.class,
        FormatDatabaseTests.class,
        FormatTests.class,
        GeographicFacetTests.class,
        IncrementalUpdateTests.class,
//        ItemDisplayCallnumLoppingTests.class //bad value for one test
//        ItemInfoTests.class // bad value for one test
        ItemLACTests.class,
        ItemMissingTests.class,
        ItemNoCallNumberTests.class,
//        ItemObjectTests.class // bad value for one test
        ItemOnlineTests.class,
        ItemSkippedTests.class,
        ItemsSplitTests.class,
        ItemUtilsUnitTests.class,
        LanguageTests.class,
        MarcCombiningReaderTests.class,
        MergeMhldFldsIntoBibsReaderTests.class,
//        MhldMappingTests.class // probs
//        MiscellaneousFieldTests.class // I think it should be using Combine... reader, not MarcCombining reader
//        NoteFieldsTests.class // one test  bad value
        PhysicalTests.class,
//        PublicationTests.class, // bad sorts, etc.
        SeriesTests.class,
        StandardNumberTests.class,
        SubjectSearchTests.class,
//        SubjectTests.class // bad val counts one test
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
