package bankapp.auth.application.shared.service;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ByteArrayUtil {

    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
