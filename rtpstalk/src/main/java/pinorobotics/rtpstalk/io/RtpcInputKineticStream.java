package pinorobotics.rtpstalk.io;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import id.kineticstreamer.InputKineticStream;
import id.kineticstreamer.KineticStreamReader;
import id.xfunction.XAsserts;
import id.xfunction.logging.XLogger;
import pinorobotics.rtpstalk.dto.submessages.BuiltinEndpointSet;
import pinorobotics.rtpstalk.dto.submessages.Duration;
import pinorobotics.rtpstalk.dto.submessages.Guid;
import pinorobotics.rtpstalk.dto.submessages.Locator;
import pinorobotics.rtpstalk.dto.submessages.LocatorKind;
import pinorobotics.rtpstalk.dto.submessages.UserDataQosPolicy;
import pinorobotics.rtpstalk.dto.submessages.elements.GuidPrefix;
import pinorobotics.rtpstalk.dto.submessages.elements.Parameter;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterId;
import pinorobotics.rtpstalk.dto.submessages.elements.ParameterList;
import pinorobotics.rtpstalk.dto.submessages.elements.ProtocolVersion;
import pinorobotics.rtpstalk.dto.submessages.elements.VendorId;

public class RtpcInputKineticStream implements InputKineticStream {

	private static final XLogger LOGGER = XLogger.getLogger(RtpcInputKineticStream.class);
	private static final short PID_SENTINEL = 0x1;
	private ByteBuffer buf;
	private KineticStreamReader reader = new KineticStreamReader(this);

	public RtpcInputKineticStream(ByteBuffer buf) {
		this.buf = buf;
	}

	@Override
	public void close() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] readArray(Object[] a, Class<?> type) throws Exception {
//        LOGGER.entering("readArray");
//        if (a.length == 0) {
//        	a = (Object[])Array.newInstance(type, (int) readLong());
//        }
//        for (int i = 0; i < a.length; i++) {
//            a[i] = new KineticStreamReader(this).read(type);
//        }
//        LOGGER.exiting("readArray");
//        return a;
		throw new UnsupportedOperationException();        
	}

	@Override
	public boolean readBool() throws Exception {
		LOGGER.entering("readBool");
		var ret = readByte() != 0;
		LOGGER.exiting("readBool");
		return ret;
	}

	@Override
	public boolean[] readBooleanArray(boolean[] a) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte readByte() throws Exception {
		LOGGER.entering("readByte");
		var ret = buf.get();
		LOGGER.exiting("readByte");
		return ret;
	}

	@Override
	public byte[] readByteArray(byte[] a) throws Exception {
		LOGGER.entering("readByteArray");
        for (int i = 0; i < a.length; i++) {
            a[i] = readByte();
        }
        LOGGER.exiting("readByteArray");
        return a;
	}

	@Override
	public double readDouble() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public double[] readDoubleArray(double[] a) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public float readFloat() throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readInt() throws Exception {
		LOGGER.entering("readInt");
		var ret = Integer.reverseBytes(buf.getInt());
		LOGGER.exiting("readInt");
		return ret;
	}

	@Override
	public int[] readIntArray(int[] a) throws Exception {
		LOGGER.entering("readIntArray");
        for (int i = 0; i < a.length; i++) {
            a[i] = readInt();
        }
        LOGGER.exiting("readIntArray");
        return a;
	}

	@Override
	public String readString() throws Exception {
		var strBuf = new StringBuilder();
		byte b = 0;
		while ((b = buf.get()) != 0) strBuf.append(b); 
		return strBuf.toString();
	}

	@Override
	public long readLong() throws Exception {
		LOGGER.entering("readLong");
		var ret = Long.reverseBytes(buf.getLong());
		LOGGER.exiting("readLong");
		return ret;
	}

	@Override
	public long[] readLongArray(long[] a) throws Exception {
		throw new UnsupportedOperationException();
//		LOGGER.entering("readLongArray");
//        for (int i = 0; i < a.length; i++) {
//            a[i] = readLong();
//        }
//        LOGGER.exiting("readLongArray");
//        return a;
	}

	@Override
	public short readShort() throws Exception {
		LOGGER.entering("readShort");
		var ret = Short.reverseBytes(buf.getShort());
		LOGGER.exiting("readShort");
		return ret;
	}

	@Override
	public short[] readShortArray(short[] a) throws Exception {
		LOGGER.entering("readShortArray");
        for (int i = 0; i < a.length; i++) {
            a[i] = readShort();
        }
        LOGGER.exiting("readShortArray");
        return a;
	}
	
	public ParameterList readParameterList() throws Exception {
		LOGGER.entering("readParameterList");
		var params = new ArrayList<Parameter>();
		short id;
		while ((id = readShort()) != PID_SENTINEL) {
			var parameterId = ParameterId.map.get(id);
			var len = readShort();
			var startPos = buf.position();
			if (parameterId == null) {
				LOGGER.warning("Unknown parameter {0}, ignoring...", Short.toUnsignedInt(id));
				skip(len);
				continue;
			}
			Object value = null;
			switch (parameterId) {
			case PID_ENTITY_NAME: value = reader.read(String.class); break;
			case PID_BUILTIN_ENDPOINT_SET: value = reader.read(BuiltinEndpointSet.class); break;
			case PID_PARTICIPANT_LEASE_DURATION: value = reader.read(Duration.class); break;
			case PID_DEFAULT_UNICAST_LOCATOR:
			case PID_METATRAFFIC_UNICAST_LOCATOR: value = readLocator(); break;
			case PID_PARTICIPANT_GUID: value = reader.read(Guid.class); break;
			case PID_PROTOCOL_VERSION: value = reader.read(ProtocolVersion.class); break;
			case PID_VENDORID: value = reader.read(VendorId.class); break;
			case PID_USER_DATA: value = reader.read(UserDataQosPolicy.class); break;
			default: throw new UnsupportedOperationException("Parameter id " + id);
			}
			skip(len - (buf.position() - startPos));
			LOGGER.fine(parameterId + ": " + value);
			params.add(new Parameter(parameterId, value));
		}
		// ignoring
		readShort();
		LOGGER.exiting("readParameterList");
		return new ParameterList(params);
	}
	
	private void skip(int offset) {
		XAsserts.assertTrue(offset >= 0, "Negative offset");
		LOGGER.fine("Skipping {0} bytes", offset);
		buf.position(buf.position() + offset);
	}

	@Override
	public List readList(List list, Class<?> genericType)  throws Exception {
		var out = new ArrayList<>();
		var len = readInt();
		for (int i = 0; i < len; i++) {
			out.add(reader.read(genericType));
		}
		return out;
	}

	public Locator readLocator() throws Exception {
		var kind = LocatorKind.VALUES.getOrDefault(readInt(), LocatorKind.LOCATOR_KIND_INVALID);
		var port = readInt();
		var buf = new byte[16];
		readByteArray(buf);
		var address = "";
		switch (kind) {
		case LOCATOR_KIND_UDPv4: address = Stream.of(buf[12], buf[13], buf[14], buf[15])
				.map(Byte::toUnsignedInt)
				.map(Object::toString)
				.collect(Collectors.joining("."));
		}
		return new Locator(kind, port, address);
	}
	
}
