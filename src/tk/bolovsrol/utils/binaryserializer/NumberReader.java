package tk.bolovsrol.utils.binaryserializer;

public interface NumberReader {
    int readFrom(byte[] buf, PosContainer pos);

    NumberReader BYTE_NUMBER_READER = (buf, pos) -> buf[pos.getPosAndInc()];

    NumberReader SHORT_NUMBER_READER = (buf, pos) -> (buf[pos.getPosAndInc()] & 0xff) << 8 | buf[pos.getPosAndInc()] & 0xff;

}
