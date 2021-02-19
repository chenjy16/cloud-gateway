package org.cloud.gateway.core.rule;
import com.google.common.base.Preconditions;
import lombok.Getter;
import java.util.Collection;
import java.util.LinkedList;



@Getter
public class RouteRule {

    
    private final Collection<String> broadcastTables = new LinkedList<>();

    public RouteRule(final Collection<String> dataSourceNames) {
        Preconditions.checkNotNull(dataSourceNames, "Data sources cannot be null.");
        Preconditions.checkArgument(!dataSourceNames.isEmpty(), "Data sources cannot be empty.");

    }

}
