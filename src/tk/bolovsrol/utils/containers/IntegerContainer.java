package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.MathUtils;
import tk.bolovsrol.utils.box.Box;

public interface IntegerContainer extends NumberContainer<Integer> {

    @Override default int signum() {return Integer.signum(getValue());}

    //-- несколько арифметических действий над полем
    default void incValue() {
        setValue(MathUtils.inc(Box.with(getValue()).getOr(0)));
    }

    default void decValue() {
        setValue(MathUtils.dec(Box.with(getValue()).getOr(0)));
    }

    default void addValue(Number item) {
        setValue(MathUtils.add(Box.with(getValue()).getOr(0), item));
    }

    default void subValue(Number subtrahend) {
        setValue(MathUtils.sub(Box.with(getValue()).getOr(0), subtrahend));
    }

    default void mulValue(Number multiplier) {
        setValue(MathUtils.mul(Box.with(getValue()).getOr(0), multiplier));
    }

    default void divValue(Number divisor) {
        setValue(MathUtils.div(Box.with(getValue()).getOr(0), divisor));
    }

    default void modValue(Number divisor) {
        setValue(MathUtils.mod(Box.with(getValue()).getOr(0), divisor));
    }

    default void addValue(int item) {
        setValue(MathUtils.add(Box.with(getValue()).getOr(0), item));
    }

    default void subValue(int subtrahend) {
        setValue(MathUtils.sub(Box.with(getValue()).getOr(0), subtrahend));
    }

    default void mulValue(int multiplier) {
        setValue(MathUtils.mul(Box.with(getValue()).getOr(0), multiplier));
    }

    default void divValue(int divisor) {
        setValue(MathUtils.div(Box.with(getValue()).getOr(0), divisor));
    }

    default void modValue(int divisor) {
        setValue(MathUtils.mod(Box.with(getValue()).getOr(0), divisor));
    }

    default void andValue(int mask) {
        setValue(Box.with(getValue()).getOr(0) & mask);
    }

    default void orValue(int mask) {
        setValue(Box.with(getValue()).getOr(0) | mask);
    }
}
