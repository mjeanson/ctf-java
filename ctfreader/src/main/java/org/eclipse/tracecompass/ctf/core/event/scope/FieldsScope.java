/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.scope;

import org.jetbrains.annotations.Nullable;

/**
 * A lttng specific speedup node field scope of a lexical scope
 *
 * @author Matthew Khouzam
 */
public final class FieldsScope extends LexicalScope {

    /**
     * The scope constructor
     *
     * @param parent
     *            The parent node, can be null, but shouldn't
     * @param name
     *            the name of the field
     */
    FieldsScope(ILexicalScope parent, String name) {
        super(parent, name);
    }

    @Override
    public @Nullable ILexicalScope getChild(String name) {
        if (name.equals(FIELDS_RET.getName())) {
            return FIELDS_RET;
        }
        if (name.equals(FIELDS_TID.getName())) {
            return FIELDS_TID;
        }
        return super.getChild(name);
    }

}
