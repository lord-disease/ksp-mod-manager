/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package llorx.kspModManager.parse;

import llorx.kspModManager.mod.Mod;
import org.jsoup.Connection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author disease
 */
public class JenkinsDataParserTest {
    
    public JenkinsDataParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parseData method, of class JenkinsDataParser.
     */
    @Test
    public void testParseData() {
        System.out.println("parseData");
        Mod mod = null;
        Connection.Response res = null;
        JenkinsDataParser.parseData(mod, res);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
