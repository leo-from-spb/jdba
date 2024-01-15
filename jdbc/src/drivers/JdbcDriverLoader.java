package org.jetbrains.dekaf.jdbc.drivers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;



public class JdbcDriverLoader {

    public static @NotNull JdbcDriver loadDriver(@Nullable Path basePath,
                                                 @NotNull String @NotNull [] jarNames,
                                                 @Nullable String driverClassName) {
        URLClassLoader driverClassLoader = loadDriverJars(basePath, jarNames);
        try {
            Class loadedClass;
            if (driverClassName != null) {
                try {
                    loadedClass = driverClassLoader.loadClass(driverClassName);
                }
                catch (ClassNotFoundException cnf) {
                    throw new IllegalArgumentException("Class \""+driverClassName+"\" doesn't exist in the loaded jars", cnf);
                }
            }
            else {
                throw new RuntimeException("NIY");
            }
            if (!(java.sql.Driver.class.isAssignableFrom(loadedClass))) {
                String loadedClassName = loadedClass.getName();
                throw new IllegalStateException("The loaded class \""+loadedClassName+"\" is not a valid JDBC driver class");
            }
            return new JdbcDriver(loadedClass, driverClassLoader);
        }
        catch (RuntimeException e) {
            closeClassLoader(driverClassLoader);
            throw e;
        }
    }

    public static @NotNull URLClassLoader loadDriverJars(@Nullable Path basePath,
                                                         @NotNull String @NotNull [] jarNames) {
        Path[] jarFilePaths = adjustJarFilePaths(basePath, jarNames);
        int n = jarFilePaths.length;
        URLClassLoader classLoader;
        try {
            URL[] jarUrls = new URL[n];
            for (int i = 0; i < n; i++) jarUrls[i] = jarFilePaths[i].toUri().toURL();
            classLoader = new URLClassLoader("JdbcDrivers",
                                             jarUrls,
                                             JdbcDriverLoader.class.getClassLoader());
        }
        catch (MalformedURLException mue) {
            throw new RuntimeException("Cannot load JDBC driver: " + mue.getMessage(), mue);
        }
        return classLoader;
    }

    static @NotNull Path[] adjustJarFilePaths(@Nullable Path basePath, @NotNull String @NotNull [] jarNames) {
        int n = jarNames.length;
        final Path baseDir = basePath != null
                                 ? basePath.toAbsolutePath()
                                 : getCurrentDir();
        final Path[] jarFiles = new Path[n];
        for (int i = 0; i < n; i++) {
            String jarName = jarNames[i];
            Path jarFile = Path.of(jarName);
            if (!jarFile.isAbsolute()) {
                jarFile = baseDir.resolve(jarFile);
            }
            jarFiles[i] = jarFile;
        }
        return jarFiles;
    }

    public static @NotNull Path getCurrentDir() {
        Path dot = Path.of(".");
        return dot.toAbsolutePath();
    }

    private static void closeClassLoader(final URLClassLoader driverClassLoader) {
        try {
            driverClassLoader.close();
        }
        catch (IOException e) {
            System.err.println("PANIC: driverClassLoader.close() throws an unexpected exception: " + e.getMessage());
        }
    }

}
