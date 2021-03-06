/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.enumeration;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.ctf.core.event.types.EnumDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;

import java.util.List;

/**
 * Body parser for an enumeration, this parses the list of elements in an enum
 *
 * @author Matthew Khouzam
 *
 */
public final class EnumBodyParser implements ICommonTreeParser {

    /**
     * Enum declaration parameter object
     *
     * @author Matthew Khouzam
     *
     */
    public static final class Param implements ICommonTreeParserParameter {

        private final EnumDeclaration fEnumDeclaration;

        /**
         * Constructor
         *
         * @param enumDeclaration
         *            the enumeration
         */
        public Param(EnumDeclaration enumDeclaration) {
            fEnumDeclaration = enumDeclaration;
        }

    }

    /**
     * The instance
     */
    public static final EnumBodyParser INSTANCE = new EnumBodyParser();

    private EnumBodyParser() {
    }

    @Override
    public EnumDeclaration parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        Param parameter = (Param) param;
        EnumDeclaration enumDeclaration = parameter.fEnumDeclaration;
        @SuppressWarnings("unchecked")
        List<CommonTree> enumerators = (List<CommonTree>) tree.getChildren();
        /*
         * Start at -1, so that if the first enumrator has no explicit value, it
         * will choose 0
         */
        for (CommonTree enumerator : enumerators) {
            EnumeratorParser.INSTANCE.parse(enumerator, new EnumeratorParser.Param(enumDeclaration));
        }
        return enumDeclaration;
    }

}
