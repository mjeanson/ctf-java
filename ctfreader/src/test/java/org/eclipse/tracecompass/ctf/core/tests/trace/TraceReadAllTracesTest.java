/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Read all the traces and verify some metrics. Nominally the event count and
 * the duration of the trace (not the time to parse it).
 *
 * @author Matthew Khouzam
 */
@RunWith(Parameterized.class)
public class TraceReadAllTracesTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    /**
     * Get the list of traces
     *
     * @return the list of traces
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTracePaths() {
        CtfTestTrace[] values = CtfTestTrace.values();
        List<Object[]> list = new ArrayList<>();
        for (CtfTestTrace value : values) {
            list.add(new Object[] { value.name(), value });
        }
        return list;
    }

    private final CtfTestTrace fTraceEnum;

    /**
     * Constructor
     *
     * @param name
     *            name of the enum
     *
     * @param traceEnum
     *            the enum to test
     */
    public TraceReadAllTracesTest(String name, CtfTestTrace traceEnum) {
        fTraceEnum = traceEnum;
    }

    /**
     * Reads all the traces
     */
    @Test
    public void readTraces() {
        if (fTraceEnum.getNbEvents() != -1) {
            try (CtfTestTraceExtractor testTraceWrapper = CtfTestTraceExtractor.extractTestTrace(fTraceEnum);
                    CTFTraceReader reader = new CTFTraceReader(testTraceWrapper.getTrace());) {
                IEventDefinition currentEventDef = reader.getCurrentEventDef();
                double start = currentEventDef.getTimestamp();
                long count = 0;
                double end = start;
                while (reader.hasMoreEvents()) {
                    reader.advance();
                    count++;
                    currentEventDef = reader.getCurrentEventDef();
                    if (currentEventDef != null) {
                        end = currentEventDef.getTimestamp();
                        if (currentEventDef.getDeclaration().getName().equals(CTFStrings.LOST_EVENT_NAME)) {
                            count += ((IntegerDefinition) currentEventDef.getFields()
                                    .getDefinition(CTFStrings.LOST_EVENTS_FIELD)).getValue() - 1;
                        }
                    }
                }
                assertEquals("Event count", fTraceEnum.getNbEvents(), count);
                assertEquals("Trace duration", fTraceEnum.getDuration(), (end - start) / 1000000000.0, 1.0);
            } catch (CTFException e) {
                fail(fTraceEnum.name() + " " + e.getMessage());
            }
        } else {
            assumeTrue("Trace did not specify events count", false);
        }
    }
}
