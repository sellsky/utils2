package tk.bolovsrol.utils.textformatter.compiling;

import tk.bolovsrol.utils.textformatter.compiling.modifiers.AddModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.AfterFirstModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.AfterLastModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.ComparisonModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.DateFormatModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.Dec2HexModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.DivideModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.DurationFormatModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.Hex2DecModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfEmptyModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfNotEmptyModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfNotNumberModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfNotRegexModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfNumberModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IfRegexModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.IndexOfModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.LastIndexOfModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.LengthModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.LowercaseModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.MultiplyModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.NumberRoundModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.NumericCaseModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.NumericComparisonModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.NumericUniModifier;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.PaddingModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.RadixModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.ReplaceModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.SubstringModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.SubtractModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.TrimModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.UntilFirstModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.UntilLastModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.UppercaseModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.WithoutPrefixModifierFactory;
import tk.bolovsrol.utils.textformatter.compiling.modifiers.WithoutSuffixModifierFactory;

import java.util.Map;
import java.util.TreeMap;

/** Статический неизменяемый справочник модификаторов. */
final class CommonModifierFactories {

    static final Map<String, ValueModifierFactory> MODIFIER_FACTORIES = new TreeMap<String, ValueModifierFactory>();

    private CommonModifierFactories() {
    }

    static {
        register(new String[]{"=", "=="}, () -> ComparisonModifier.EQUALS);
        register(new String[]{"!=", "<>"}, () -> ComparisonModifier.NOT_EQUAL);
        register(new String[]{"eq"}, () -> NumericComparisonModifier.EQUALS);
        register(new String[]{"ne"}, () -> NumericComparisonModifier.NOT_EQUAL);
        register(new String[]{"gt"}, () -> NumericComparisonModifier.GT);
        register(new String[]{"lt"}, () -> NumericComparisonModifier.LT);
        register(new String[]{"ge"}, () -> NumericComparisonModifier.GE);
        register(new String[]{"le"}, () -> NumericComparisonModifier.LE);
        register(new String[]{"neg", "negate"}, () -> NumericUniModifier.NEGATE);
        register(new String[]{"abs", "absolute"}, () -> NumericUniModifier.ABSOLUTE);
        register(new String[]{"if-regex"}, new IfRegexModifierFactory());
        register(new String[]{"if-nregex"}, new IfNotRegexModifierFactory());
        register(new String[]{"if-emp", "if-empty", "?"}, new IfEmptyModifierFactory());
        register(new String[]{"if-nemp", "if-not-empty", "!?"}, new IfNotEmptyModifierFactory());
        register(new String[]{"if-num", "if-number"}, new IfNumberModifierFactory());
        register(new String[]{"if-nnum", "if-not-number"}, new IfNotNumberModifierFactory());
        register(new String[]{"index-of"}, new IndexOfModifierFactory());
        register(new String[]{"date", "time", "datetime"}, new DateFormatModifierFactory());
        register(new String[]{"dur", "duration"}, new DurationFormatModifierFactory());
        register(new String[]{"upper"}, new UppercaseModifierFactory());
        register(new String[]{"last-index-of"}, new LastIndexOfModifierFactory());
        register(new String[]{"lower"}, new LowercaseModifierFactory());
        register(new String[]{"#", "numcase"}, new NumericCaseModifierFactory());
        register(new String[]{"pad"}, new PaddingModifierFactory());
        register(new String[]{"trim"}, new TrimModifierFactory());
        register(new String[]{"substr"}, new SubstringModifierFactory());
        register(new String[]{"round"}, new NumberRoundModifierFactory());
        register(new String[]{"radix"}, new RadixModifierFactory());
        register(new String[]{"~", "replace"}, new ReplaceModifierFactory());
        register(new String[]{"hex2dec"}, new Hex2DecModifierFactory());
        register(new String[]{"dec2hex"}, new Dec2HexModifierFactory());
        register(new String[]{"+", "add"}, new AddModifierFactory());
        register(new String[]{"-", "sub"}, new SubtractModifierFactory());
        register(new String[]{"*", "mul"}, new MultiplyModifierFactory());
        register(new String[]{"/", "div"}, new DivideModifierFactory());
        register(new String[]{"len", "length"}, new LengthModifierFactory());
        register(new String[]{"without-suffix"}, new WithoutSuffixModifierFactory());
        register(new String[]{"without-prefix"}, new WithoutPrefixModifierFactory());

        register(new String[]{"until-first"}, new UntilFirstModifierFactory());
        register(new String[]{"until-last"}, new UntilLastModifierFactory());
        register(new String[]{"after-first"}, new AfterFirstModifierFactory());
        register(new String[]{"after-last"}, new AfterLastModifierFactory());
    }

    private static void register(String tag, ValueModifierFactory functionModifierFactory) {
        MODIFIER_FACTORIES.put(tag, functionModifierFactory);
        MODIFIER_FACTORIES.put(tag.toLowerCase(), functionModifierFactory);
        MODIFIER_FACTORIES.put(tag.toUpperCase(), functionModifierFactory);
    }

    private static void register(String[] tags, ValueModifierFactory functionModifierFactory) {
        for (String tag : tags) {
            register(tag, functionModifierFactory);
        }
    }

}
