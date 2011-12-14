package com.jeffplaisance.serialization;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.Sets;
import com.jeffplaisance.serialization.ast.Include;
import com.jeffplaisance.serialization.ast.Module;
import com.jeffplaisance.serialization.ast.Parser;
import com.jeffplaisance.serialization.ast.Struct;
import com.jeffplaisance.serialization.ast.Union;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

/**
 * @author jplaisance
 */
public final class Compiler {

    public static void main(String[] args) throws CmdLineException, IOException {
        Config conf = new Config();
        CmdLineParser cmdLineParser = new CmdLineParser(conf);
        cmdLineParser.parseArgument(args);
        if (conf.includePaths == null) conf.includePaths = Lists.newArrayList();
        conf.includePaths.add(System.getenv("JEFF_SERIALIZATION_HOME")+"/include");
        Set<String> compiledFiles = Sets.newHashSet();
        for (String file : conf.files) {
            if (!compiledFiles.contains(file))
            compileFile(conf, file, compiledFiles);
        }
    }

    public static final class Config {
        @Option(name = "-f", usage="file name", required = true, multiValued = true)
        List<String> files;

        @Option(name = "-java_out", usage = "output dir for java generated files")
        String javaOut;

        @Option(name = "-java_pkg", usage = "package for generated java files")
        String javaPackage;

        @Option(name = "-I", usage = "include path", multiValued = true)
        List<String> includePaths;
    }

    public static void compileFile(
            Config conf,
            String fileName,
            Set<String> compiledFiles
    ) throws IOException {
        List<CodeGenerator> generators = Lists.newArrayList();
        if (conf.javaOut != null) {
            generators.add(new JavaGenerator(new File(conf.javaOut), new File(fileName).getName(), conf.javaPackage));
        }
        File file = new File(fileName);
        List list = parseFile(file);
        int i = 0;
        for (Object o : list) {
            if (o instanceof Include) {
                Include include = (Include)o;
                if (!compiledFiles.contains(include.getFileName().getValue())) {
                    compiledFiles.add(include.getFileName().getValue());
                    compileFile(conf, include.getFileName().getValue(), compiledFiles);
                }
                for (CodeGenerator generator : generators) {
                    generator.addInclude(include);
                }
                i++;
            } else {
                break;
            }
        }
        list = list.subList(i, list.size());
        if (file.exists()) {
            addDefinitions(conf, list, compiledFiles, generators);
        } else {
            String[] parts = fileName.split(File.separator);
            for (String path : conf.includePaths) {
                file = new File(path);
                for (String part : parts) {
                    file = new File(file, part);
                }
                if (file.exists()) {
                    addDefinitions(conf, list, compiledFiles, generators);
                    break;
                }
            }
            if (!file.exists()) throw new FileNotFoundException(fileName);
        }
        for (CodeGenerator generator : generators) {
            generator.close();
        }
    }

    public static void addDefinitions(
            Config conf,
            List list,
            Set<String> compiledFiles,
            List<CodeGenerator> generators
    ) throws IOException {
        for (Object o : list) {
            if (o instanceof Include) {
                throw new RuntimeException("includes must all be at the beginning of the file");
            } else if (o instanceof Module) {
                Module module = (Module)o;
                for (CodeGenerator generator : generators) {
                    generator.pushModule(module.getName().getValue());
                }
                addDefinitions(conf, module.getDefinitions(), compiledFiles, generators);
                for (CodeGenerator generator : generators) {
                    generator.popModule();
                }
            } else if (o instanceof Struct) {
                Struct struct = (Struct)o;
                for (CodeGenerator generator : generators) {
                    generator.addStruct(struct);
                }
            } else if (o instanceof Union) {
                Union union = (Union)o;
                for (CodeGenerator generator : generators) {
                    generator.addUnion(union);
                }
            }
        }
    }

    public static List parseFile(File file) throws FileNotFoundException {
        List ret = Lists.newArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
        final PeekingIterator<Token> tokens = Lexer.lex(reader);
        for (List list; (list = SExpressionParser.parse(tokens)) != null;) {
            ret.add(Parser.parse(list));
        }
        return ret;
    }
}
