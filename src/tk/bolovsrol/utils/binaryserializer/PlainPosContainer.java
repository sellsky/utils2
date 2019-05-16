package tk.bolovsrol.utils.binaryserializer;

class PlainPosContainer implements PosContainer {
    private int i = 0;

    @Override public int getPos() {
        return i;
    }

    @Override public int getPosAndInc() {
        return i++;
    }

    @Override public int getPosAndInc(int delta) {
        int pos = i;
        i += delta;
        return pos;
    }

    @Override public void setPos(int pos) {
        i = pos;
    }

    @Override public void incPos() {
        i++;
    }

    public String toString() {
        return Integer.toString(i) + " (" + Integer.toHexString(i) + ')';
    }
}
