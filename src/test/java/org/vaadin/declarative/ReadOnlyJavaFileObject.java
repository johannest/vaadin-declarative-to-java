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

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

class ReadOnlyJavaFileObject implements JavaFileObject {
    private final String classNameFqn;
    private final ByteArrayOutputStream baos;

    ReadOnlyJavaFileObject(String classNameFqn, ByteArrayOutputStream baos) {
        this.classNameFqn = classNameFqn;
        this.baos = baos;
    }

    private String simpleName() {
        int dot = classNameFqn.lastIndexOf('.');
        return dot >= 0 ? classNameFqn.substring(dot + 1) : classNameFqn;
    }

    private String path() {
        return classNameFqn.replace('.', '/') + Kind.SOURCE.extension; // org/vaadin/.../StorefrontViewDesign.java
    }


    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        return kind == Kind.SOURCE && simpleName().equals(simpleName);
    }

    @Override
    public NestingKind getNestingKind() {
        return NestingKind.TOP_LEVEL;
    }

    @Override
    public Modifier getAccessLevel() {
        return Modifier.PUBLIC;
    }

    @Override
    public URI toUri() {
        return URI.create("string:///" + path());
    }

    @Override
    public String getName() {
        return path();
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new IOException("Read-only");
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) {
        return new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), StandardCharsets.UTF_8);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public Writer openWriter() throws IOException {
        throw new IOException("Read-only");
    }

    @Override
    public long getLastModified() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean delete() {
        return false;
    }
}
