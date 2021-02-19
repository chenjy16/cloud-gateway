package org.cloud.gateway.core.rule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public final class Authentication {
    
    private String username;
    
    private String password;
}
