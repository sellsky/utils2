package tk.bolovsrol.utils.binaryserializer;

/** Контейнер для указателя. */
public interface PosContainer {

    int getPos();

    int getPosAndInc();

    int getPosAndInc(int delta);

    void setPos(int pos);

    void incPos();
}
