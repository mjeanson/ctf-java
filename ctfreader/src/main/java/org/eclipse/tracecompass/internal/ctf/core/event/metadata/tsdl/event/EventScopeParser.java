/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.event;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.MetadataStrings;

import java.util.List;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;

/**
 * A local static scope consists in the scope generated by the declaration of
 * fields within a compound type. A static scope is a local static scope
 * augmented with the nested sub-static-scopes it contains.
 * <p>
 * A dynamic scope consists in the static scope augmented with the implicit
 * event structure definition hierarchy.
 * <p>
 * Multiple declarations of the same field name within a local static scope is
 * not valid. It is however valid to re-use the same field name in different
 * local scopes.
 * <p>
 * Nested static and dynamic scopes form lookup paths. These are used for
 * variant tag and sequence length references. They are used at the variant and
 * sequence definition site to look up the location of the tag field associated
 * with a variant, and to lookup up the location of the length field associated
 * with a sequence.
 * <p>
 * Variants and sequences can refer to a tag field either using a relative path
 * or an absolute path. The relative path is relative to the scope in which the
 * variant or sequence performing the lookup is located. Relative paths are only
 * allowed to lookup within the same static scope, which includes its nested
 * static scopes. Lookups targeting parent static scopes need to be performed
 * with an absolute path.
 * <p>
 * Absolute path lookups use the full path including the dynamic scope followed
 * by a . and then the static scope. Therefore, variants (or sequences) in lower
 * levels in the dynamic scope (e.g., event context) can refer to a tag (or
 * length) field located in upper levels (e.g., in the event header) by
 * specifying, in this case, the associated tag with
 * <stream.event.header.field_name>. This allows, for instance, the event
 * context to define a variant referring to the id field of the event header as
 * selector.
 * <p>
 * The dynamic scope prefixes are thus:
 * <ul>
 * <li>Trace environment: <env. ></li>
 * <li>Trace packet header: <trace.packet.header. ></li>
 * <li>Stream packet context: <stream.packet.context. ></li>
 * <li>Event header: <stream.event.header. ></li>
 * <li>Stream event context: <stream.event.context. ></li>
 * <li>Event context: <event.context. ></li>
 * <li>Event payload: <event.fields. ></li>
 * </ul>
 * <p>
 * The target dynamic scope must be specified explicitly when referring to a
 * field outside of the static scope (absolute scope reference). No conflict can
 * occur between relative and dynamic paths, because the keywords trace, stream,
 * and event are reserved, and thus not permitted as field names. It is
 * recommended that field names clashing with CTF and C99 reserved keywords use
 * an underscore prefix to eliminate the risk of generating a description
 * containing an invalid field name. Consequently, fields starting with an
 * underscore should have their leading underscore removed by the CTF trace
 * readers.
 * <p>
 * The information available in the dynamic scopes can be thought of as the
 * current tracing context. At trace production, information about the current
 * context is saved into the specified scope field levels. At trace consumption,
 * for each event, the current trace context is therefore readable by accessing
 * the upper dynamic scopes.
 *
 * @author Matthew Khouzam - Initial API and implementation
 * @author Efficios - Description
 *
 */
public final class EventScopeParser implements ICommonTreeParser {

    /**
     * Parameter object containing a list of common trees
     *
     * @author Matthew Khouzam
     *
     */
    public static final class Param implements ICommonTreeParserParameter {
        private final List<CommonTree> fList;

        /**
         * Constructor
         *
         * @param list
         *            parameter list
         */
        public Param(List<CommonTree> list) {
            fList = list;

        }

    }

    /**
     * Instance
     */
    public static final EventScopeParser INSTANCE = new EventScopeParser();

    private EventScopeParser() {
    }

    /**
     * Parses in a different way, the AST node is unused, and the event list
     * contains data to read. This converts a scope from a set of strings to
     * "event.subscope1.subscope2".
     *
     * @param unused
     *            unused AST node
     * @param param
     *            a list of tree nodes that contain the scope description.
     * @return a concatenated string of the scope
     */
    @Override
    public String parse(CommonTree unused, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        List<CommonTree> lengthChildren = ((Param) param).fList;
        CommonTree nextElem = (CommonTree) lengthChildren.get(1).getChild(0);
        String lengthName;
        switch (nextElem.getType()) {
        case CTFParser.UNARY_EXPRESSION_STRING:
        case CTFParser.IDENTIFIER:
            List<CommonTree> sublist = lengthChildren.subList(1, lengthChildren.size());
            lengthName = MetadataStrings.EVENT + '.' + concatenateUnaryStrings(sublist);
            break;
        default:
            throw new ParseException("Unsupported scope event." + nextElem); //$NON-NLS-1$
        }
        return lengthName;
    }

}
