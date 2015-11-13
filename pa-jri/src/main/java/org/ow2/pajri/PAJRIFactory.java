package org.ow2.pajri;

import org.ow2.parengine.PAREngineFactory;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;

/**
 * R implementation of ScriptEngineFactory for the ProActive Scheduler Worker.
 *
 * @author Activeeon Team
 */
public final class PAJRIFactory extends PAREngineFactory {

    public static final String PARSCRIPT_NAME = "parscript";


    @Override
    public ScriptEngine getScriptEngine() {
        try {
            return PAJRIEngine.create(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the PAJRIEngine", e);
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<String>(super.ENGINE_NAMES);
        names.add(PARSCRIPT_NAME);
        return names;
    }


    @Override
    public List<String> getExtensions() {
        List<String> exts = new ArrayList<String>(super.R_FILE_EXTENSIONS);
        exts.add(PARSCRIPT_NAME);
        return exts;
    }

}
