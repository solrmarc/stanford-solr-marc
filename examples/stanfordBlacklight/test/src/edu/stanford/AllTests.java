package edu.stanford;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    	AccessTests.class,
        AuthorTests.class,
        CallNumberTests.class,
        CallNumUtilsUnitTests.class,
        CallNumLoppingUnitTests.class,
        CallNumTopFacetTests.class,
        DiacriticTests.class,
        FormatTests.class,
        ItemDisplayCallnumLoppingTests.class,
        ItemInfoTests.class,
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
