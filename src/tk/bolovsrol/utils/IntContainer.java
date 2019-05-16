package tk.bolovsrol.utils;

public class IntContainer {

    int value;

    public IntContainer() {
    }

    public IntContainer(int value) {
        this.value = value;
    }

    @Override public String toString() {
        return String.valueOf(value);
    }

    public int get() {
        return this.value;
    }

    public void set(int value) {
        this.value = value;
    }

    public int addAndGet(int delta) {
        this.value += delta;
        return this.value;
    }

    public int getAndAdd(int delta) {
        int result = this.value;
        this.value += delta;
        return result;
    }

    public int incAndGet() {
        return ++this.value;
    }

    public int incrementAndGet() {
        return ++this.value;
    }

    public int getAndInc() {
        return this.value++;
    }

    public int getAndIncrement() {
        return this.value++;
    }

    public int DecAndGet() {
        return --this.value;
    }

    public int DecrementAndGet() {
        return --this.value;
    }

    public int getAndDec() {
        return this.value--;
    }

    public int getAndDecrement() {
        return this.value--;
    }

    public int maxAndGet(int candidate) {
        if (this.value < candidate) {
            this.value = candidate;
        }
        return this.value;
    }

    public int minAndGet(int candidate) {
        if (this.value > candidate) {
            this.value = candidate;
        }
        return this.value;
    }
}
