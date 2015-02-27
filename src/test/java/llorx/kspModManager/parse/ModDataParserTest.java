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
public class ModDataParserTest {
    
    public ModDataParserTest() {
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
     * Test of parseModData method, of class ModDataParser.
     */
    @Test
    public void testParseModData() {
        System.out.println("parseModData");
        Mod mod = null;
        Connection.Response res = null;
        ModDataParser.parseModData(mod, res);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDownloadLink method, of class ModDataParser.
     */
    @Test
    public void testGetDownloadLink() {
        System.out.println("getDownloadLink");
        Mod mod = null;
        String expResult = "";
        String result = ModDataParser.getDownloadLink(mod);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
