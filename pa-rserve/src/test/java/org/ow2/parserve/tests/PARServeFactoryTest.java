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
package org.ow2.parserve.tests;

import static org.junit.Assert.assertNotNull;

import javax.script.ScriptEngineManager;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;
import org.ow2.parserve.PARServeFactory;


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
