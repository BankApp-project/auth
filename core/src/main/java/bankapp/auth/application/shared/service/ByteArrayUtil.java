package bankapp.auth.application.shared.service;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ByteArrayUtil {

    /**
     * Converts a {@link UUID} to a byte array representation.
     *
     * @param uuid the UUID to be converted into a byte array
     * @return a byte array of length 16 containing the most significant and least significant bits of the UUID
     */
    public static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID bytesToUuid(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be exactly 16 bytes long");
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long mostSigBits = bb.getLong();
        long leastSigBits = bb.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public static byte[] intToBytes(int value) {
        var buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }
}
