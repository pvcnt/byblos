package dev.byblos.core.stacklang;

import java.util.List;

public final class Macro extends AbstractWord {
    private final List<Object> body;

    public Macro(String name, List<Object> body) {
        super(name, "? -- ?");
        this.body = List.copyOf(body);
    }

    @Override
    public boolean matches(Stack stack) {
        return true;
    }

    @Override
    public Context execute(Context context) throws InvalidSyntaxException {
        return context.interpreter().execute(body, context, false);
    }
}
