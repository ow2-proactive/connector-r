package org.ow2.parserve.tests;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.ow2.parserve.PARServeFactory;

import javax.script.ScriptEngineManager;

import static org.junit.Assert.assertNotNull;


public class PARServeFactoryTest {

    @Test
    public void findEngineByExtensions() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        assertNotNull(new ScriptEngineManager().getEngineByExtension("r"));
        assertNotNull(new ScriptEngineManager().getEngineByExtension("R"));
        assertNotNull(new ScriptEngineManager().getEngineByExtension(PARServeFactory.PARSERVE_NAME));
    }

    @Test
    public void findByName() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        assertNotNull(new ScriptEngineManager().getEngineByName("R"));
        assertNotNull(new ScriptEngineManager().getEngineByName("r"));
        assertNotNull(new ScriptEngineManager().getEngineByName(PARServeFactory.PARSERVE_NAME));
    }
}