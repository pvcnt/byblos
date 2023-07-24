package dev.byblos.webapi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import dev.byblos.model.Expr;
import dev.byblos.model.StyleExpr;
import dev.byblos.model.TypeUtils;
import dev.byblos.stacklang.*;
import dev.byblos.util.Strings;
import dev.byblos.eval.graph.DefaultSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.byblos.model.TypeUtils.isPresentationType;

@Controller
public final class ExprResource {
    private final Vocabulary vocabulary;
    private final Map<String, Vocabulary> vocabularies;
    private final Set<String> excludedWords;

    @Autowired
    public ExprResource(ApiSettings apiSettings, DefaultSettings defaultSettings) {
        vocabulary = defaultSettings.vocabulary();
        var vocabularies = ImmutableMap.<String, Vocabulary>builder();
        vocabulary.dependencies().forEach(v -> vocabularies.put(v.name(), v));
        vocabularies.put(vocabulary.name(), vocabulary);
        this.vocabularies = vocabularies.build();
        excludedWords = apiSettings.excludedWords();
    }

    @ExceptionHandler({InvalidSyntaxException.class, IllegalArgumentException.class})
    public ResponseEntity<?> handleException(Throwable e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @GetMapping("/api/v1/expr/debug")
    public ResponseEntity<?> debug(
            @RequestParam("q") String query,
            @RequestParam(value = "vocab", required = false) String vocabName
    ) throws InvalidSyntaxException {
        var interpreter = newInterpreter(vocabName);
        var execSteps = interpreter.debug(query);
        if (!execSteps.isEmpty()) {
            verifyStackContents(vocabName, Iterables.getLast(execSteps).context().stack());
        }
        var steps = execSteps.stream().map(step -> {
            var stack = step.context().stack().stream().map(ExprResource::valueString).toList();
            var vars = Maps.transformValues(step.context().variables(), ExprResource::valueString);
            var ctxt = Map.of("stack", stack, "variables", vars);
            return Map.of("program", step.program(), "context", ctxt);
        });
        return ResponseEntity.ok(steps);
    }

    @GetMapping("/api/v1/expr/normalize")
    public ResponseEntity<?> normalize(
            @RequestParam("q") String query,
            @RequestParam(value = "vocab", required = false) String vocabName
    ) throws InvalidSyntaxException {
        var exprs = evaluate(query, vocabName);
        var normalized = exprs.stream()
                .map(ExprResource::normalizeLegendVars)
                .map(ExprResource::normalizeExprString)
                .toList();
        return ResponseEntity.ok(normalized);
    }

    @GetMapping("/api/v1/expr/eval")
    public ResponseEntity<?> eval(
            @RequestParam("q") String query,
            @RequestParam(value = "vocab", required = false) String vocabName
    ) throws InvalidSyntaxException {
        var exprs = evaluate(query, vocabName);
        var evaluated = exprs.stream()
                .map(ExprResource::normalizeLegendVars)
                .map(e -> Map.of(
                        "expr", normalizeExprString(e.expr()),
                        "settings", e.settings()))
                .toList();
        return ResponseEntity.ok(evaluated);
    }

    @GetMapping("/api/v1/expr/complete")
    public ResponseEntity<?> complete(
            @RequestParam("q") String query,
            @RequestParam(value = "vocab", required = false) String vocabName
    ) throws InvalidSyntaxException {
        var interpreter = newInterpreter(vocabName);
        var result = interpreter.execute(query);
        var descriptions = interpreter.vocabulary()
                .stream()
                .filter(w -> matches(interpreter, w, result))
                .map(w -> Map.of("name", w.name(), "signature", w.signature()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(descriptions);
    }

    /**
     * Currently the values just get converted to a string as the automatic json mapping doesn't
     * provide enough context. Also, the formatter when laying out the expressions for the debug
     * view works well enough for displaying the expr strings to the user. The output can be
     * enhanced in a later version.
     */
    private static String valueString(Object value) {
        if (value instanceof Expr) {
            return ((Expr) value).exprString();
        }
        return value.toString();
    }

    private String getVocabularyName(@Nullable String vocabName) {
        return (vocabName == null) ? vocabulary.name() : vocabName;
    }

    private Interpreter newInterpreter(@Nullable String vocabName) {
        var vocab = vocabularies.get(getVocabularyName(vocabName));
        if (null == vocab) {
            throw new IllegalArgumentException("unknown vocabulary [" + vocabName + "]");
        }
        return new Interpreter(vocab.allWords());
    }

    private static StyleExpr normalizeLegendVars(StyleExpr expr) {
        // For use-cases such as performing automated rewrites of expressions to move off of legacy
        // data it is more convenient to have a consistent way of showing variables. This ensures
        // that it will always include the parenthesis.
        // https://github.com/Netflix/atlas/issues/863
        var legend = expr.settings().get("legend");
        if (null == legend) {
            return expr;
        }
        var settings = new HashMap<>(expr.settings());
        settings.put("legend", Strings.substitute(legend, k -> "$(" + k + ")"));
        return new StyleExpr(expr.expr(), settings);
    }

    private static String normalizeExprString(Expr e) {
        // Remove explicit :const and :query, as they can be determined from implicit
        // conversion and add visual clutter.
        return e.toString().replace(",:const", "").replace(",:query", "");
    }

    private List<StyleExpr> evaluate(String expr, @Nullable String vocabName) throws InvalidSyntaxException {
        var ctx = newInterpreter(vocabName).execute(expr);
        // Reverse the stack to match the order the user would expect.
        return ctx.stack()
                .reverse()
                .stream()
                .filter(TypeUtils::isPresentationType)
                .map(TypeUtils::asPresentationType)
                .collect(Collectors.toList());
    }

    private void verifyStackContents(@Nullable String vocabName, Stack stack) {
        vocabName = getVocabularyName(vocabName);
        if (vocabName.equals("std")) {
            // Don't need to do anything, any stack should be considered valid
            return;
        }
        // Expecting a style expression that can be used in a graph
        var invalidItem = stack.stream().filter(o -> !isPresentationType(o)).findAny();
        if (invalidItem.isPresent()) {
            var summary = Interpreter.typeSummary(Stack.of(invalidItem.get()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "expected an expression, found " + summary);
        }
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("expected an expression, stack is empty");
        }
    }

    private boolean matches(Interpreter interpreter, Word word, Context ctxt) {
        return !excludedWords.contains(word.name())
                && word.matches(ctxt.stack())
                && execWorks(interpreter, word, ctxt);
    }

    private boolean execWorks(Interpreter interpreter, Word word, Context ctxt) {
        // This check is needed to be sure an operation will work if matches is not exhaustive. In
        // some cases it only validates types, but not acceptable values such as :time. For others like
        // macros it always returns true. This ensures the operation will actually be successful before
        // returning to a user.
        try {
            interpreter.execute(List.of(":" + word.name()), ctxt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
