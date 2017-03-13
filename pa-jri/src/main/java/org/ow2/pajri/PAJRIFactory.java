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
package org.ow2.pajri;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;

import org.ow2.parengine.PAREngineFactory;


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
