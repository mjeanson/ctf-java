/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * The class <code>StructDefinitionTest</code> contains tests for the class
 * <code>{@link StructDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
public class StructDefinitionTest {

    private static final @NotNull String TEST_STRUCT_ID = "testStruct";
    private static final @NotNull String ENUM_2 = "y";
    private static final @NotNull String ENUM_1 = "x";
    private static final @NotNull String TAG_ID = "Tag";
    private static final @NotNull String INT_ID = "_id";
    private static final @NotNull String STRING_ID = "_args";
    private static final @NotNull String ENUM_ID = "_enumArgs";
    private static final @NotNull String SEQUENCE_ID = "_seq";
    private static final @NotNull String LENGTH_SEQ = "_len";
    private static final @NotNull String VAR_FIELD_NAME = "SomeVariant";

    private StructDefinition fixture;
    private StructDefinition emptyStruct;
    private StructDefinition simpleStruct;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             won't happen
     */
    @Before
    public void setUp() throws CTFException {
        StructDeclaration sDec = new StructDeclaration(12);
        IntegerDeclaration id = IntegerDeclaration.INT_32B_DECL;
        IntegerDeclaration lenDec = IntegerDeclaration.UINT_8_DECL;
        StringDeclaration sd = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        EnumDeclaration ed = new EnumDeclaration(id);
        SequenceDeclaration seqDec = new SequenceDeclaration(LENGTH_SEQ, id);
        VariantDeclaration varDec = new VariantDeclaration();
        EnumDeclaration tagDec = new EnumDeclaration(id);
        tagDec.add(0, 1, ENUM_1);
        tagDec.add(2, 3, ENUM_2);
        varDec.addField(ENUM_2, id);
        varDec.addField(ENUM_1, sd);
        varDec.setTag(TAG_ID);
        sDec.addField(INT_ID, id);
        sDec.addField(STRING_ID, sd);
        sDec.addField(ENUM_ID, ed);
        sDec.addField(TAG_ID, tagDec);
        sDec.addField(LENGTH_SEQ, lenDec);
        sDec.addField(SEQUENCE_ID, seqDec);
        sDec.addField(VAR_FIELD_NAME, varDec);
        byte bytes[] = new byte[100];
        bytes[4] = 1;
        bytes[8] = 2;
        bytes[13] = 3;
        BitBuffer bb = new BitBuffer(Util.testMemory(ByteBuffer.wrap(bytes)));
        fixture = sDec.createDefinition(null, TEST_STRUCT_ID, bb);
        EnumDefinition eDef = tagDec.createDefinition(fixture, TAG_ID, bb);
        assertNotNull(eDef);
        VariantDefinition vd = varDec.createDefinition(fixture, VAR_FIELD_NAME, bb);
        assertNotNull(vd);
        // Create an empty struct
        StructDeclaration esDec = new StructDeclaration(32);
        emptyStruct = esDec.createDefinition(null, TEST_STRUCT_ID, bb);

        // Create a simple struct with two items
        StructDeclaration ssDec = new StructDeclaration(32);
        ssDec.addField(INT_ID, id);
        ssDec.addField(STRING_ID, sd);
        simpleStruct = ssDec.createDefinition(null, TEST_STRUCT_ID, bb);
    }

    /**
     * Run the StructDeclaration getDeclaration() method test.
     */
    @Test
    public void testGetDeclaration() {
        StructDeclaration result = fixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the HashMap<String, Definition> getDefinitions() method test.
     */
    @Test
    public void testGetDefinitions_1() {
        IDefinition result = fixture.getDefinition("_id");
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition lookupArray(String) method test.
     */
    @Test
    public void testLookupArray() {
        String name = INT_ID;
        AbstractArrayDefinition result = fixture.lookupArrayDefinition(name);
        assertNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    public void testLookupDefinition() {
        String lookupPath = "args";
        IDefinition result = fixture.lookupDefinition(lookupPath);

        assertNotNull(result);
    }

    /**
     * Run the EnumDefinition lookupEnum(String) method test.
     */
    @Test
    public void testLookupEnum() {
        String name = ENUM_ID;
        EnumDefinition result = fixture.lookupEnum(name);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_1() {
        String name = "_id";
        IntegerDefinition result = fixture.lookupInteger(name);
        assertNotNull(result);
    }

    /**
     * Run the IntegerDefinition lookupInteger(String) method test.
     */
    @Test
    public void testLookupInteger_2() {
        String name = VAR_FIELD_NAME;
        IntegerDefinition result = fixture.lookupInteger(name);
        assertNull(result);
    }

    /**
     * Run the SequenceDefinition lookupSequence(String) method test.
     */
    @Test
    public void testLookupFixedStringDefinition() {
        String name = SEQUENCE_ID;
        AbstractArrayDefinition result = fixture.lookupArrayDefinition(name);
        assertNotNull(result);
    }

    /**
     * Run the StringDefinition lookupString(String) method test.
     */
    @Test
    public void testLookupString() {
        String name = VAR_FIELD_NAME;
        StringDefinition result = fixture.lookupString(name);

        assertNull(result);
    }

    /**
     * Run the StructDefinition lookupStruct(String) method test.
     */
    @Test
    public void testLookupStruct() {
        String name = VAR_FIELD_NAME;
        StructDefinition result = fixture.lookupStruct(name);

        assertNull(result);
    }

    /**
     * Run the VariantDefinition lookupVariant(String) method test.
     */
    @Test
    public void testLookupVariant() {
        String name = VAR_FIELD_NAME;
        VariantDefinition result = fixture.lookupVariant(name);

        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    public void testToString() {
        String result = fixture.toString();
        assertNotNull(result);

        result = emptyStruct.toString();
        assertEquals("{  }", result);

        result = simpleStruct.toString();
        assertEquals("{ _id = 0, _args = \"\" }", result);
    }
}