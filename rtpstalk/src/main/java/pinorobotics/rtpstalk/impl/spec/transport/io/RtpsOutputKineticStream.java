/*
 * Copyright 2022 rtpstalk project
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

import id.kineticstreamer.KineticStreamWriter;
import id.kineticstreamer.OutputKineticStream;
import id.xfunction.Preconditions;
import id.xfunction.XByte;
import id.xfunction.logging.XLogger;
import id.xfunction.util.ImmutableMultiMap;
import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import pinorobotics.rtpstalk.impl.InternalUtils;
import pinorobotics.rtpstalk.impl.spec.RtpsSpecReference;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.StatusInfo;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.DataSubmessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ProtocolVersion.Predefined;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;

/**
 * @author aeon_flux aeon_flux@eclipso.ch
 */
class RtpsOutputKineticStream implements OutputKineticStream {

    private static final XLogger LOGGER = XLogger.getLogger(RtpsInputKineticStream.class);
    private ByteBuffer buf;
    private KineticStreamWriter writer;

    public RtpsOutputKineticStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeArray(Object[] a, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeArray");
        if (a.getClass().componentType() == Submessage.class) {
            // writing it manually since it has polymorphic types inside
            writeSubmessages((Submessage[]) a);
        } else {
            for (int i = 0; i < a.length; i++) {
                writer.write(a[i]);
            }
        }
        LOGGER.exiting("writeArray");
    }

    private void writeSubmessages(Submessage[] a) throws Exception {
        for (int i = 0; i < a.length; i++) {
            Preconditions.isTrue(buf.position() % 4 == 0, "Invalid submessage alignment");

            if (a[i] instanceof DataSubmessage data) writeData(data);
            else writer.write(a[i]);

            // The PSM aligns each Submessage on a 32-bit boundary with respect
            // to the start of the Message (9.4 Mapping of the RTPS Messages)
            // To satisfy RTPS requirement we may need to add padding
            align(4);
        }
    }

    @RtpsSpecReference(
            paragraph = "9.4.1",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "The PSM aligns each Submessage on a 32-bit boundary with respect to the start"
                            + " of the Message")
    private void align(int blockSize) throws Exception {
        var pos = buf.position();
        var padding = InternalUtils.getInstance().padding(pos, 4);
        for (int i = 0; i < padding; i++) {
            writeByte((byte) 0);
        }
    }

    private void writeByte(byte b) throws Exception {
        writeByte(b, EMPTY_ANNOTATIONS);
    }

    @Override
    public void writeByte(Byte b, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeByte");
        buf.put(b);
        LOGGER.exiting("writeByte");
    }

    @Override
    public void writeByteArray(byte[] a, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeByteArray");
        buf.put(a);
        LOGGER.exiting("writeByteArray");
    }

    @Override
    public void writeInt(Integer i, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeInt");
        buf.putInt(i);
        LOGGER.exiting("writeInt");
    }

    @Override
    public void writeLong(Long l, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeLong");
        buf.putLong(l);
        LOGGER.exiting("writeLong");
    }

    @Override
    public void writeShort(Short s, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeShort");
        buf.putShort(s);
        LOGGER.exiting("writeShort");
    }

    @Override
    public void writeShortArray(short[] a, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeShortArray");
        if (a.length > 0) {
            var tmpBuf = buf.asShortBuffer();
            tmpBuf.put(a);
            buf.position(buf.position() + a.length * Short.BYTES);
        }
        LOGGER.exiting("writeShortArray");
    }

    @Override
    public void writeString(String str, Annotation[] annotations) throws Exception {
        LOGGER.entering("writeString");
        writeInt(str.length() + 1 /* NULL byte */);
        writeByteArray(str.getBytes(), EMPTY_ANNOTATIONS);
        writeByte((byte) 0);
        LOGGER.exiting("writeString");
    }

    public void setWriter(KineticStreamWriter writer) {
        this.writer = writer;
    }

    public void writeData(DataSubmessage data) throws Exception {
        LOGGER.entering("writeData");
        writer.write(data);
        if (data.getInlineQos().isPresent()) writeParameterList(data.getInlineQos().get());
        try {
            var payload = data.getSerializedPayload().orElse(null);
            if (payload == null) return;
            if (payload.serializedPayloadHeader.isPresent())
                writer.write(payload.serializedPayloadHeader.get());
            writer.write(payload);
        } finally {
            LOGGER.exiting("writeData");
        }
    }

    public void writeParameterList(ParameterList pl) throws Exception {
        LOGGER.entering("writeParameterList");
        if (pl.isEmpty()) return;
        var paramListStart = buf.position();
        writeParameterList(
                pl.getUserParameters(),
                k -> k,
                e ->
                        LengthCalculator.getInstance()
                                .calculateUserParameterValueLength(e.getValue()));
        writeParameterList(
                pl.getProtocolParameters(),
                k -> k.getValue(),
                e -> LengthCalculator.getInstance().calculateParameterValueLength(e));
        writeShort(ParameterId.PID_SENTINEL.getValue(), EMPTY_ANNOTATIONS);
        writeShort((short) 0, EMPTY_ANNOTATIONS);
        Preconditions.isTrue(
                (buf.position() - paramListStart) % 4 == 0,
                "Invalid param alignment: PID_SENTINEL");
        LOGGER.exiting("writeParameterList");
    }

    @RtpsSpecReference(
            paragraph = "9.4.2.11",
            protocolVersion = Predefined.Version_2_3,
            text =
                    "ParameterList A ParameterList contains a list of Parameters, terminated"
                            + " with a sentinel. Each Parameter within the ParameterList starts"
                            + " aligned on a 4-byte boundary with respect to the start of the"
                            + " ParameterList.")
    private <K, V> void writeParameterList(
            ImmutableMultiMap<K, V> parameterList,
            Function<K, Short> paramIdMapper,
            Function<Entry<K, List<V>>, Integer> lenCalculator)
            throws Exception {
        LOGGER.entering("writeParameterList");
        if (parameterList.isEmpty()) return;
        var paramListStart = buf.position();
        for (var param : parameterList) {
            var values = param.getValue();
            if (values.isEmpty()) continue;
            var len = lenCalculator.apply(param);
            if (len == 0) continue;
            Preconditions.isTrue(
                    (buf.position() - paramListStart) % 4 == 0,
                    "Invalid param alignment: %s",
                    param.getKey());
            writeShort(paramIdMapper.apply(param.getKey()), EMPTY_ANNOTATIONS);
            writeShort(len.shortValue(), EMPTY_ANNOTATIONS);
            var endPos = buf.position() + len;
            var firstValue = values.get(0);
            if (firstValue instanceof Locator locator) writeLocator(locator);
            else if (firstValue instanceof StatusInfo statusInfo) writeStatusInfo(statusInfo);
            else writer.write(firstValue);

            // pad rest with zeros
            while (buf.position() < endPos) writeByte((byte) 0);
        }
        LOGGER.exiting("writeParameterList");
    }

    private void writeLocator(Locator locator) throws Exception {
        LOGGER.entering("writeLocator");
        writeInt(locator.kind().value);
        writeInt((int) locator.port());
        switch (locator.kind()) {
            case LOCATOR_KIND_UDPv4:
                {
                    var buf = new byte[LengthCalculator.ADDRESS_SIZE];
                    System.arraycopy(locator.address().getAddress(), 0, buf, 12, 4);
                    writeByteArray(buf, EMPTY_ANNOTATIONS);
                    break;
                }
            case LOCATOR_KIND_UDPv6:
                {
                    LOGGER.severe("LOCATOR_KIND_UDPv6 is not supported, writing empty address");
                    var buf = new byte[LengthCalculator.ADDRESS_SIZE];
                    writeByteArray(buf, EMPTY_ANNOTATIONS);
                    break;
                }
            default:
                // ignore
                break;
        }
        LOGGER.exiting("writeLocator");
    }

    private void writeInt(int value) throws Exception {
        writeInt(value, EMPTY_ANNOTATIONS);
    }

    public void writeSequenceNumber(SequenceNumber num) throws Exception {
        LOGGER.entering("writeSequenceNumber");
        writeInt((int) (num.value >> 31));
        writeInt((int) ((-1L >> 31) & num.value));
        LOGGER.exiting("writeSequenceNumber");
    }

    public void writeSequenceNumberSet(SequenceNumberSet set) throws Exception {
        LOGGER.entering("writeSequenceNumberSet");
        writeSequenceNumber(set.bitmapBase);
        writeInt(set.numBits.value);
        for (var i : set.bitmap) {
            buf.putInt(XByte.reverseBitsInBytes(Integer.reverseBytes(i)));
        }
        LOGGER.exiting("writeSequenceNumberSet");
    }

    public void writeEntityId(EntityId entiyId) throws Exception {
        LOGGER.entering("writeEntityId");
        buf.putInt(Integer.reverseBytes(entiyId.value));
        LOGGER.exiting("writeEntityId");
    }

    public void writeStatusInfo(StatusInfo statusInfo) {
        LOGGER.entering("writeStatusInfo");
        buf.putInt(Integer.reverseBytes(statusInfo.value));
        LOGGER.exiting("writeStatusInfo");
    }

    @Override
    public void writeBoolean(Boolean arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBooleanArray(boolean[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDouble(Double arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDoubleArray(double[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloat(Float arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloatArray(float[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeChar(Character arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeCharArray(char[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeStringArray(String[] arg0, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeIntArray(int[] a, Annotation[] annotations) throws Exception {
        throw new UnsupportedOperationException();
    }
}
