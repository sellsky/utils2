package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Три старших бита (7..5) означают расположение значащих битов (big-endian):<br/>
 * 0x00 — в битах 4..0 этого байта,<br/>
 * 0x20 — в битах 4..0 этого байта и в следующем байте,<br/>
 * 0x40 — в битах 4..0 этого байта и в двух следующих байтах,<br/>
 * ...<br/>
 * 0xc0 — в битах 4..0 этого байта и в шести следующих байтах,<br/>
 * 0xe0 — если хотя бы один бит 4..0 этого байта установлен, то в них и семи следующих байтах, иначе в восьми следующих байтах.
 */
public class LongFieldValueCodec implements FieldValueCodec<Long> {

    @Override public void encodeValue(OutputStream target, Long value) throws IOException {
        long val = value.longValue();
        int lzc = Long.numberOfLeadingZeros(val);
        if (lzc >= 59) {
            target.write((int) val);
        } else if (lzc >= 51) {
            target.write((int) (val >> 8) | 0x20);
            target.write((int) val);
        } else if (lzc >= 43) {
            target.write((int) (val >> 16) | 0x40);
            target.write((int) (val >> 8));
            target.write((int) val);
        } else if (lzc >= 35) {
            target.write((int) (val >> 24) | 0x60);
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        } else if (lzc >= 27) {
            target.write((int) (val >> 32) | 0x80);
            target.write((int) (val >> 24));
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        } else if (lzc >= 19) {
            target.write((int) (val >> 40) | 0xa0);
            target.write((int) (val >> 32));
            target.write((int) (val >> 24));
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        } else if (lzc >= 11) {
            target.write((int) (val >> 48) | 0xc0);
            target.write((int) (val >> 40));
            target.write((int) (val >> 32));
            target.write((int) (val >> 24));
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        } else if (lzc >= 3) {
            target.write((int) (val >> 56) | 0xe0);
            target.write((int) (val >> 48));
            target.write((int) (val >> 40));
            target.write((int) (val >> 32));
            target.write((int) (val >> 24));
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        } else {
            target.write(0xe0);
            target.write((int) (val >> 56));
            target.write((int) (val >> 48));
            target.write((int) (val >> 40));
            target.write((int) (val >> 32));
            target.write((int) (val >> 24));
            target.write((int) (val >> 16));
            target.write((int) (val >> 8));
            target.write((int) val);
        }
    }

    @Override public Long decodeValue(byte[] source, PosContainer posContainer, Class<? extends Long> type) {
        int ref = source[posContainer.getPosAndInc()];
        long val;
        switch (ref & 0xe0) {
        case 0x00:
            val = ref & 0x1fL;
            break;
        case 0x20:
            val = ((ref & 0x1fL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0x40:
            val = ((ref & 0x1fL) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0x60:
            val = ((ref & 0x1fL) << 24)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0x80:
            val = ((ref & 0x1fL) << 32)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 24)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0xa0:
            val = ((ref & 0x1fL) << 40)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 32)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 24)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0xc0:
            val = ((ref & 0x1fL) << 48)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 40)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 32)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 24)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xffL);
            break;
        case 0xe0:
            int highVal = ref & 0x1f;
            if (highVal != 0) {
                val = ((long) highVal << 56)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 48)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 40)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 32)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 24)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                        | (source[posContainer.getPosAndInc()] & 0xffL);
            } else {
                val = ((long) source[posContainer.getPosAndInc()] << 56)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 48)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 40)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 32)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 24)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 16)
                        | ((source[posContainer.getPosAndInc()] & 0xffL) << 8)
                        | (source[posContainer.getPosAndInc()] & 0xffL);
            }
            break;
        default:
            throw new RuntimeException("OMG! This won't happen!");
        }
        return Long.valueOf(val);
    }
}
