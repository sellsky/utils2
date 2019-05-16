package tk.bolovsrol.utils.textformatter.compiling.modifiers;

import tk.bolovsrol.utils.textformatter.compiling.EvaluationFailedException;
import tk.bolovsrol.utils.textformatter.compiling.ValueModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.IntContainer;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.containers.StringContainer;
import tk.bolovsrol.utils.textformatter.compiling.sections.Section;

/**
 * Отдаёт часть строки, ограничиваемую модификатором.
 * <p/>
 * Параметры:
 * - разделитель;
 * - результат, если разделитель не найден, по умолчанию пустая строка;
 * - позиция, с которой искать, по умолчанию соответствующий направлению край строки.
 */
class SubstringByDelimiterModifier implements ValueModifier {
    private final boolean until;
    private final boolean first;

    private final StringContainer specimenCont = new StringContainer();
    private final StringContainer ifEmptyCont = new StringContainer();
    private final IntContainer fromPosCont = new IntContainer();

    /**
     * @param until true: с начала строки до разделителя, false: от разделителя до конца строки.
     * @param first true: поиск слева направо, false: поиск справа налево.
     */
    SubstringByDelimiterModifier(boolean until, boolean first) {
        this.until = until;
        this.first = first;
    }

    @Override
    public int getMinParameterCount() {
        return 1;
    }

    @Override
    public int getMaxParameterCount() {
        return 3;
    }

    @Override
    public String eval(String source, Section[] parameters, boolean strict) throws EvaluationFailedException {
        String specimen = specimenCont.get(parameters[0], strict);
        int fromPos = parameters.length > 2 ? fromPosCont.get(parameters[2], strict) : first ? 0 : -1;

        if (fromPos < 0) {
            fromPos = source.length() + fromPos;
        }

        int index = first ? source.indexOf(specimen, fromPos) : source.lastIndexOf(specimen, fromPos);
        if (index < 0) {
            return parameters.length > 1 ? ifEmptyCont.get(parameters[1], strict) : "";
        } else {
            return until ? source.substring(0, index) : source.substring(index + specimen.length());
        }
    }


//    public static void main(String[] args) throws EvaluationFailedException {
//        SubstringByDelimiterModifier z = new SubstringByDelimiterModifier(false, false);
//
//        String source = "trulala.bobobo.xls";
//
//        Section[] parameters = {
//                new ConstSection("."),
//                new ConstSection("fukaka")
//        };
//
//        System.out.println(z.eval(source, parameters, true));
//
//    }
}
