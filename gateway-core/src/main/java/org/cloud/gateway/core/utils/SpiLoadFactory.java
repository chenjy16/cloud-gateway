package org.cloud.gateway.core.utils;

import java.util.Iterator;
import java.util.ServiceLoader;


public class SpiLoadFactory {


    public static <S> S loadFirst(final Class<S> clazz) {
        final ServiceLoader<S> loader = loadAll(clazz);
        final Iterator<S> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format( "No implementation defined in /META-INF/services/%s",clazz.getName()));
        }
        return iterator.next();
    }


    public static <S> ServiceLoader<S> loadAll(final Class<S> clazz) {
        return ServiceLoader.load(clazz);
    }
}
