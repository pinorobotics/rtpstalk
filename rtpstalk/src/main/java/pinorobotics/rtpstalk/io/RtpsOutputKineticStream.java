package pinorobotics.rtpstalk.io;

import java.nio.ByteBuffer;

import id.kineticstreamer.KineticStreamWriter;
import id.kineticstreamer.OutputKineticStream;
import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.dto.Locator;
import pinorobotics.rtpstalk.dto.submessages.Data;
import pinorobotics.rtpstalk.dto.submessages.Submessage;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterList;

public class RtpsOutputKineticStream implements OutputKineticStream {

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
            XAsserts.assertTrue(buf.position() % 4 == 0, "Invalid submessage alignment");
            if (a[i] instanceof Data data)
                writeData(data);
            else
                writer.write(a[i]);
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
    public void writeIntArray(int[] arg0) throws Exception {
        throw new UnsupportedOperationException();
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
        writeInt(str.length());
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
        for (var param : pl.getParameters()) {
            XAsserts.assertTrue((buf.position() - paramListStart) % 4 == 0,
                    "Invalid param alignment: " + param.parameterId());
            writeShort(param.parameterId().getValue());
            var len = LengthCalculator.getInstance().calculateParameterValueLength(param);
            writeShort((short) len);
            var endPos = buf.position() + len;
            if (param.value() instanceof Locator locator)
                writeLocator(locator);
            else
                writer.write(param.value());
            while (buf.position() < endPos)
                writeByte((byte) 0);
        }
        writeShort(ParameterId.PID_SENTINEL.getValue());
        writeShort((short) 0);
        XAsserts.assertTrue((buf.position() - paramListStart) % 4 == 0, "Invalid param alignment: PID_SENTINEL");
        LOGGER.exiting("writeParameterList");
    }

    private void writeLocator(Locator locator) throws Exception {
        LOGGER.entering("writeLocator");
        writeInt(locator.kind().value);
        writeInt(locator.port());
        switch (locator.kind()) {
        case LOCATOR_KIND_UDPv4: {
            var buf = new byte[LengthCalculator.ADDRESS_SIZE];
            System.arraycopy(locator.address().getAddress(), 0, buf, 12, 4);
            writeByteArray(buf);
            break;
        }
        case LOCATOR_KIND_UDPv6: {
            LOGGER.severe("LOCATOR_KIND_UDPv6 is not supported, writing empty address");
            var buf = new byte[LengthCalculator.ADDRESS_SIZE];
            writeByteArray(buf);
            break;
        }
        }
        LOGGER.exiting("writeLocator");
    }
}