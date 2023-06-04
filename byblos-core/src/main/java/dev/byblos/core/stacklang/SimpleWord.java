package dev.byblos.core.stacklang;

public abstract class SimpleWord extends AbstractWord {

    protected SimpleWord(String name, String signature) {
        super(name, signature);
    }

    @Override
    public final Context execute(Context context) {
        return context.withStack(execute(context.stack()));
    }

    protected abstract Stack execute(Stack stack);
}
