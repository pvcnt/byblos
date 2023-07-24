package dev.byblos.stacklang;

public abstract class SimpleWord extends AbstractWord {

    protected SimpleWord(String name, String signature) {
        super(name, signature);
    }

    @Override
    public final Context execute(Context context) throws InvalidSyntaxException {
        return context.withStack(execute(context.stack()));
    }

    protected abstract Stack execute(Stack stack) throws InvalidSyntaxException;
}
