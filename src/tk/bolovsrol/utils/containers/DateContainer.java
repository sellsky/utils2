package tk.bolovsrol.utils.containers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public interface DateContainer extends MillisContainer<Date> {

    void setValue(Instant value);

    Instant getValueInstant();

    String valueToString(SimpleDateFormat format);

    void parseValue(String value, SimpleDateFormat format) throws ValueParsingException;

}
