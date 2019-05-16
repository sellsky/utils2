package tk.bolovsrol.utils.binaryserializer.fieldcodec;

import tk.bolovsrol.utils.binaryserializer.FieldValueCodec;
import tk.bolovsrol.utils.binaryserializer.PosContainer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Два старших бита (7 и 6) означают расположение значащих битов (big-endian):<br/>
 * 0x00 — в битах 5..0 этого байта,<br/>
 * 0x40 — в битах 5..0 этого байта и в следующем байте,<br/>
 * 0x80 — в битах 5..0 этого байта и в двух следующих байтах,<br/>
 * 0xc0 — если хотя бы один бит 5..0 этого байта установлен, то в них и в трёх следующих байтах, иначе в четырёх следующих байтах.
 */
public class IntegerFieldValueCodec implements FieldValueCodec<Integer> {

    @Override public void encodeValue(OutputStream target, Integer value) throws IOException {
        int val = value.intValue();
        int lzc = Integer.numberOfLeadingZeros(val);
        if (lzc >= 26) {
            target.write(val);
        } else if (lzc >= 18) {
            target.write((val >> 8) | 0x40);
            target.write(val);
        } else if (lzc >= 10) {
            target.write((val >> 16) | 0x80);
            target.write(val >> 8);
            target.write(val);
        } else if (lzc >= 2) {
            target.write((val >> 24) | 0xc0);
            target.write(val >> 16);
            target.write(val >> 8);
            target.write(val);
        } else {
            target.write(0xc0);
            target.write(val >> 24);
            target.write(val >> 16);
            target.write(val >> 8);
            target.write(val);
        }
    }

    @Override public Integer decodeValue(byte[] source, PosContainer posContainer, Class<? extends Integer> type) {
        int ref = source[posContainer.getPosAndInc()];
        int val;
        switch (ref & 0xC0) {
        case 0x00:
            val = ref & 0x3f;
            break;
        case 0x40:
            val = ((ref & 0x3f) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xff);
            break;
        case 0x80:
            val = ((ref & 0x3f) << 16)
                    | ((source[posContainer.getPosAndInc()] & 0xff) << 8)
                    | (source[posContainer.getPosAndInc()] & 0xff);
            break;
        case 0xC0:
            int highVal = ref & 0x3f;
            if (highVal != 0) {
                val = (highVal << 24)
                        | ((source[posContainer.getPosAndInc()] & 0xff) << 16)
                        | ((source[posContainer.getPosAndInc()] & 0xff) << 8)
                        | (source[posContainer.getPosAndInc()] & 0xff);
            } else {
                val = (source[posContainer.getPosAndInc()] << 24)
                        | ((source[posContainer.getPosAndInc()] & 0xff) << 16)
                        | ((source[posContainer.getPosAndInc()] & 0xff) << 8)
                        | (source[posContainer.getPosAndInc()] & 0xff);
            }
            break;
        default:
            throw new RuntimeException("OMG! This won't happen!");
        }

        return Integer.valueOf(val);
    }

}
