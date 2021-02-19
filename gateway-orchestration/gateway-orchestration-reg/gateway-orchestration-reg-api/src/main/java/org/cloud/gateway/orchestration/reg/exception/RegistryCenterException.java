
package org.cloud.gateway.orchestration.reg.exception;


public final class RegistryCenterException extends RuntimeException {
    
    private static final long serialVersionUID = -6417179023552012152L;
    
    public RegistryCenterException(final String errorMessage, final Object... args) {
        super(String.format(errorMessage, args));
    }
    
    public RegistryCenterException(final Exception cause) {
        super(cause);
    }
}
