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

package org.apache.tinkerpop.gremlin.java.translator.jython;

import org.apache.tinkerpop.gremlin.java.translator.PythonTranslator;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.python.jsr223.JythonScriptEngineSetup;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.util.function.Lambda;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PythonJythonTranslatorTest {

    static {
        JythonScriptEngineSetup.setup();
    }

    @Test
    public void shouldSupportStringSupplierLambdas() throws Exception {
        final GraphTraversalSource g = TinkerFactory.createModern().traversal().withTranslator(PythonJythonTranslator.of("g", true));
        GraphTraversal.Admin<Vertex, Integer> t = g.withSideEffect("lengthSum", 0).withSack(1)
                .V()
                .filter(Lambda.predicate("lambda : \"lambda x: x.get().label() == 'person'\""))
                .flatMap(Lambda.<Traverser<Vertex>, Iterator<Vertex>>function("lambda: 'lambda x: x.get().vertices(OUT)'"))
                .map(Lambda.<Traverser<Vertex>, Integer>function("lambda: \"lambda x: len(x.get().value('name'))\""))
                .sideEffect(Lambda.consumer("lambda: \"lambda x : x.sideEffects('lengthSum', x.sideEffects('lengthSum') + x.get())\""))
                .order().by(Lambda.comparator("lambda: 'lambda a,b : 1 if a > b else 0 if a == b else -1'"))
                .sack(Lambda.biFunction("lambda: 'lambda a,b : a + b'"))
                .asAdmin();
        final List<Integer> sacks = new ArrayList<>();
        final List<Integer> lengths = new ArrayList<>();
        while (t.hasNext()) {
            final Traverser.Admin<Integer> traverser = t.getEndStep().next();
            sacks.add(traverser.sack());
            lengths.add(traverser.get());
        }
        assertFalse(t.hasNext());
        //
        assertEquals(6, lengths.size());
        assertEquals(3, lengths.get(0).intValue());
        assertEquals(3, lengths.get(1).intValue());
        assertEquals(3, lengths.get(2).intValue());
        assertEquals(4, lengths.get(3).intValue());
        assertEquals(5, lengths.get(4).intValue());
        assertEquals(6, lengths.get(5).intValue());
        ///
        assertEquals(6, sacks.size());
        assertEquals(4, sacks.get(0).intValue());
        assertEquals(4, sacks.get(1).intValue());
        assertEquals(4, sacks.get(2).intValue());
        assertEquals(5, sacks.get(3).intValue());
        assertEquals(6, sacks.get(4).intValue());
        assertEquals(7, sacks.get(5).intValue());
        //
        assertEquals(24, t.getSideEffects().<Number>get("lengthSum").intValue());
    }

    @Test
    public void shouldHaveValidToString() {
        Assert.assertEquals("translator[h:jython]", PythonTranslator.of("h").toString());
        assertEquals("translator[h:gremlin-jython]", PythonJythonTranslator.of("h").toString());
    }

}