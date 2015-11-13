package org.ow2.pajri.tests;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.ow2.pajri.PAJRIFactory;

import javax.script.ScriptEngineManager;

import static org.junit.Assert.assertNotNull;


public class PAJRIFactoryTest {

    @Test
    public void findEngineByExtensions() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        assertNotNull(new ScriptEngineManager().getEngineByExtension("r"));
        assertNotNull(new ScriptEngineManager().getEngineByExtension("R"));
        assertNotNull(new ScriptEngineManager().getEngineByExtension(PAJRIFactory.PARSCRIPT_NAME));
    }

    @Test
    public void findByName() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        assertNotNull(new ScriptEngineManager().getEngineByName("R"));
        assertNotNull(new ScriptEngineManager().getEngineByName("r"));
        assertNotNull(new ScriptEngineManager().getEngineByName(PAJRIFactory.PARSCRIPT_NAME));
    }
}