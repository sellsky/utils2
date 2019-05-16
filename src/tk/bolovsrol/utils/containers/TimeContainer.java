package tk.bolovsrol.utils.containers;

import tk.bolovsrol.utils.time.Duration;

import java.sql.Time;
import java.time.LocalTime;

/** Created by andrew.cherepivsky */
public interface TimeContainer extends MillisContainer<Time> {

	void setValue(LocalTime value);

	LocalTime getValueLocalTime();

	void setValue(Duration value);

	Duration getValueDuration();

}
