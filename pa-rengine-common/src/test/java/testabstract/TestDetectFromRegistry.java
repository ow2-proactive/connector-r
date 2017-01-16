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
package testabstract;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.BasicConfigurator;
import org.ow2.parengine.util.RLibPathConfigurator;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.scripting.SimpleScript;
import org.ow2.proactive.scripting.TaskScript;


/**
 * Tests R homedir detection from Windows registry.
 *
 * @author Activeeon Team
 */
public class TestDetectFromRegistry {
    public static String TAG = "test.detect.registry";

    public void test(String engineName) throws Exception {
        if (!RLibPathConfigurator.isWindows) {
            return;
        }

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();

        if (System.getProperty(TAG) == null) {
            // Spawn a new process without R_HOME in the env
            try {
                ProcessBuilder pb = new ProcessBuilder();
                pb.redirectErrorStream(true);
                pb.environment().remove("R_HOME");
                pb.directory(new File(System.getProperty("user.dir")));
                String fs = File.separator;
                String javaExe = System.getProperty("java.home") + fs + "bin" + fs +
                                 (RLibPathConfigurator.isWindows ? "java.exe" : "java");
                ArrayList<String> command = new ArrayList<String>();
                command.add(javaExe);
                command.add("-D" + TAG);
                command.add("-Djava.class.path=" + System.getProperty("java.class.path"));
                command.addAll(Arrays.asList(System.getProperty("sun.java.command").split(" ")));
                pb.command(command);

                Process p = pb.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("> " + line);
                }
                in.close();
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            org.junit.Assert.assertNull("There should be no R_HOME env var", System.getenv("R_HOME"));

            // We suppose the R_HOME is undefined
            String rScript = "result=version[['os']]";
            SimpleScript ss = new SimpleScript(rScript, engineName);
            TaskScript taskScript = new TaskScript(ss);
            ScriptResult<Serializable> res = taskScript.execute();

            org.junit.Assert.assertEquals("The detection of R homedir from Windows Registry is broken",
                                          "mingw32",
                                          res.getResult());
        }
    }
}
