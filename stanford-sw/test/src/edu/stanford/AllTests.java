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
        DatabaseAZSubjectTests.class,
        DiacriticTests.class,
        FormatDatabaseTests.class,
        FormatTests.class,
        GeographicFacetTests.class,
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
        MergeMhldFldsIntoBibsReaderTests.class,
        MhldMappingTests.class,
        MiscellaneousFieldTests.class,
        NoteFieldsTests.class,
        PhysicalTests.class,
        PublicationTests.class,
        SeriesTests.class,
        StandardNumberTests.class,
        SubjectSearchTests.class,
        SubjectTests.class,
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
