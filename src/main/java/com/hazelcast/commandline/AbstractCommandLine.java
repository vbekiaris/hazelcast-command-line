/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.commandline;

import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Abstract command line class.
 */
public abstract class AbstractCommandLine implements Runnable {
    /**
     * Working directory of the distribution
     */
    public static final String WORKING_DIRECTORY = System.getProperty("hazelcast.commandline.workingdirectory", "distro/src");
    /**
     * Name separator based on the running file system
     */
    public static final String NAME_SEPARATOR = FileSystems.getDefault().getSeparator();
    /**
     * FINE level logging properties file for java.util.logging
     */
    public static final String LOGGING_PROPERTIES_FINE_LEVEL = "/config/hazelcast-fine-level-logging.properties";
    /**
     * FINEST level logging properties file for java.util.logging
     */
    public static final String LOGGING_PROPERTIES_FINEST_LEVEL = "/config/hazelcast-finest-level-logging.properties";
    static final String CLASSPATH_SEPARATOR = ":";
    static final int MIN_JAVA_VERSION_FOR_MODULAR_OPTIONS = 9;

    protected final PrintStream out;
    protected final PrintStream err;
    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec spec;
    protected ProcessExecutor processExecutor;

    public AbstractCommandLine(PrintStream out, PrintStream err, ProcessExecutor processExecutor) {
        this.out = out;
        this.err = err;
        this.processExecutor = processExecutor;
    }

    protected void addLogging(List<String> args, boolean verbose, boolean finestVerbose) {
        if (verbose) {
            args.add("-Djava.util.logging.config.file=" + WORKING_DIRECTORY + LOGGING_PROPERTIES_FINE_LEVEL);
        }
        if (finestVerbose) {
            args.add("-Djava.util.logging.config.file=" + WORKING_DIRECTORY + LOGGING_PROPERTIES_FINEST_LEVEL);
        }
    }

    /**
     * {@code picocli.CommandLine.IParameterConsumer} implementation to handle Java options.
     * Please see the details <a href=https://github.com/remkop/picocli/issues/1125>here</a>.
     */
    public static class JavaOptionsConsumer implements CommandLine.IParameterConsumer {
        public void consumeParameters(Stack<String> args, CommandLine.Model.ArgSpec argSpec,
                                      CommandLine.Model.CommandSpec commandSpec) {
            if (args.isEmpty()) {
                throw new CommandLine.ParameterException(commandSpec.commandLine(),
                        "Error: option '-J', '--JAVA_OPTS' requires a parameter");
            }
            List<String> list = argSpec.getValue();
            if (list == null) {
                list = new ArrayList<>();
                argSpec.setValue(list);
            }
            String arg = args.pop();
            String[] splitArgs = arg.split(argSpec.splitRegex());
            Collections.addAll(list, splitArgs);
        }
    }
}
