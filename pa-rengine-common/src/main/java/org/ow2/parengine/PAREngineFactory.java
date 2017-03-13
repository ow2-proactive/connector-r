/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.parengine;

import static javax.script.ScriptEngine.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;


/**
 * R implementation of ScriptEngineFactory for the ProActive Scheduler Worker.
 *
 * @author Activeeon Team
 */
public abstract class PAREngineFactory implements ScriptEngineFactory {

    public static final String ENGINE_NAME = "R";

    protected static final String R_ENGINE_VERSION = "1";

    protected static final String R_LANGUAGE_NAME = "R";

    protected static final String R_LANGUAGE_VERSION = "3";

    protected static final List<String> R_FILE_EXTENSIONS = Arrays.asList("R", "r");

    protected static final List<String> ENGINE_NAMES = Arrays.asList("R", "r");

    protected static final List<String> R_MIME_TYPES = Collections.singletonList("text/x-R");

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public Object getParameter(String key) {
        if (key == null) {
            return null;
        }
        if (key.equals(NAME)) {
            return ENGINE_NAME;
        } else if (key.equals(ENGINE)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (key.equals(LANGUAGE)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else {
            return null;
        }
    }

    @Override
    public abstract ScriptEngine getScriptEngine();

    @Override
    public String getEngineVersion() {
        return R_ENGINE_VERSION;
    }

    @Override
    public List<String> getMimeTypes() {
        return R_MIME_TYPES;
    }

    @Override
    public String getLanguageName() {
        return R_LANGUAGE_NAME;
    }

    @Override
    public String getLanguageVersion() {
        return R_LANGUAGE_VERSION;
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuilder ret = new StringBuilder(m + "(" + obj);
        for (String arg : args) {
            ret.append(",").append(arg);
        }
        ret.append(");");
        return ret.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "cat('" + toDisplay + "');";
    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder ret = new StringBuilder();
        for (String str : statements) {
            ret.append(str).append(";\n");
        }
        return ret.toString();
    }
}
