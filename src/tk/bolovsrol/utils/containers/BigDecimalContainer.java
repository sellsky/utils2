package tk.bolovsrol.utils.containers;

import java.math.BigDecimal;

public interface BigDecimalContainer extends NumberContainer<BigDecimal> {

    void setValue(long value, int scale) throws ArithmeticException;

    @Override void setValue(BigDecimal value) throws ArithmeticException;

    @Override default int signum() {return getValue().signum();}

    default void addValue(BigDecimal item) { setValue(isValueNull() ? item : getValue().add(item)); }

    default void subValue(BigDecimal subtrahend) { setValue(isValueNull() ? subtrahend.negate() : getValue().subtract(subtrahend)); }
}