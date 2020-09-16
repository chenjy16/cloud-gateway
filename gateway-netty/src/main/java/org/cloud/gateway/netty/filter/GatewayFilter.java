package org.cloud.gateway.netty.filter;
import io.netty.handler.codec.http.HttpContent;
import org.cloud.gateway.message.GatewayMessage;
import rx.Observable;

public interface GatewayFilter<I extends GatewayMessage, O extends GatewayMessage> {

    boolean isDisabled();

    String filterName();

    /**
     * filterOrder() must also be defined for a filter. Filters may have the same  filterOrder if precedence is not
     * important for a filter. filterOrders do not need to be sequential.
     *
     * @return the int order of a filter
     */
    int filterOrder();

    /**
     * to classify a filter by type. Standard types in Zuul are "in" for pre-routing filtering,
     * "end" for routing to an origin, "out" for post-routing filters.
     *
     * @return FilterType
     */
    FilterType filterType();

    /**
     * Whether this filter's shouldFilter() method should be checked, and apply() called, even
     * if SessionContext.stopFilterProcessing has been set.
     *
     * @return boolean
     */
    boolean overrideStopFilterProcessing();

    /**
     * Called by zuul filter runner before sending request through this filter. The filter can throw
     * ZuulFilterConcurrencyExceededException if it has reached its concurrent requests limit and does
     * not wish to process the request. Generally only useful for async filters.
     */
    void incrementConcurrency() ;

    /**
     * if shouldFilter() is true, this method will be invoked. this method is the core method of a ZuulFilter
     */
    Observable<O> applyAsync(I input);

    /**
     * Called by zuul filter after request is processed by this filter.
     *
     */
    void decrementConcurrency();

    FilterSyncType getSyncType();

    /**
     * Choose a default message to use if the applyAsync() method throws an exception.
     *
     * @return ZuulMessage
     */
    O getDefaultOutput(I input);

    /**
     * Filter indicates it needs to read and buffer whole body before it can operate on the messages by returning true.
     * The decision can be made at runtime, looking at the request type. For example if the incoming message is a MSL
     * message MSL decryption filter can return true here to buffer whole MSL message before it tries to decrypt it.
     * @return true if this filter needs to read whole body before it can run, false otherwise
     */
    boolean needsBodyBuffered(I input);

    /**
     * Optionally transform HTTP content chunk received
     * @param chunk
     * @return
     */
    HttpContent processContentChunk(GatewayMessage zuulMessage, HttpContent chunk);
}
