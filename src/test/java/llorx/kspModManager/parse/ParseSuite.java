/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager.parse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author disease
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({llorx.kspModManager.parse.CurseDataParserTest.class, llorx.kspModManager.parse.KerbalSpacePartsDataParserTest.class, llorx.kspModManager.parse.BitBucketDataParserTest.class, llorx.kspModManager.parse.SpaceportParserTest.class, llorx.kspModManager.parse.GitHubDataParserTest.class, llorx.kspModManager.parse.ModDataParserTest.class, llorx.kspModManager.parse.KspForumDataParserTest.class, llorx.kspModManager.parse.CurseForgeDataParserTest.class, llorx.kspModManager.parse.JenkinsDataParserTest.class})
public class ParseSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
