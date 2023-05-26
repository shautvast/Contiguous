package nl.sanderhautvast.contiguous;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/*
 * Reads a value from the storage
 * the layout is SQLite-like type:Varint, value byte[]
 *                           Varint: byte[]
 */
class ValueReader {

    /**
     * Reads a value from the buffer.
     *
     * @param buffer Bytebuffer containing the storage.
     *
     * //TODO can we make a typesafe read method? I think so
     */
    public static Object read(ByteBuffer buffer) {
        long type = Varint.read(buffer);
        return read(buffer, type);
    }

    /**
     * Reads a value from the buffer
     *
     * @param buffer     Bytebuffer containing the storage.
     * @param columnType type representation borrowed from SQLite
     *
     * @return the value implementation
     */
    private static Object read(ByteBuffer buffer, long columnType) {
        if (columnType == 0) {
            return null;
        } else if (columnType < 6L) {
            byte[] integerBytes = new byte[getvalueLengthForType(columnType)];
            buffer.get(integerBytes);
            return bytesToLong(integerBytes);
        } else if (columnType == 7) {
            return buffer.getDouble();
        } else if (columnType == 8) {
            return 0L; //has to be long!
        } else if (columnType == 9) {
            return 1L;
        } else if (columnType == 10) {
            return buffer.getFloat();
        } else if (columnType >= 12 && columnType % 2 == 0) {
            byte[] bytes = new byte[getvalueLengthForType(columnType)];
            buffer.get(bytes);
            return bytes;
        } else if (columnType >= 13) {
            byte[] bytes = new byte[getvalueLengthForType(columnType)];
            buffer.get(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } else throw new IllegalStateException("unknown column type" + columnType);
    }

    private static int getvalueLengthForType(long columnType) {
        // can't switch on long
        if (columnType == 0 || columnType == 8 || columnType == 9) {
            return 0;
        } else if (columnType < 5) {
            return (int) columnType;
        } else if (columnType == 5) {
            return 6;
        } else if (columnType == 6 || columnType == 7) {
            return 8;
        } else if (columnType < 12) {
            return -1;
        } else {
            if (columnType % 2 == 0) {
                return (int) ((columnType - 12) >> 1);
            } else {
                return (int) ((columnType - 13) >> 1);
            }
        }
    }

    public static long bytesToLong(final byte[] b) {
        long n = 0;
        for (int i = 0; i < b.length; i++) {
            byte v = b[i];
            int shift = ((b.length - i - 1) * 8);
            if (i == 0 && (v & 0x80) != 0) {
                n -= (0x80L << shift);
                v &= 0x7f;
            }
            n += ((long)(v&0xFF)) << shift;
        }
        return n;
    }
}
