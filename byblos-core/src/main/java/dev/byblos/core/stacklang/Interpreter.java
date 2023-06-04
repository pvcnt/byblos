package dev.byblos.core.stacklang;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import dev.byblos.core.util.Features;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Interpreter {
    private final List<Word> vocabulary;
    private final int vocabularySize;
    private final Map<String, List<Word>> words;

    /**
     * Constructor.
     *
     * @param words Set of supported words. If multiple words have the same name,
     *              then the first one that matches with the current stack will
     *              get used.
     */
    public Interpreter(List<Word> words) {
        this.vocabulary = List.copyOf(words);
        vocabularySize = words.size();
        this.words = words.stream().collect(Collectors.groupingBy(Word::name));
    }

    public record Step(List<Object> program, Context context) {
    }

    public List<Word> vocabulary() {
        return vocabulary;
    }

    public Context execute(String program) throws InvalidSyntaxException {
        return execute(splitAndTrim(program));
    }

    public Context execute(List<Object> program) throws InvalidSyntaxException {
        return execute(program, Context.initial(this));
    }

    public Context execute(String program, Map<String, Object> vars, Features features) throws InvalidSyntaxException {
        return execute(splitAndTrim(program), Context.initial(this, vars, features));
    }

    public Context execute(List<Object> program, Context context, boolean unfreeze) throws InvalidSyntaxException {
        var result = execute(new Step(program, context));
        return unfreeze ? result.unfreeze() : result;
    }

    public Context execute(List<Object> program, Context context) throws InvalidSyntaxException {
        return execute(program, context, true);
    }

    public List<Step> debug(String program) throws InvalidSyntaxException {
        return debug(splitAndTrim(program));
    }

    public List<Step> debug(List<Object> program) throws InvalidSyntaxException {
        return debug(program, Context.initial(this));
    }

    public List<Step> debug(List<Object> program, Context context) throws InvalidSyntaxException {
        var result = debugImpl(List.of(), new Step(program, context));
        if (result.isEmpty()) {
            return result;
        }
        return Lists.reverse(Stream.concat(Stream.of(new Step(result.get(0).program, result.get(0).context.unfreeze())), result.stream().skip(1)).collect(Collectors.toList()));
    }

    public List<Step> debugImpl(List<Step> steps, Step step) throws InvalidSyntaxException {
        var trace = Stream.concat(Stream.of(step), steps.stream()).collect(Collectors.toList());
        return step.program.isEmpty() ? trace : debugImpl(trace, nextStep(step));
    }

    @Override
    public String toString() {
        return String.format("Interpreter(%s words)", vocabularySize);
    }

    private Context execute(Step s) throws InvalidSyntaxException {
        return (s.program.isEmpty()) ? s.context : execute(nextStep(s));
    }

    private Step nextStep(Step s) throws InvalidSyntaxException {
        if (s.program.isEmpty()) {
            return s;
        }
        var token = s.program.get(0);
        var tail = s.program.subList(1, s.program.size());
        if ("(".equals(token)) {
            return popAndPushList(0, List.of(), new Step(tail, s.context));
        } else if (")".equals(token)) {
            throw new InvalidSyntaxException("unmatched closing parenthesis");
        } else if (token instanceof String && ((String) token).startsWith(":")) {
            return new Step(tail, executeWord(((String) token).substring(1), s.context));
        }
        return new Step(tail, s.context.withStack(s.context.stack().push(token)));
    }

    /**
     * Called with the remaining items from the program after an opening parenthesis is found. It
     * will push a list containing all items until the matching closing parenthesis. Commands inside
     * the list will not get executed.
     */
    private Step popAndPushList(int depth, List<Object> acc, Step step) throws InvalidSyntaxException {
        if (step.program.isEmpty()) {
            throw new InvalidSyntaxException("unmatched opening parenthesis");
        }
        var token = step.program.get(0);
        var tokens = step.program.subList(1, step.program.size());
        if ("(".equals(token)) {
            return popAndPushList(depth + 1, pushLeft("(", acc), new Step(tokens, step.context));
        } else if (")".equals(token) && depth == 0) {
            var stack = step.context.stack().push(Lists.reverse(acc));
            return new Step(tokens, step.context.withStack(stack));
        } else if (")".equals(token) && depth > 0) {
            return popAndPushList(depth - 1, pushLeft(")", acc), new Step(tokens, step.context));
        }
        return popAndPushList(depth, pushLeft(token, acc), new Step(tokens, step.context));
    }

    public static List<Object> pushLeft(Object token, List<Object> tokens) {
        return Stream.concat(Stream.of(token), tokens.stream()).collect(Collectors.toList());
    }

    private Context executeWord(String name, Context context) throws InvalidSyntaxException {
        var ws = words.get(name);
        if (null == ws) {
            throw new InvalidSyntaxException(String.format("unknown word ':%s'", name));
        }
        return executeFirstMatchingWord(name, ws, context);
    }

    private Context executeFirstMatchingWord(String name, List<Word> ws, Context context) throws InvalidSyntaxException {
        var res = executeFirstMatchingWordImpl(ws, context);
        if (res.isPresent()) {
            return res.get();
        }
        var stackSummary = Interpreter.typeSummary(context.stack());
        var candidates = ws.stream().map(Word::signature).collect(Collectors.joining("], [", "[", "]"));
        throw new InvalidSyntaxException(
                String.format("no matches for word ':%s' with stack %s, candidates: %s", name, stackSummary, candidates)
        );
    }

    private Optional<Context> executeFirstMatchingWordImpl(List<Word> ws, Context context) throws InvalidSyntaxException {
        if (ws.isEmpty()) {
            return Optional.empty();
        }
        var v = ws.get(0);
        if (v.matches(context.stack())) {
            if (!v.isStable() && context.features() != Features.UNSTABLE) {
                throw new InvalidSyntaxException(String.format("to use :%s enable unstable features", v.name()));
            }
            return Optional.of(v.execute(context));
        }
        return executeFirstMatchingWordImpl(ws.subList(1, ws.size()), context);
    }

    public static String typeSummary(Stack stack) {
        return stack.reverse()
                .stream()
                .map(Interpreter::getTypeName)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String getTypeName(Object v) {
        if (v instanceof List) {
            // Prevents internal implementations to surface, e.g., List12.
            return "List";
        }
        return v.getClass().getSimpleName();
    }

    private static final Map<Character, Character> blocks = Map.of('(', ')', '{', '}', '[', ']');

    public static List<Object> splitAndTrim(String str) throws InvalidSyntaxException {
        var stack = new LinkedList<Character>();
        var sb = new StringBuilder();
        var builder = ImmutableList.builder();
        var chars = str.toCharArray();
        for (var i = 0; i < chars.length; i++) {
            var c = chars[i];
            if (stack.isEmpty() && c == ',') {
                addIfNotEmpty(sb, builder);
                sb.setLength(0);
            } else {
                if (isParenCommand(chars, i)) {
                    // Do not fall in the following cases.
                } else if (blocks.containsKey(c)) {
                    stack.push(blocks.get(c));
                } else if (blocks.containsValue(c)) {
                    if (stack.isEmpty()) {
                        throw new InvalidSyntaxException(String.format("unmatched closing character '%s'", c));
                    }
                    var head = stack.pop();
                    if (head != c) {
                        throw new InvalidSyntaxException(String.format("unmatched closing character '%s'", c));
                    }
                }
                sb.append(c);
            }
        }
        if (!stack.isEmpty()) {
            throw new InvalidSyntaxException("unmatched opening character");
        }
        addIfNotEmpty(sb, builder);
        return builder.build();
    }

    private static boolean isParenCommand(char[] chars, int i) {
        return (chars[i] == '(' || chars[i] == ')')
                && (i == 0 || chars[i - 1] == ',')
                && (i >= chars.length - 1 || chars[i + 1] == ',');
    }

    private static void addIfNotEmpty(StringBuilder sb, ImmutableList.Builder<Object> builder) {
        var s = sb.toString().trim();
        if (!s.isEmpty()) {
            builder.add(s);
        }
    }
}
