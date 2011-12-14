package com.jeffplaisance.serialization;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.jeffplaisance.serialization.ast.Field;
import com.jeffplaisance.serialization.ast.Include;
import com.jeffplaisance.serialization.ast.Struct;
import com.jeffplaisance.serialization.ast.Type;
import com.jeffplaisance.serialization.ast.Union;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author jplaisance
 */
public final class JavaGenerator implements CodeGenerator {

    private final File outputDir;

    private final String packageName;
    
    private final List<Include> includes = Lists.newArrayList();
    
    private final Deque<Module> moduleStack = new ArrayDeque<Module>();
    
    private Module root;

    public JavaGenerator(File outputDir, String fileName, String packageName) {
        this.packageName = packageName+"."+fileName;
        File temp = outputDir;
        String[] parts = packageName.split("\\.");
        for (String part : parts) {
            temp = new File(temp, part);
        }
        temp = new File(temp, fileName);
        this.outputDir = temp;
        this.outputDir.mkdirs();
        root = new Module(fileName);
        moduleStack.addLast(root);
    }

    public void pushModule(final String moduleName) {
        Module newModule = new Module(moduleName);
        moduleStack.getLast().subModules.add(newModule);
        moduleStack.addLast(newModule);
    }

    public void popModule() {
        moduleStack.removeLast();
    }

    public void addStruct(final Struct struct) {
        moduleStack.getLast().structs.add(struct);
    }

    public void addUnion(final Union union) {
        moduleStack.getLast().unions.add(union);
    }

    public void addInclude(final Include include) {
        includes.add(include);
    }

    public void close() throws IOException {
        for (Struct struct : root.structs) {
            PrintWriter out = open(struct.getType().getName().getValue());
            printFileHeader(out, packageName, includes);
            printStruct(out, 0, struct);
            out.close();
        }
        for (Union union : root.unions) {
            PrintWriter out = open(union.getType().getName().getValue());
            printFileHeader(out, packageName, includes);
            printUnion(out, 0, union);
            out.close();
        }
        for (Module module : root.subModules) {
            PrintWriter out = open(module.name);
            printFileHeader(out, packageName, includes);
            printModule(out, 0, module);
            out.close();
        }
    }

    private static class Module {
        public final String name;
        public final List<Module> subModules = Lists.newArrayList();
        public final List<Struct> structs = Lists.newArrayList();
        public final List<Union> unions = Lists.newArrayList();

        public Module(final String name) {
            this.name = name;
        }
    }
        
    public static void printFileHeader(PrintWriter out, String packageName, List<Include> includes) {
        out.println("package "+packageName+";");
        out.println();
        for (Include include : includes) {
            out.println("import "+packageName+"."+include.getFileName().getValue().replaceAll(File.separator, ".")+";");
        }
        out.println();
    }
    
    public static String buildTypeString(Type type) {
        StringBuilder builder = new StringBuilder(type.getName().getValue());
        if (type.getTypeParameters().size() > 0) {
            builder.append('<');
            for (Type parameter : type.getTypeParameters()) {
                builder.append(buildTypeString(parameter));
                builder.append(",");
            }
            builder.setCharAt(builder.length()-1, '>');
        }
        return builder.toString();
    }

    public static void printStruct(PrintWriter out, int indentation, Struct struct) {
        String type = buildTypeString(struct.getType());
        println(out, indentation, "public"+(indentation == 0 ? "" : " static")+" final class " + type + " {");
        for (Field field : struct.getFields()) {
            println(out, indentation+1, "public "+buildTypeString(field.getValueType())+" "+field.getName().getValue()+";");
        }
        println(out, indentation, "}");
    }

    public static void printUnion(PrintWriter out, int indentation, Union union) {
        println(out, indentation, "public"+(indentation == 0 ? "" : " static")+" final class " + union.getType().getName().getValue() + " {");
        for (Field field : union.getFields()) {
            println(out, indentation+1, "public static final int "+field.getName().getValue()+" = "+field.getTag()+";");
        }
        out.println();
        println(out, indentation+1, "public int tag;");
        println(out, indentation+1, "public Object value;");
        println(out, indentation, "}");
    }
    
    public static void printModule(PrintWriter out, int indentation, Module module) {
        println(out, indentation, "public"+(indentation == 0 ? "" : " static")+" final class " + module.name + " {");
        for (Struct struct : module.structs) {
            printStruct(out, indentation+1, struct);
        }
        for (Union union : module.unions) {
            printUnion(out, indentation + 1, union);
        }
        for (Module subModule : module.subModules) {
            printModule(out, indentation+1, subModule);
        }
        println(out, indentation, "}");
    }

    public static void println(PrintWriter out, int indentation, String str) {
        for (int i = 0; i < indentation; i++) {
            out.print("    ");
        }
        out.println(str);
    }
    
    private PrintWriter open(String str) throws FileNotFoundException {
        return new PrintWriter(
                new OutputStreamWriter(
                        new BufferedOutputStream(
                                new FileOutputStream(
                                        new File(outputDir, str+".java")
                                )
                        ),
                        Charsets.UTF_8
                )
        );
    }
}
