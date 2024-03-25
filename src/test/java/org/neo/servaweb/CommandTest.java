package org.neo.servaframe;

import java.util.*;
import java.sql.SQLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.neo.servaframe.util.*;
import org.neo.servaframe.interfaces.*;
import org.neo.servaframe.model.*;

import org.neo.servaweb.*;
import org.neo.servaweb.ifc.*;
import org.neo.servaweb.impl.*;
import org.neo.servaweb.model.*;
import org.neo.servaweb.util.*;

/**
 * Unit test 
 */
public class CommandTest 
    extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CommandTest( String testName ) {
        super( testName );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Code to set up resources or initialize variables before each test method
    }

    @Override
    protected void tearDown() throws Exception {
        // Code to clean up resources after each test method
        super.tearDown();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( CommandTest.class );
    }

    public void testCommand() throws Exception {
        String command = "ls -l /tmp/";
        System.out.println("command = " + command);
        try {
            String result = CommonUtil.executeCommand(command);
            System.out.println("execute success, result = " + result);
        }
        catch(Exception ex) {
            System.out.println("execute fail, message = " + ex.getMessage());
        }
    }
}

