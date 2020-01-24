package org.cloud.gateway.transport.netty;
import java.util.concurrent.ThreadFactory;

/**
 * Created by cjy on 2020/1/24.
 */
public class CategorizedThreadFactory implements ThreadFactory
{
    private String category;
    private int num = 0;

    public CategorizedThreadFactory(String category) {
        super();
        this.category = category;
    }

    public Thread newThread(final Runnable r) {
        final FastThreadLocalThread t = new FastThreadLocalThread(r,
                category + "-" + num++);
        return t;
    }
}
