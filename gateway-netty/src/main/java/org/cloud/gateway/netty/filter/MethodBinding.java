package org.cloud.gateway.netty.filter;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;



public class MethodBinding<T> {


    private final BiConsumer<Runnable, T> boundMethod;
    private final Callable<T> bindingContextExtractor;

    public static MethodBinding<?> NO_OP_BINDING = new MethodBinding<>((r, t) -> {}, () -> null);

    public MethodBinding(BiConsumer<Runnable, T> boundMethod, Callable<T> bindingContextExtractor) {
        this.boundMethod = boundMethod;
        this.bindingContextExtractor = bindingContextExtractor;
    }

    public void bind(Runnable method) throws Exception {
        T bindingContext = bindingContextExtractor.call();
        if (bindingContext == null) {
            method.run();
        }
        else {
            boundMethod.accept(method, bindingContext);
        }
    }
}

