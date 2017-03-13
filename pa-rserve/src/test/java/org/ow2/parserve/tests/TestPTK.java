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

import java.io.Serializable;

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.ow2.parserve.PARServeEngine;
import org.ow2.parserve.PARServeFactory;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * Tests that the ProcessTreeKiller cookie appears in the RServe sessions.
 *
 * @author Activeeon Team
 */
public class TestPTK {

    private final String COOKIE_NAME = "PROCESS_KILLER_COOKIE" + PARServeEngine.COOKIE_NAME_SUFFIX;

    @Test
    public void test() throws Exception {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        String rScript = "result = Sys.getenv('" + COOKIE_NAME + "')";

        SimpleScript ss = new SimpleScript(rScript, PARServeFactory.ENGINE_NAME);
        TaskScript taskScript = new TaskScript(ss);
        ScriptResult<Serializable> res = taskScript.execute();
        System.out.println("Result : " + res.getResult());
        Assert.assertNotNull("The R session must have env variable cookie set", res.getResult());
    }
}
