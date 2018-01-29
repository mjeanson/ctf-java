/*******************************************************************************
 * Copyright (c) 2011, 2015 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core.trace;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.ctf.core.trace.IPacketReader;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * <b><u>StreamInputPacketIndexEntry</u></b>
 * <p>
 * Represents an entry in the index of event packets.
 */
public class StreamInputPacketIndexEntry implements ICTFPacketDescriptor {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\D*(\\d+)"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Position of the start of the packet header in the file, in bits
     */
    private final long fOffsetBits;

    /**
     * Position of the start of the packet header in the file, in bytes
     */
    private final long fOffsetBytes;

    /**
     * Packet size, in bits
     */
    private final long fPacketSizeBits;

    /**
     * Content size, in bits
     */
    private final long fContentSizeBits;

    /**
     * Begin timestamp
     */
    private final long fTimestampBegin;

    /**
     * End timestamp
     */
    private final long fTimestampEnd;

    /**
     * How many lost events are there?
     */
    private final long fLostEvents;

    /**
     * Which target is being traced
     */
    private final String fTarget;
    private final long fTargetID;

    /**
     * Attributes of this index entry
     */
    private final @NotNull Map<String, Object> fAttributes;

    private final long fEndPacketHeaderBits;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an index entry.
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param fileSizeBytes
     *            number of bytes in a file
     *
     *            TODO: Remove
     */

    public StreamInputPacketIndexEntry(long dataOffsetBits, long fileSizeBytes) {
        fAttributes = Collections.EMPTY_MAP;
        fContentSizeBits = (fileSizeBytes * Byte.SIZE);
        fPacketSizeBits = (fileSizeBytes * Byte.SIZE);
        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;
        fLostEvents = 0;
        fTarget = ""; //$NON-NLS-1$
        fTargetID = 0;
        fTimestampBegin = 0;
        fTimestampEnd = Long.MAX_VALUE;
        fEndPacketHeaderBits = dataOffsetBits;
    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param lostSoFar
     *            number of lost events so far
     *
     *            TODO: Remove
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long lostSoFar) {
        this(dataOffsetBits, streamPacketContextDef, fileSizeBytes, lostSoFar, dataOffsetBits);
    }

    /**
     * full Constructor
     *
     * @param dataOffsetBits
     *            offset in the file for the start of data in bits
     * @param streamPacketContextDef
     *            packet context
     * @param fileSizeBytes
     *            number of bytes in a file
     * @param lostSoFar
     *            number of lost events so far
     * @param endPacketHeaderBits
     *            end of packet headers
     */
    public StreamInputPacketIndexEntry(long dataOffsetBits, StructDefinition streamPacketContextDef, long fileSizeBytes, long lostSoFar, long endPacketHeaderBits) {
        fEndPacketHeaderBits = endPacketHeaderBits;
        fAttributes = computeAttributeMap(streamPacketContextDef);
        fContentSizeBits = computeContentSize(fileSizeBytes);
        fPacketSizeBits = computePacketSize(fileSizeBytes);
        fTimestampBegin = computeTsBegin();
        fTimestampEnd = computeTsEnd();
        fOffsetBits = dataOffsetBits;
        fOffsetBytes = dataOffsetBits / Byte.SIZE;

        // LTTng Specific
        Target target = lookupTarget(streamPacketContextDef);
        fTarget = target.string;
        fTargetID = target.number;
        fLostEvents = computeLostEvents(lostSoFar);
    }

    /**
     * Copy constructor that updates the timestamp end
     *
     * @param entryToAdd
     *            the original {@link StreamInputPacketIndexEntry}
     * @param newTimestampEnd
     *            the new timestamp end
     */
    public StreamInputPacketIndexEntry(ICTFPacketDescriptor entryToAdd, long newTimestampEnd) {
        fEndPacketHeaderBits = entryToAdd.getPayloadStartBits();
        fAttributes = entryToAdd.getAttributes();
        fContentSizeBits = entryToAdd.getContentSizeBits();
        fPacketSizeBits = entryToAdd.getPacketSizeBits();
        fTimestampBegin = entryToAdd.getTimestampBegin();
        fTimestampEnd = newTimestampEnd;
        fOffsetBits = entryToAdd.getOffsetBits();
        fOffsetBytes = entryToAdd.getOffsetBits();

        // LTTng Specific
        fTarget = entryToAdd.getTarget();
        fTargetID = entryToAdd.getTargetId();
        Target target = new Target();
        target.number = fTargetID;
        target.string = fTarget;
        fLostEvents = entryToAdd.getLostEvents();
    }

    private static @NotNull Map<String, Object> computeAttributeMap(StructDefinition streamPacketContextDef) {
        Builder<String, Object> attributeBuilder = ImmutableMap.<String, Object> builder();
        for (String field : streamPacketContextDef.getDeclaration().getFieldsList()) {
            IDefinition id = streamPacketContextDef.lookupDefinition(field);
            if (id instanceof IntegerDefinition) {
                attributeBuilder.put(field, ((IntegerDefinition) id).getValue());
            } else if (id instanceof FloatDefinition) {
                attributeBuilder.put(field, ((FloatDefinition) id).getValue());
            } else if (id instanceof EnumDefinition) {
                final EnumDefinition enumDec = (EnumDefinition) id;
                attributeBuilder.put(field, new AbstractMap.SimpleImmutableEntry<>(
                        requireNonNull(enumDec.getStringValue()),
                        requireNonNull(enumDec.getIntegerValue())));
            } else if (id instanceof StringDefinition) {
                attributeBuilder.put(field, ((StringDefinition) id).getValue());
            }
        }
        return attributeBuilder.build();
    }

    private Long getPacketSize() {
        return (Long) fAttributes.get(CTFStrings.PACKET_SIZE);
    }

    private long computeContentSize(long fileSizeBytes) {
        Long contentSize = (Long) fAttributes.get(CTFStrings.CONTENT_SIZE);
        /* Read the content size in bits */
        if (contentSize != null) {
            return contentSize.longValue();
        }
        Long packetSize = getPacketSize();
        if (packetSize != null) {
            return packetSize.longValue();
        }
        return fileSizeBytes * Byte.SIZE;
    }

    private long computePacketSize(long fileSizeBytes) {
        Long packetSize = getPacketSize();
        /* Read the packet size in bits */
        if (packetSize != null) {
            return packetSize.longValue();
        }
        long contentSizeBits = computeContentSize(fileSizeBytes);
        if (contentSizeBits != 0) {
            return contentSizeBits;
        }
        return fileSizeBytes * Byte.SIZE;
    }

    private long computeTsBegin() {
        Long tsBegin = (Long) fAttributes.get(CTFStrings.TIMESTAMP_BEGIN);
        /* Read the begin timestamp */
        if (tsBegin != null) {
            return tsBegin.longValue();
        }
        return 0;
    }

    private long computeTsEnd() {
        Long tsEnd = (Long) fAttributes.get(CTFStrings.TIMESTAMP_END);
        /* Read the end timestamp */
        if (tsEnd != null) {
            // check if tsEnd == unsigned long max value
            if (tsEnd == -1) {
                return Long.MAX_VALUE;
            }
            return tsEnd.longValue();
        }
        return Long.MAX_VALUE;
    }

    private long computeLostEvents(long lostSoFar) {
        Long lostEvents = (Long) fAttributes.get(CTFStrings.EVENTS_DISCARDED);
        if (lostEvents != null) {
            return lostEvents - lostSoFar;
        }
        return 0;
    }

    private static class Target {
        public String string;
        public long number;

        public Target() {
            string = null;
            number = IPacketReader.UNKNOWN_CPU;
        }
    }

    private Target lookupTarget(StructDefinition streamPacketContextDef) {
        Target ret = new Target();
        boolean hasDevice = fAttributes.containsKey(CTFStrings.DEVICE);
        if (hasDevice) {
            IDefinition def = streamPacketContextDef.lookupDefinition(CTFStrings.DEVICE);
            if (def instanceof SimpleDatatypeDefinition) {
                SimpleDatatypeDefinition simpleDefinition = (SimpleDatatypeDefinition) def;
                ret.string = simpleDefinition.getStringValue();
                ret.number = simpleDefinition.getIntegerValue();
            } else if (def instanceof StringDefinition) {
                StringDefinition stringDefinition = (StringDefinition) def;
                ret.string = stringDefinition.getValue();
                final Matcher matcher = NUMBER_PATTERN.matcher(ret.string);
                if (matcher.matches()) {
                    String number = matcher.group(1);
                    ret.number = Integer.parseInt(number);
                }
            }
        } else {
            Long cpuId = (Long) fAttributes.get(CTFStrings.CPU_ID);
            if (cpuId != null) {
                ret.string = ("CPU" + cpuId.toString()); //$NON-NLS-1$
                ret.number = cpuId;
            }
        }
        return ret;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public boolean includes(long ts) {
        return (ts >= fTimestampBegin) && (ts <= fTimestampEnd);
    }

    @Override
    public String toString() {
        return "StreamInputPacketIndexEntry [offsetBits=" + fOffsetBits //$NON-NLS-1$
                + ", timestampBegin=" + fTimestampBegin + ", timestampEnd=" //$NON-NLS-1$ //$NON-NLS-2$
                + fTimestampEnd + "]"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    @Override
    public long getOffsetBits() {
        return fOffsetBits;
    }

    @Override
    public long getPacketSizeBits() {
        return fPacketSizeBits;
    }

    @Override
    public long getContentSizeBits() {
        return fContentSizeBits;
    }

    @Override
    public long getTimestampBegin() {
        return fTimestampBegin;
    }

    @Override
    public long getTimestampEnd() {
        return fTimestampEnd;
    }

    @Override
    public long getLostEvents() {
        return fLostEvents;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return fAttributes;
    }

    @Override
    public String getTarget() {
        return fTarget;
    }

    @Override
    public long getTargetId() {
        return fTargetID;
    }

    @Override
    public long getOffsetBytes() {
        return fOffsetBytes;
    }

    @Override
    public long getPayloadStartBits() {
        return fEndPacketHeaderBits;
    }
}
