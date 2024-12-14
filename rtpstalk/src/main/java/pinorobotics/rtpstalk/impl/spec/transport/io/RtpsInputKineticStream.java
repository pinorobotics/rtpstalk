/*
 * Copyright 2022 pinorobotics
 * 
 * Website: https://github.com/pinorobotics/rtpstalk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pinorobotics.rtpstalk.impl.spec.transport.io;

import static pinorobotics.rtpstalk.impl.spec.transport.io.IoConstants.EMPTY_ANNOTATIONS;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReader;
import id.xfunction.Preconditions;
import id.xfunction.XByte;
import id.xfunction.lang.XRuntimeException;
import id.xfunction.logging.XLogger;
import id.xfunction.util.ImmutableMultiMap;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.ByteSequence;
import pinorobotics.rtpstalk.impl.spec.messages.DataRepresentationQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.Header;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.LocatorKind;
import pinorobotics.rtpstalk.impl.spec.messages.ProtocolId;
import pinorobotics.rtpstalk.impl.spec.messages.ShortSequence;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.UnsignedInt;
import pinorobotics.rtpstalk.impl.spec.messages.UserDataQosPolicy;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataFrag;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataSubmessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Heartbeat;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Payload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RawData;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.RepresentationIdentifier;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayload;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SerializedPayloadHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.SubmessageHeader;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;
import pinorobotics.rtpstalk.impl.spec.transport.io.exceptions.NotRtpsPacketException;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
class RtpsInputKineticStream implements InputKineticStream {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsInputKineticStream.class);
    private static final int SUBMESSAGE_HEADER_LENGTH =
            LengthCalculator.getInstance().getFixedLength(SubmessageHeader.class);
    private ByteBuffer buf;
    private KineticStreamReader reader;

    public RtpsInputKineticStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public void close() throws Exception {}

    @Override
    public Object[] readArray(Object[] a, Class<?> type, Annotation[] annotations)
            throws Exception {
        LOGGER.entering("readArray");
        Object[] ret = null;
        if (type == Submessage.class) {
            ret = readSubmessages();
        } else {
            throw new UnsupportedOperationException();
        }
        LOGGER.exiting("readArray");
        return ret;
    }

    @Override
    public boolean readBool(Annotation[] annotations) throws Exception {
        LOGGER.entering("readBool");
        var ret = readByte(EMPTY_ANNOTATIONS) != 0;
        LOGGER.exiting("readBool");
        return ret;
    }

    @Override
    public byte readByte(Annotation[] annotations) throws Exception {
        LOGGER.entering("readByte");
        var ret = buf.get();
        LOGGER.exiting("readByte");
        return ret;
    }

    @Override
    public byte[] readByteArray(byte[] a, Annotation[] annotations) throws Exception {
        LOGGER.entering("readByteArray");
        buf.get(a);
        LOGGER.exiting("readByteArray");
        return a;
    }

    @Override
    public int readInt(Annotation[] annotations) throws Exception {
        LOGGER.entering("readInt");
        var ret = buf.getInt();
        LOGGER.exiting("readInt");
        return ret;
    }

    @Override
    public String readString(Annotation[] annotations) throws Exception {
        var len = readInt();
        var b = new byte[len - 1];
        readByteArray(b, EMPTY_ANNOTATIONS);
        Preconditions.equals(0, readByte(EMPTY_ANNOTATIONS), "Null byte expected");
        return new String(b);
    }

    private int readInt() throws Exception {
        return readInt(EMPTY_ANNOTATIONS);
    }

    @Override
    public long readLong(Annotation[] annotations) throws Exception {
        LOGGER.entering("readLong");
        var ret = buf.getLong();
        LOGGER.exiting("readLong");
        return ret;
    }

    @Override
    public short readShort(Annotation[] annotations) throws Exception {
        LOGGER.entering("readShort");
        var ret = buf.getShort();
        LOGGER.exiting("readShort");
        return ret;
    }

    @Override
    public short[] readShortArray(short[] a, Annotation[] annotations) throws Exception {
        LOGGER.entering("readShortArray");
        if (a.length > 0) {
            var tmpBuf = buf.asShortBuffer();
            tmpBuf.get(a);
            buf.position(buf.position() + a.length * Short.BYTES);
        }
        LOGGER.exiting("readShortArray");
        return a;
    }

    @RtpsSpecReference(
            paragraph = "9.4.2.11",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "ParameterList A ParameterList contains a list of Parameters, terminated"
                            + " with a sentinel. Each Parameter within the ParameterList starts"
                            + " aligned on a 4-byte boundary with respect to the start of the"
                            + " ParameterList.")
    private ParameterList readParameterList(boolean withUserParameters) throws Exception {
        LOGGER.entering("readParameterList");
        short id;
        var protocolParams = new ArrayList<Map.Entry<ParameterId, Object>>();
        var userParams = new ArrayList<Map.Entry<Short, byte[]>>();
        while ((id = readShort(EMPTY_ANNOTATIONS)) != ParameterId.PID_SENTINEL.getValue()) {
            var len = readShort(EMPTY_ANNOTATIONS);
            var startPos = buf.position();
            var parameterId = ParameterId.map.get(id);
            if (parameterId == null) {
                if (!withUserParameters) {
                    LOGGER.fine("Unknown parameter {0}, ignoring...", Short.toUnsignedInt(id));
                    skip(len - (buf.position() - startPos));
                    continue;
                }
                var value = new byte[len];
                readByteArray(value, EMPTY_ANNOTATIONS);
                LOGGER.fine("{0}: byte array length {1}", Short.toUnsignedInt(id), value.length);
                userParams.add(Map.entry(id, value));
                continue;
            }
            Object value = null;
            switch (parameterId) {
                case PID_DOMAIN_ID:
                    value = new UnsignedInt(readInt());
                    break;
                case PID_ENTITY_NAME:
                case PID_TYPE_NAME:
                case PID_TOPIC_NAME:
                    value = readString(EMPTY_ANNOTATIONS);
                    break;
                case PID_UNICAST_LOCATOR:
                case PID_DEFAULT_UNICAST_LOCATOR:
                case PID_METATRAFFIC_UNICAST_LOCATOR:
                    value = readLocator();
                    break;
                case PID_USER_DATA:
                    value = new UserDataQosPolicy(readByteSequence());
                    break;
                case PID_DATA_REPRESENTATION:
                    value = new DataRepresentationQosPolicy(readShortSequence());
                    break;
                case PID_EXPECTS_INLINE_QOS:
                    value = readBool(EMPTY_ANNOTATIONS);
                    break;
                case PID_STATUS_INFO:
                    value = readStatusInfo();
                    break;
                case PID_BUILTIN_ENDPOINT_SET:
                case PID_BUILTIN_ENDPOINT_QOS:
                case PID_PARTICIPANT_LEASE_DURATION:
                case PID_ENDPOINT_GUID:
                case PID_PARTICIPANT_GUID:
                case PID_PROTOCOL_VERSION:
                case PID_VENDORID:
                case PID_KEY_HASH:
                case PID_RELIABILITY:
                case PID_DURABILITY:
                case PID_DEADLINE:
                case PID_LATENCY_BUDGET:
                case PID_HISTORY:
                case PID_LIFESPAN:
                case PID_DURABILITY_SERVICE:
                case PID_DESTINATION_ORDER:
                    value = reader.read(parameterId.getParameterClass());
                    break;
                default:
                    throw new UnsupportedOperationException("Parameter id " + id);
            }
            var finalValue = value;
            LOGGER.fine(() -> parameterId + ": " + finalValue);
            protocolParams.add(Map.entry(parameterId, finalValue));
            skip(len - (buf.position() - startPos));
        }
        // ignoring
        readShort(EMPTY_ANNOTATIONS);
        LOGGER.exiting("readParameterList");
        return ParameterList.of(
                new ImmutableMultiMap<>(protocolParams), new ImmutableMultiMap<>(userParams));
    }

    private void skip(int offset) {
        Preconditions.isTrue(offset >= 0, "Negative offset");
        LOGGER.fine("Skipping {0} bytes", offset);
        buf.position(buf.position() + offset);
    }

    private <T extends DataSubmessage> T readData(Class<T> dataType) throws Exception {
        LOGGER.entering("readData");
        var dataSubmessageStart = buf.position() + SUBMESSAGE_HEADER_LENGTH;
        var data = reader.read(dataType);
        if (data.isInlineQos()) {
            LOGGER.fine("Reading InlineQos");
            data.setInlineQos(readParameterList(true));
        }
        var dataLen = data.getSubmessageLength();
        if (buf.position() < dataSubmessageStart + dataLen) {
            Optional<SerializedPayloadHeader> payloadHeaderOpt = Optional.empty();
            // is it DataFrag without header
            boolean isDataFragNoHeader = false;
            if (data instanceof DataFrag dataFrag) {
                isDataFragNoHeader =
                        !DataFrag.hasSerializedPayloadHeader(
                                dataFrag.fragmentStartingNum.getUnsigned());
            }
            var representationId = Optional.of(RepresentationIdentifier.Predefined.CDR_LE);
            if (!isDataFragNoHeader) {
                var payloadHeader = reader.read(SerializedPayloadHeader.class);
                LOGGER.fine("payloadHeader: {0}", payloadHeader);
                representationId = payloadHeader.representation_identifier.getPredefinedValue();
                if (representationId.isEmpty())
                    throw new XRuntimeException(
                            "Unknown representation identifier %s",
                            payloadHeader.representation_identifier);
                payloadHeaderOpt = Optional.of(payloadHeader);
            }
            Payload payload =
                    switch (representationId.get()) {
                        case PL_CDR_LE -> readParameterList(false);
                        case CDR_LE ->
                                readRawData(dataLen - (buf.position() - dataSubmessageStart));
                        default ->
                                throw new UnsupportedOperationException(
                                        "Representation identifier " + representationId.get());
                    };

            LOGGER.fine("payload: {0}", payload);
            data.setSerializedPayload(new SerializedPayload(payloadHeaderOpt, payload));
        }
        LOGGER.exiting("readData");
        return data;
    }

    private RawData readRawData(int len) throws Exception {
        var a = new byte[len];
        readByteArray(a, EMPTY_ANNOTATIONS);
        return new RawData(a);
    }

    @RtpsSpecReference(
            paragraph = "9.4.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The PSM aligns each Submessage on a 32-bit boundary with respect to the start"
                            + " of the Message")
    private void align() throws Exception {
        var pos = buf.position();
        var padding = InternalUtils.getInstance().padding(pos, 4);
        buf.position(pos + padding);
    }

    private Submessage[] readSubmessages() throws Exception {
        LOGGER.entering("readSubmessages");
        var submessages = new ArrayList<Submessage>();
        while (buf.hasRemaining()) {

            // Skip padding if any
            align();
            Preconditions.isTrue(buf.position() % 4 == 0, "Invalid submessage alignment");

            // peek submessage type
            buf.mark();
            var submessageHeader = reader.read(SubmessageHeader.class);
            LOGGER.fine("submessageHeader: {0}", submessageHeader);

            // save position where submessage itself (NOT its header) starts
            var submessageStart = buf.position();
            LOGGER.fine("submessageStart: {0}", submessageStart);
            buf.reset();

            int submessageLen = submessageHeader.submessageLength.getUnsigned();
            var messageClassOpt = submessageHeader.submessageKind.getSubmessageClass();
            if (messageClassOpt.isEmpty()) {
                LOGGER.fine(
                        "Submessage kind {0} is not supported", submessageHeader.submessageKind);
                skip(submessageLen + SUBMESSAGE_HEADER_LENGTH);
            } else {
                // knowing submessage type now we can read it fully
                var submessage = readSubmessage(messageClassOpt.get());
                submessage.validate();
                if (!submessage.isLittleEndian()) {
                    throw new UnsupportedOperationException("Only Little Endian CDR is supported");
                }
                submessages.add(submessage);
                LOGGER.fine("submessage: {0}", submessage);
            }

            var submessageEnd = buf.position();
            LOGGER.fine("submessageEnd: {0}", submessageEnd);

            Preconditions.equals(
                    submessageLen,
                    submessageEnd - submessageStart,
                    "Read submessage size does not match expected");
        }
        LOGGER.exiting("readSubmessages");
        return submessages.toArray(Submessage[]::new);
    }

    private Submessage readSubmessage(Class<? extends Submessage> type) throws Exception {
        // submessages with polymorphic types inside we read manually
        if (type == Data.class) return readData(Data.class);
        else if (type == DataFrag.class) return readData(DataFrag.class);
        else if (type == Heartbeat.class) return readHeartbeat();
        else /* rest we leave for kineticstreamer */ return reader.read(type);
    }

    private Heartbeat readHeartbeat() throws Exception {
        // TODO support HeartbeatWithGroupInfo
        return reader.read(Heartbeat.class);
    }

    private ByteSequence readByteSequence() throws Exception {
        var value = new byte[readInt()];
        readByteArray(value, EMPTY_ANNOTATIONS);
        return new ByteSequence(value);
    }

    private ShortSequence readShortSequence() throws Exception {
        var value = new short[readInt()];
        readShortArray(value, EMPTY_ANNOTATIONS);
        return new ShortSequence(value);
    }

    private Locator readLocator() throws Exception {
        LOGGER.entering("readLocator");
        var kind = LocatorKind.VALUES.getOrDefault(readInt(), LocatorKind.LOCATOR_KIND_INVALID);
        var port = readInt();
        var buf = new byte[LengthCalculator.ADDRESS_SIZE];
        readByteArray(buf, EMPTY_ANNOTATIONS);
        InetAddress address = null;
        switch (kind) {
            case LOCATOR_KIND_UDPv4:
                address = InetAddress.getByAddress(new byte[] {buf[12], buf[13], buf[14], buf[15]});
                break;
            case LOCATOR_KIND_UDPv6:
                {
                    LOGGER.severe("LOCATOR_KIND_UDPv6 is not supported, reading empty address");
                    address = InetAddress.getByAddress(new byte[4]);
                    break;
                }
            case LOCATOR_KIND_INVALID:
                return Locator.INVALID;
            default:
                throw new XRuntimeException("Unknown locator kind %s", kind);
        }
        LOGGER.exiting("readLocator");
        return new Locator(kind, port, address);
    }

    public void setKineticStreamReader(KineticStreamReader ksr) {
        reader = ksr;
    }

    public Object readHeader() throws Exception {
        LOGGER.entering("readHeader");
        var header = reader.read(Header.class);
        if (!Objects.equals(header.protocolId, ProtocolId.Predefined.RTPS.getValue())) {
            throw new NotRtpsPacketException();
        }
        if (!ProtocolVersion.isSupported(header.protocolVersion)) {
            throw new XRuntimeException(
                    "RTPS protocol version %s not supported", header.protocolVersion);
        }
        // TODO check little endian only
        LOGGER.fine("header: {0}", header);
        LOGGER.exiting("readHeader");
        return header;
    }

    public SequenceNumber readSequenceNumber() throws Exception {
        int high = readInt();
        int low = readInt();
        return new SequenceNumber((high << 31) | low);
    }

    public SequenceNumberSet readSequenceNumberSet() throws Exception {
        LOGGER.entering("readSequenceNumberSet");
        var bitmapBase = readSequenceNumber();
        var numBits = readInt();
        var bits = new int[(numBits + 31) / 32];
        for (int i = 0; i < bits.length; i++) {
            bits[i] = XByte.reverseBitsInBytes(Integer.reverseBytes(buf.getInt()));
        }
        LOGGER.exiting("readSequenceNumberSet");
        return new SequenceNumberSet(bitmapBase, numBits, bits);
    }

    public EntityId readEntityId() throws Exception {
        var val = Integer.reverseBytes(buf.getInt());
        return new EntityId(val);
    }

    public StatusInfo readStatusInfo() {
        return new StatusInfo(Integer.reverseBytes(buf.getInt()));
    }

    @Override
    public double readDouble(Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] readDoubleArray(double[] a, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public float readFloat(Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public float[] readFloatArray(float[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public char readChar(Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public char[] readCharArray(char[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean[] readBooleanArray(boolean[] a, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] readIntArray(int[] a, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public long[] readLongArray(long[] a, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] readStringArray(String[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }
}
