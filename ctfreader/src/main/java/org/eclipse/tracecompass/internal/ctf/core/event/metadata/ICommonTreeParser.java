/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.event.metadata;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.event.metadata.ParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Common tree parser interface. Should only have one method
 * {@link #parse(CommonTree, ICommonTreeParserParameter)}
 *
 * It is recommended to add to the javadoc on this inerface as it is not
 * specific
 *
 * @author Matthew Khouzam
 *
 */
public interface ICommonTreeParser {

    /**
     * Parameter object to avoid passing "Object" as a parameter
     * @author Matthew Khouzam
     *
     */
    public interface ICommonTreeParserParameter{

    }

    /**
     * The only parse method of the common tree parser. Caution must be used
     * handling this as it can return any type and thus care must be used with
     * the input and output.
     *
     *
     * @param tree
     *            the common tree input
     * @param param
     *            the parameter to pass (for lookups)
     * @return the parsed data
     * @throws ParseException
     *             if the tree or data is wrong
     */
    @NotNull Object parse(CommonTree tree, ICommonTreeParserParameter param) throws ParseException;

}
