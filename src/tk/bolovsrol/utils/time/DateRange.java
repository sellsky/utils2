package tk.bolovsrol.utils.time;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.Date;

/** Интервал дат. */
public class DateRange {

    private Date since;
    private Date until;

    public DateRange() {
    }

    public DateRange(Date since, Date until) {
        this.since = since;
        this.until = until;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public long getDuration() {
        return until.getTime() - since.getTime();
    }

    public String toString() {
        return new StringDumpBuilder()
                .append("since", since)
                .append("until", until)
                .toString();
    }
}
