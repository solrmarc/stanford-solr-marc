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
//works        CallNumLibLocComboLopTests.class,
//        CallNumLongestComnPfxTests.class // bad values one test using dewey instead of LC?
        CallNumLoppingUnitTests.class,
//        CallNumTopFacetTests.class // bad values
        CallNumUtilsLoppingUnitTests.class,
//        CombineMultBibsMhldsReaderTest.class // unreadable rec between isn't working right
//works        DatabaseAZSubjectTests.class, 
//works        DiacriticTests.class,
//works        FormatDatabaseTests.class,
//works        FormatTests.class,
//works        GeographicFacetTests.class,
//works        IncrementalUpdateTests.class,
//        ItemDisplayCallnumLoppingTests.class //bad value for one test
//        ItemInfoTests.class // bad value for one test
//works        ItemLACTests.class,
//works        ItemMissingTests.class,
//works        ItemNoCallNumberTests.class,
//        ItemObjectTests.class // bad value for one test
//works        ItemOnlineTests.class,
//works        ItemSkippedTests.class,
//works        ItemsSplitTests.class,
        ItemUtilsUnitTests.class,
//works        LanguageTests.class,
//works        MarcCombiningReaderTests.class,
        MergeMhldFldsIntoBibsReaderTests.class,
//        MhldMappingTests.class // probs
//        MiscellaneousFieldTests.class // I think it should be using Combine... reader, not MarcCombining reader
//        NoteFieldsTests.class // one test  bad value
//works        PhysicalTests.class,
//        PublicationTests.class, // bad sorts, etc.
//works        SeriesTests.class,
//works        StandardNumberTests.class,
        SubjectSearchTests.class
//        SubjectTests.class // bad val counts one test
//works        TitleSearchTests.class,
//works        TitleSearchVernTests.class,
//works        TitleTests.class,
//works        UrlTests.class,
//works        VernFieldsTests.class,
//works        WordDelimiterTests.class
        })

        
public class AllTests 
{
}
