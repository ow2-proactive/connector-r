package org.ow2.parscript;

import javax.script.ScriptEngineManager;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;


public class PARScriptFactoryTest {

    @Test
    public void findEngineByExtensions() throws Exception {
        assertNotNull(new ScriptEngineManager().getEngineByExtension("r"));
        assertNotNull(new ScriptEngineManager().getEngineByExtension("R"));
    }

    @Test
    public void findByName() throws Exception {
        assertNotNull(new ScriptEngineManager().getEngineByName("R"));
        assertNotNull(new ScriptEngineManager().getEngineByName("r"));
    }
}