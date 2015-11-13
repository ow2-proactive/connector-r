package org.ow2.parserve;

import org.ow2.parengine.PAREngineFactory;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;

/**
 * R implementation of ScriptEngineFactory for the ProActive Scheduler Worker.
 *
 * @author Activeeon Team
 */
public final class PARServeFactory extends PAREngineFactory {

    public static final String PARSERVE_NAME = "parserve";


    @Override
    public ScriptEngine getScriptEngine() {
        try {
            return PARServeEngine.create(this);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the PARServeEngine", e);
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<String>(super.ENGINE_NAMES);
        names.add(PARSERVE_NAME);
        return names;
    }


    @Override
    public List<String> getExtensions() {
        List<String> exts = new ArrayList<String>(super.R_FILE_EXTENSIONS);
        exts.add(PARSERVE_NAME);
        return exts;
    }
}
