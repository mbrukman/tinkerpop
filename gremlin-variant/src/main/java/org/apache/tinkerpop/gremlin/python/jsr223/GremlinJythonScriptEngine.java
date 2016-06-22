/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tinkerpop.gremlin.python.jsr223;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.python.jsr223.PyScriptEngine;
import org.python.jsr223.PyScriptEngineFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.io.Reader;
import java.util.Arrays;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class GremlinJythonScriptEngine implements ScriptEngine {

    private final PyScriptEngine pyScriptEngine;

    public GremlinJythonScriptEngine() {
        this.pyScriptEngine = (PyScriptEngine) new PyScriptEngineFactory().getScriptEngine();
        try {
            // Groovy's AbstractImportCustomizer should pull from a common source
            for (final Class x : Arrays.asList(Graph.class, GraphTraversal.class, GraphTraversalSource.class)) {
                this.pyScriptEngine.eval("from " + x.getPackage().getName() + " import " + x.getSimpleName());
            }
        } catch (final ScriptException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }


    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return this.pyScriptEngine.eval(script, context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return this.pyScriptEngine.eval(reader, context);
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return this.pyScriptEngine.eval(script);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return this.pyScriptEngine.eval(reader);
    }

    @Override
    public Object eval(String script, Bindings n) throws ScriptException {
        return this.pyScriptEngine.eval(script, n);
    }

    @Override
    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return this.pyScriptEngine.eval(reader, n);
    }

    @Override
    public void put(String key, Object value) {
        this.pyScriptEngine.put(key, value);
    }

    @Override
    public Object get(String key) {
        return this.pyScriptEngine.get(key);
    }

    @Override
    public Bindings getBindings(int scope) {
        return this.pyScriptEngine.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        this.pyScriptEngine.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return this.pyScriptEngine.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return this.pyScriptEngine.getContext();
    }

    @Override
    public void setContext(ScriptContext context) {
        this.pyScriptEngine.setContext(context);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new GremlinJythonScriptEngineFactory();
    }
}