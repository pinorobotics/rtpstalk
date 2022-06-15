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

import id.kineticstreamer.KineticStreamWriter;
import id.kineticstreamer.OutputKineticStream;
import id.xfunction.Preconditions;
import id.xfunction.XByte;
import id.xfunction.logging.XLogger;
import java.nio.ByteBuffer;
import pinorobotics.rtpstalk.impl.spec.messages.Locator;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Data;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.Submessage;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.EntityId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumber;
import pinorobotics.rtpstalk.impl.spec.messages.submessages.elements.SequenceNumberSet;

/** @author aeon_flux aeon_flux@eclipso.ch */
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
    public void writeArray(Object[] a) throws Exception {
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
            // The PSM aligns each Submessage on a 32-bit boundary with respect
            // to the start of the Message (9.4 Mapping of the RTPS Messages)
            // To satisfy RTPS requirement we may need to add padding
            while (buf.position() % 4 != 0) writeByte((byte) 0);
            Preconditions.isTrue(buf.position() % 4 == 0, "Invalid submessage alignment");

            if (a[i] instanceof Data data) writeData(data);
            else writer.write(a[i]);
        }
    }

    @Override
    public void writeBoolean(Boolean arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeBooleanArray(boolean[] arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeByte(Byte b) throws Exception {
        LOGGER.entering("writeByte");
        buf.put(b);
        LOGGER.exiting("writeByte");
    }

    @Override
    public void writeByteArray(byte[] a) throws Exception {
        LOGGER.entering("writeByteArray");
        for (int i = 0; i < a.length; i++) {
            writeByte(a[i]);
        }
        LOGGER.exiting("writeByteArray");
    }

    @Override
    public void writeDouble(Double arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDoubleArray(double[] arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeFloat(Float arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInt(Integer i) throws Exception {
        LOGGER.entering("writeInt");
        buf.putInt(Integer.reverseBytes(i));
        LOGGER.exiting("writeInt");
    }

    @Override
    public void writeIntArray(int[] a) throws Exception {
        LOGGER.entering("writeIntArray");
        for (int i = 0; i < a.length; i++) {
            writeInt(a[i]);
        }
        LOGGER.exiting("writeIntArray");
    }

    @Override
    public void writeLong(Long l) throws Exception {
        LOGGER.entering("writeLong");
        buf.putLong(Long.reverseBytes(l));
        LOGGER.exiting("writeLong");
    }

    @Override
    public void writeShort(Short s) throws Exception {
        LOGGER.entering("writeShort");
        buf.putShort(Short.reverseBytes(s));
        LOGGER.exiting("writeShort");
    }

    @Override
    public void writeShortArray(short[] arg0) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeString(String str) throws Exception {
        LOGGER.entering("writeString");
        writeInt(str.length() + 1 /* NULL byte */);
        writeByteArray(str.getBytes());
        writeByte((byte) 0);
        LOGGER.exiting("writeString");
    }

    public void setWriter(KineticStreamWriter writer) {
        this.writer = writer;
    }

    public void writeData(Data data) throws Exception {
        LOGGER.entering("writeData");
        writer.write(data);
        writer.write(data.serializedPayload);
        LOGGER.exiting("writeData");
    }

    public void writeParameterList(ParameterList pl) throws Exception {
        LOGGER.entering("writeParameterList");
        var paramListStart = buf.position();
        for (var param : pl.getParameters().entrySet()) {
            Preconditions.isTrue(
                    (buf.position() - paramListStart) % 4 == 0,
                    "Invalid param alignment: " + param.getKey());
            writeShort(param.getKey().getValue());
            var len = LengthCalculator.getInstance().calculateParameterValueLength(param);
            writeShort((short) len);
            var endPos = buf.position() + len;
            if (param.getValue() instanceof Locator locator) writeLocator(locator);
            else writer.write(param.getValue());
            while (buf.position() < endPos) writeByte((byte) 0);
        }
        writeShort(ParameterId.PID_SENTINEL.getValue());
        writeShort((short) 0);
        Preconditions.isTrue(
                (buf.position() - paramListStart) % 4 == 0,
                "Invalid param alignment: PID_SENTINEL");
        LOGGER.exiting("writeParameterList");
    }

    private void writeLocator(Locator locator) throws Exception {
        LOGGER.entering("writeLocator");
        writeInt(locator.kind().value);
        writeInt(locator.port());
        switch (locator.kind()) {
            case LOCATOR_KIND_UDPv4:
                {
                    var buf = new byte[LengthCalculator.ADDRESS_SIZE];
                    System.arraycopy(locator.address().getAddress(), 0, buf, 12, 4);
                    writeByteArray(buf);
                    break;
                }
            case LOCATOR_KIND_UDPv6:
                {
                    LOGGER.severe("LOCATOR_KIND_UDPv6 is not supported, writing empty address");
                    var buf = new byte[LengthCalculator.ADDRESS_SIZE];
                    writeByteArray(buf);
                    break;
                }
            default:
                // ignore
                break;
        }
        LOGGER.exiting("writeLocator");
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
        writeInt(set.numBits);
        for (var i : set.bitmap) {
            buf.putInt(XByte.reverseBitsInBytes(i));
        }
        LOGGER.exiting("writeSequenceNumberSet");
    }

    public void writeEntityId(EntityId entiyId) throws Exception {
        LOGGER.entering("writeEntityId");
        writeByte((byte) (entiyId.entityKey >> 16));
        writeByte((byte) (entiyId.entityKey >> 8));
        writeByte((byte) entiyId.entityKey);
        writeByte(entiyId.entityKind);
        LOGGER.exiting("writeEntityId");
    }

    @Override
    public void writeStringArray(String[] arg0) throws Exception {
        throw new UnsupportedOperationException();
    }
}