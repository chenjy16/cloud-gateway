package org.cloud.gateway.core.keygen;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGeneratorFactory {
    

    public static KeyGenerator newInstance(final String keyGeneratorClassName) {
        try {
            return (KeyGenerator) Class.forName(keyGeneratorClassName).newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new IllegalArgumentException(String.format("Class %s should have public privilege and no argument constructor", keyGeneratorClassName));
        }
    }
}
