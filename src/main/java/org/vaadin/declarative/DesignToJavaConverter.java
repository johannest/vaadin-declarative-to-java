/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.declarative;

import com.sun.codemodel.*;
import com.vaadin.ui.declarative.Design;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

/**
 * @author https://github.com/elmot
 */
/*
 * TODO Grid special handling
 */
public class DesignToJavaConverter {


    public static void main(String[] args) throws Exception {
        if (args.length == 0 || args.length % 2 == 1) {
            printUsageAndExit();
        }
        String packageName = "";
        String className = "Example";
        String baseClassName = null;
        InputStream input = System.in;
        OutputStream output = System.out;

        for (int i = 0; i < args.length; i += 2) {
            switch (args[i]) {
                case "-p":
                    packageName = args[i + 1];
                    break;
                case "-c":
                    className = args[i + 1];
                    break;
                case "-b":
                    baseClassName = args[i + 1];
                    break;
                case "-s":
                    input = new BufferedInputStream(new FileInputStream(args[i + 1]));
                    break;
                case "-o":
                    output = new BufferedOutputStream(new FileOutputStream(args[i + 1]));
                    break;
                default:
                    printUsageAndExit();
            }
        }
        convertDeclarativeToJava(packageName, className, baseClassName, input, output);
    }

    private static void printUsageAndExit() {
        System.out.println("Usage:");
        System.out.println("    java " + DesignToJavaConverter.class.getName() + " <options>");
        System.out.println("Options:");
        System.out.println("    -p packageName");
        System.out.println("    -c className");
        System.out.println("    -b baseClassName");
        System.out.println("    -s sourceFilePath");
        System.exit(1);
    }

    public static void convertDeclarativeToJava(String packageName,
                                                String className,
                                                String baseClassName,
                                                InputStream input, OutputStream output) throws Exception {

        JCodeModel jCodeModel = new JCodeModel();
        JPackage jPackage = jCodeModel._package(packageName);
        JDefinedClass declarativeClass = jPackage._class(className);
        if (baseClassName != null) {
            declarativeClass._extends(jCodeModel.directClass(baseClassName));
        }
        JMethod init = declarativeClass.method(Modifier.PUBLIC + Modifier.STATIC, void.class, "init");
        SpyComponentFactory componentFactory = new SpyComponentFactory(jCodeModel, declarativeClass, init.body());
        Design.setComponentFactory(componentFactory);
        Design.read(preProcessInputFile(input));
        JCodeModel refactoredCodeModel = refactor(jCodeModel, componentFactory.getMemberFieldJVars());
        refactoredCodeModel.build(new MyCodeWriter(output));
    }

    /**
     * Converts _id="x" to data="x" to facilitate recognizing member field
     */
    private static InputStream preProcessInputFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder editedContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            String editedLine = line.replace(" _id=\"", " data=\"");
            editedContent.append(editedLine).append("\n");
        }
        return new ByteArrayInputStream(editedContent.toString().getBytes());
    }

    /**
     * Rebuild the code model but making named local variables as member fields
     */
    private static JCodeModel refactor(JCodeModel original, Set<JVar> memberFieldJVars) throws JClassAlreadyExistsException {
        JCodeModel copy = new JCodeModel();
        for (Iterator<JPackage> it = original.packages(); it.hasNext(); ) {
            JPackage pkg = it.next();
            JPackage newPkg = copy._package(pkg.name());
            for (Iterator<JDefinedClass> iter = pkg.classes(); iter.hasNext(); ) {
                JDefinedClass originalClass = iter.next();
                JDefinedClass newClass = newPkg._class(originalClass.name());
                newClass._extends(originalClass.superClass());
                // Copy fields
                for (JFieldVar field : originalClass.fields().values()) {
                    newClass.field(field.mods().getValue(), field.type(), field.name());
                }
                // Copy methods
                for (JMethod method : originalClass.methods()) {
                    JMethod newMethod = newClass.method(method.mods().getValue(), method.type(), method.name());
                    // Copy method parameters
                    for (JVar param : method.params()) {
                        newMethod.param(param.type(), param.name());
                    }
                    for (Object obj : method.body().getContents()) {
                        if (isNamedField(obj, memberFieldJVars)) {
                            JVar localVar = (JVar) obj;
                            newClass.field(JMod.PROTECTED, localVar.type(), localVar.name());
                            newMethod.body().assign(JExpr._this().ref(localVar.name()), JExpr._new(localVar.type()));
                        } else {
                            if (obj instanceof JVar) {
                                JVar originalJVar = (JVar) obj;
                                newMethod.body().decl(originalJVar.type(), originalJVar.name(), JExpr._new(originalJVar.type()));
                            }
                            if (obj instanceof JInvocation) {
                                JInvocation originalJInv = (JInvocation) obj;
                                newMethod.body().add(originalJInv);
                            }
                        }
                    }
                }
            }
        }
        return copy;
    }

    private static boolean isNamedField(Object obj, Set<JVar> memberFieldJVars) {
        return memberFieldJVars.contains(obj);
    }

    private static class MyCodeWriter extends CodeWriter {
        private final OutputStream output;

        public MyCodeWriter(OutputStream output) {
            this.output = output;
        }

        @Override
        public OutputStream openBinary(JPackage pkg, String fileName) throws IOException {
            return output;
        }

        @Override
        public void close() throws IOException {

        }
    }
}
