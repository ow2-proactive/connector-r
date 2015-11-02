/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ow2.parengine.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author ActiveEon Team
 */
public class RLibPathConfigurator {

    public static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    public static final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");

    /**
     * Tries to locate from registry or usual paths then adds rhome to the
     * library path
     */
    public static void configureLibraryPath() {
        String rHome = System.getenv("R_HOME");
        boolean isBlank = StringUtils.isBlank(rHome);
        if (isWindows) {
            // On Windows try to locate from Windows Registry
            if (isBlank) {
                try {
                    rHome = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, // HKEY
                            "SOFTWARE\\R-core\\R", // Key
                            "InstallPath");
                    if (!(new File(rHome).exists())) {
                        throw new FileNotFoundException("The path " + rHome
                                + " given by the Windows Registry key does not exists");
                    }

                    RLibPathConfigurator.setEnvVar("R_HOME", rHome);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Unable to locate R homedir from Windows Registry, it seems R is not installed, please define the R_HOME env variable",
                            e);
                }
            }
            dynamicAddLibraryPathWindows(rHome);
        } else if (isMac) {
            // On Mac try to locate from usual install path
            if (isBlank) {
                rHome = "/Library/Frameworks/R.framework/Resources";
                try {
                    if (!(new File(rHome).exists())) {
                        throw new FileNotFoundException("The usual " + rHome + " path does not exists");
                    }
                    RLibPathConfigurator.setEnvVar("R_HOME", rHome);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Unable to locate R homedir, the R_HOME env variable must be defined", e);
                }
            }
            dynamicAddLibraryPathMac(rHome);
        } else {
            // On Linux try to locate from usual install path
            if (isBlank) {
                rHome = "/usr/lib/R";
                try {
                    if (!(new File(rHome).exists())) {
                        throw new FileNotFoundException("The usual " + rHome + " path does not exists");
                    }
                    RLibPathConfigurator.setEnvVar("R_HOME", rHome);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            "Unable to locate R homedir, the R_HOME env variable must be defined", e);
                }
            }
            dynamicAddLibraryPathLinux(rHome);
        }
    }

    private static void dynamicAddLibraryPathWindows(final String rHome) {
        String fs = java.io.File.separator;
        // Get the architecture of the jvm not the os
        String sunArchDataModel = System.getProperty("sun.arch.data.model");
        String rLibraryPath = rHome + fs + "bin" + fs;
        String packagesLibraryPath = rHome + fs + "library";
        // If R_LIBS env var is defined locate rJava there
        String rLibs = System.getenv("R_LIBS");
        if (!StringUtils.isBlank(rLibs)) {
            packagesLibraryPath = rLibs;
        }
        String rJavaPath = packagesLibraryPath + fs + "rJava";
        if (!new File(rJavaPath).exists()) {
            throw new IllegalStateException("Unable to locate rJava in " + rJavaPath
                    + " the R_LIBS env variable must be defined");
        }
        String jriLibraryPath = rJavaPath + fs + "jri" + fs;
        // Use correct libraries depending on jvm architecture
        if ("32".equals(sunArchDataModel)) {
            rLibraryPath += "i386";
            jriLibraryPath += "i386";
        } else if ("64".equals(sunArchDataModel)) {
            rLibraryPath += "x64";
            jriLibraryPath += "x64";
        }
        // Dynamically add to java library path
        try {
            RLibPathConfigurator.addLibraryPath(jriLibraryPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add jri to library path " + jriLibraryPath, e);
        }
        // Update the current process 'Path' environment variable
        try {
            String varValue = System.getenv("Path");
            RLibPathConfigurator.setEnvVar("Path", varValue + File.pathSeparator + rLibraryPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add R lib to Path environment variable "
                    + rLibraryPath, e);
        }
    }

    private static void dynamicAddLibraryPathMac(final String rHome) {
        String fs = java.io.File.separator;
        String packagesLibraryPath = rHome + fs + "library";
        // If R_LIBS env var is defined locate rJava there
        String rLibs = System.getenv("R_LIBS");
        if (!StringUtils.isBlank(rLibs)) {
            if (new File(rLibs + fs + "rJava").exists()) {
                packagesLibraryPath = rLibs;
            }
        }
        String rJavaPath = packagesLibraryPath + fs + "rJava";
        if (!new File(rJavaPath).exists()) {
            throw new IllegalStateException("Unable to locate rJava package in " + rJavaPath
                    + " the R_LIBS env variable must be defined");
        }
        // Dynamically add to java library path
        String jriLibraryPath = rJavaPath + fs + "jri" + fs;
        try {
            RLibPathConfigurator.addLibraryPath(jriLibraryPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add jri to library path " + jriLibraryPath, e);
        }
    }

    private static void dynamicAddLibraryPathLinux(final String rHome) {
        String fs = java.io.File.separator;
        String packagesLibraryPath = rHome + fs + "site-library";
        // If R_LIBS env var is defined locate rJava there
        String rLibs = System.getenv("R_LIBS");
        if (!StringUtils.isBlank(rLibs)) {
            if (new File(rLibs + fs + "rJava").exists()) {
                packagesLibraryPath = rLibs;
            }
        }
        String rJavaPath = packagesLibraryPath + fs + "rJava";
        if (!new File(rJavaPath).exists()) {
            throw new IllegalStateException("Unable to locate rJava package in " + rJavaPath
                    + " the R_LIBS env variable must be defined");
        }
        // Dynamically add to java library path
        String jriLibraryPath = rJavaPath + fs + "jri" + fs;
        try {
            RLibPathConfigurator.addLibraryPath(jriLibraryPath);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to add jri to library path " + jriLibraryPath, e);
        }
    }

    /**
     * Adds the specified path to the java library path
     *
     * @param pathToAdd the path to add
     * @throws Exception
     */
    public static void addLibraryPath(String pathToAdd) throws Exception {
        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        // get array of paths
        final String[] paths = (String[]) usrPathsField.get(null);

        // check if the path to add is already present
        for (String path : paths) {
            if (path.equals(pathToAdd)) {
                return;
            }
        }

        // add the new path
        final String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
        newPaths[newPaths.length - 1] = pathToAdd;
        usrPathsField.set(null, newPaths);
    }

    private static void setEnvVar(String var, String value) {
        Class<?> clazz;
        try {
            clazz = Class.forName("org.ow2.proactive.utils.Environment");
        } catch (Exception e) {
            // This can occur for scheduler > 3.4.0
            try {
                clazz = Class.forName("org.ow2.proactive.scheduler.util.process.Environment");
            } catch (Exception ee) {
                throw new IllegalStateException(
                        "Unable to load Environement class required to set the env variable " + var, e);
            }
        }
        try {
            Method method = clazz.getMethod("setenv", String.class, String.class, Boolean.TYPE);
            method.invoke(null, var, value, true);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set the env variable " + var, e);
        }
    }

}
