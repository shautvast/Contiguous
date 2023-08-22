package com.github.shautvast.contiguous;

import java.nio.ByteBuffer;

/**
 * Writes integers to byte representation like Sqlite's putVarint64
 * not threadsafe (take out B8 and B9 if you need that)
 */
public final class Varint {

    private Varint() {
    }

    public static byte[] write(long v) {
        if ((v & ((0xff000000L) << 32)) != 0) {
            byte[] result = new byte[9];
            result[8] = (byte) v;
            v >>= 8;
            for (int i = 7; i >= 0; i--) {
                result[i] = (byte) ((v & 0x7f) | 0x80);
                v >>= 7;
            }
            return result;
        } else {
            int n;
            byte[] buf = new byte[8];
            for (n = 0; v != 0; n++, v >>= 7) {
                buf[n] = (byte) ((v & 0x7f) | 0x80);
            }
            buf[0] &= 0x7f;
            byte[] result = new byte[n];
            for (int i = 0, j = n - 1; j >= 0; j--, i++) {
                result[i] = buf[j];
            }
            return result;
        }
    }

    /*
     * read a long value from a variable nr of bytes in varint format
     *
     * copied from the sqlite source, with some java specifics, most notably the addition of
     * &0xFF for the right conversion from byte => signed in java, but to be interpreted as unsigned,
     * to long
     *
     * Does not have the issue that the read(byte[] bytes) method has. The nr of bytes read is determined
     * by the varint64 format.
     */
    public static long read(ByteBuffer buffer) {
        int SLOT_2_0 = 0x001fc07f;
        int SLOT_4_2_0 = 0xf01fc07f;

        long a = buffer.get() & 0xFF;
        if ((a & 0x80) == 0) {
            return a;
        }

        long b = buffer.get() & 0xFF;
        if ((b & 0x80) == 0) {
            a &= 0x7F;
            a = a << 7;
            a |= b;
            return a;
        }

        a = a << 14;
        a |= (buffer.get() & 0xFF);
        if ((a & 0x80) == 0) {
            a &= SLOT_2_0;
            b &= 0x7F;
            b = b << 7;
            a |= b;
            return a;
        }

        a &= SLOT_2_0;
        b = b << 14;
        b |= (buffer.get() & 0xFF);
        if ((b & 0x80) == 0) {
            b &= SLOT_2_0;
            a = a << 7;
            a |= b;
            return a;
        }

        b &= SLOT_2_0;
        long s = a;
        a = a << 14;
        int m = buffer.get() & 0xFF;
        a |= m;
        if ((a & 0x80) == 0) {
            b = b << 7;
            a |= b;
            s = s >> 18;
            return (s << 32) | a;
        }

        s = s << 7;
        s |= b;
        b = b << 14;
        b |= (buffer.get() & 0xFF);
        if ((b & 0x80) == 0) {
            a &= SLOT_2_0;
            a = a << 7;
            a |= b;
            s = s >> 18;
            return (s << 32) | a;
        }

        a = a << 14;
        a |= (buffer.get() & 0xFF);
        if ((a & 0x80) == 0) {
            a &= SLOT_4_2_0;
            b &= SLOT_2_0;
            b = b << 7;
            a |= b;
            s = s >> 11;
            return (s << 32) | a;
        }

        a &= SLOT_2_0;
        b = b << 14;
        b |= (buffer.get() & 0xFF);
        if ((b & 0x80) == 0) {
            b &= SLOT_4_2_0;
            a = a << 7;
            a |= b;
            s = s >> 4;
            return (s << 32) | a;
        }

        a = a << 15;
        a |= (buffer.get() & 0xFF);
        b &= SLOT_2_0;

        b = b << 8;
        a |= b;
        s = s << 4;
        b = m;
        b &= 0x7F;
        b = b >> 3;
        s |= b;
        return (s << 32) | a;
    }
}
