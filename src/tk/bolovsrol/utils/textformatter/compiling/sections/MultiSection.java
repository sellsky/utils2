package tk.bolovsrol.utils.textformatter.compiling.sections;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;

import java.util.List;

/** Последовательность нескольких секций. */
public class MultiSection implements Section {

    private final List<Section> sections;

    private String value;

    public MultiSection(List<Section> sections) {
        this.sections = sections;
    }

    @Override
    public boolean isConstant() {
        // мультисекция состоит как минимум из одной непостоянной секции, так что не будем рыскать
        return false;
    }

    @Override
    public String evaluate(boolean strict) throws EvaluationFailedException {
        if (value == null) {
            StringBuilder sb = new StringBuilder(32 * sections.size());
            for (Section section : sections) {
                sb.append(section.evaluate(strict));
            }
            value = sb.toString();
        }
        return value;
    }

    @Override
    public void reset() {
        value = null;
        for (Section section : sections) {
            section.reset();
        }
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof MultiSection && this.sections.equals(((MultiSection) that).sections);
    }

    @Override
    public int hashCode() {
        return sections.hashCode();
    }

    @Override public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("multi{");
        boolean first = true;
        for (Section section : sections) {
            if (!first) {
                sb.append(',');
            }
            sb.append(section.toString());
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }

}
