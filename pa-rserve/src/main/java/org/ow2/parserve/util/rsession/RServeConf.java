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
package org.ow2.parserve.util.rsession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;


/**
 * Holds all configuration info to start or connect to a RServe process
 *
 * @author Activeeon Team
 */
public class RServeConf {
    public final static String RURL_START = "R://";

    public final static String DEFAULT_RSERVE_HOST = "localhost";

    public final static int RSERVER_DEFAULT_PORT = 6311;

    public final static long DEFAULT_TIMEOUT = 120000;

    public final static File DEFAULT_CONFIG_FILEPATH = new File(System.getProperty("java.io.tmpdir"), "Rserv.conf");

    public String host = DEFAULT_RSERVE_HOST;

    public int port = RSERVER_DEFAULT_PORT;

    public String login;

    public String password;

    public boolean daemon;

    public boolean debug;

    public long timeout = DEFAULT_TIMEOUT;

    public Properties additionalConf = new Properties();

    public Properties localRProperties = new Properties();

    public RServeConf(String host, int port, String login, String password, long timeout, boolean daemon, boolean debug,
            Properties additionalConf, Properties localRProperties) {
        this.host = host;
        this.port = port;
        this.login = login;
        this.password = password;
        if (timeout > 0) {
            this.timeout = timeout;
        }
        this.daemon = daemon;
        this.debug = debug;
        if (additionalConf != null) {
            this.additionalConf = additionalConf;
        }
        if (localRProperties != null) {
            this.localRProperties = localRProperties;
        }
    }

    public static RServeConf parse(String RURL) {
        String login = null;
        String passwd = null;
        String host = null;
        int port = -1;
        try {
            String hostport = null;
            if (RURL.contains("@")) {
                String loginpasswd = RURL.split("@")[0].substring((RURL_START).length());
                login = loginpasswd.split(":")[0];
                if (login.equals("user.name")) {
                    login = System.getProperty("user.name");
                }
                passwd = loginpasswd.split(":")[1];
                hostport = RURL.split("@")[1];
            } else {
                hostport = RURL.substring((RURL_START).length());
            }

            if (hostport.contains(":")) {
                host = hostport.split(":")[0];
                port = Integer.parseInt(hostport.split(":")[1]);
            } else {
                host = hostport;
            }

            return new RServeConf(host, port, login, passwd, DEFAULT_TIMEOUT, false, false, null, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Impossible to parse " + RURL + ":\n  host=" + host + "\n  port=" +
                                               port + "\n  login=" + login + "\n  password=" + passwd);
        }

    }

    public boolean isLocal() {
        try {
            return host == null || InetAddress.getLocalHost().getHostName() == host ||
                   InetAddress.getByName(host).isLoopbackAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return RURL_START + (login != null ? (login + ":" + password + "@") : "") +
               (host == null ? DEFAULT_RSERVE_HOST : host) +
               (port > 0 ? ":" + port
                         : "") /* + " http_proxy=" + http_proxy + " RLibPath=" + RLibPath */;
    }

    public File getConfFilePath() {
        return DEFAULT_CONFIG_FILEPATH;
    }

    public void writeConfToFile() throws IOException {
        FileWriter fw = new FileWriter(DEFAULT_CONFIG_FILEPATH);
        if (port > 0) {
            fw.write("port " + port + "\n");
        }
        if (additionalConf != null) {
            for (String key : additionalConf.stringPropertyNames()) {
                if (!(key.equals("daemon"))) {
                    fw.write(key + " " + additionalConf.get(key) + "\n");
                }
            }
        }
        fw.close();
    }
}
