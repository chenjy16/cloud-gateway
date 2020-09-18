package org.cloud.gateway.message;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Headers implements Cloneable {


    private final ListMultimap<HeaderName, String> delegate;
    private final boolean immutable;

    public Headers()
    {
        delegate = ArrayListMultimap.create();
        immutable = false;
    }

    private Headers(ListMultimap<HeaderName, String> delegate)
    {
        this.delegate = delegate;
        immutable = ImmutableListMultimap.class.isAssignableFrom(delegate.getClass());
    }

    protected HeaderName getHeaderName(String name)
    {
        return HttpHeaderNames.get(name);
    }

    private boolean delegatePut(HeaderName hn, String value) {
        return delegate.put(hn, stripMaliciousHeaderChars(value));
    }

    private void delegatePutAll(Headers headers) {
        // enforce using above delegatePut method, for stripping malicious characters
        headers.delegate.entries().forEach(entry -> delegatePut(entry.getKey(), entry.getValue()));
    }

    /**
     * Get the first value found for this key even if there are multiple. If none, then
     * return null.
     *
     * @param name
     * @return
     */
    public String getFirst(String name)
    {
        HeaderName hn = getHeaderName(name);
        return getFirst(hn);
    }
    public String getFirst(HeaderName hn)
    {
        List<String> values = delegate.get(hn);
        if (values != null) {
            if (values.size() > 0) {
                return values.get(0);
            }
        }
        return null;
    }

    /**
     * Get the first value found for this key even if there are multiple. If none, then
     * return the specified defaultValue.
     *
     * @param name
     * @return
     */
    public String getFirst(String name, String defaultValue)
    {
        String value = getFirst(name);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    public String getFirst(HeaderName hn, String defaultValue)
    {
        String value = getFirst(hn);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public List<String> get(String name)
    {
        HeaderName hn = getHeaderName(name);
        return get(hn);
    }
    public List<String> get(HeaderName hn)
    {
        return delegate.get(hn);
    }

    /**
     * Replace any/all entries with this key, with this single entry.
     *
     * If value is null, then not added, but any existing header of same name is removed.
     *
     * @param name
     * @param value
     */
    public void set(String name, String value)
    {
        HeaderName hn = getHeaderName(name);
        set(hn, value);
    }
    public void set(HeaderName hn, String value)
    {
        delegate.removeAll(hn);
        if (value != null) {
            delegatePut(hn, value);
        }
    }

    public boolean setIfAbsent(String name, String value)
    {
        HeaderName hn = getHeaderName(name);
        return setIfAbsent(hn, value);
    }
    public boolean setIfAbsent(HeaderName hn, String value)
    {
        boolean did = false;
        if (! contains(hn)) {
            set(hn, value);
            did = true;
        }
        return did;
    }

    public void add(String name, String value)
    {
        HeaderName hn = getHeaderName(name);
        add(hn, value);
    }
    public void add(HeaderName hn, String value)
    {
        delegatePut(hn, value);
    }

    public void putAll(Headers headers)
    {
        delegatePutAll(headers);
    }

    public List<String> remove(String name)
    {
        HeaderName hn = getHeaderName(name);
        return remove(hn);
    }
    public List<String> remove(HeaderName hn)
    {
        return delegate.removeAll(hn);
    }

    public boolean removeIf(Predicate<? super Map.Entry<HeaderName, String>> filter) {
        return delegate.entries().removeIf(filter);
    }

    public Collection<Header> entries()
    {
        return delegate.entries()
                .stream()
                .map(entry -> new Header(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public Set<HeaderName> keySet()
    {
        return delegate.keySet();
    }

    public boolean contains(String name)
    {
        return contains(getHeaderName(name));
    }
    public boolean contains(HeaderName hn)
    {
        return delegate.containsKey(hn);
    }

    public boolean contains(String name, String value)
    {
        HeaderName hn = getHeaderName(name);
        return contains(hn, value);
    }
    public boolean contains(HeaderName hn, String value)
    {
        return delegate.containsEntry(hn, value);
    }

    public int size()
    {
        return delegate.size();
    }

    @Override
    public Headers clone()
    {
        Headers copy = new Headers();
        copy.delegatePutAll(this);
        return copy;
    }

    public Headers immutableCopy()
    {
        return new Headers(ImmutableListMultimap.copyOf(delegate));
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;
        if (! (obj instanceof Headers))
            return false;

        Headers h2 = (Headers) obj;
        return Iterables.elementsEqual(delegate.entries(), h2.delegate.entries());
    }

    @Override
    public String toString()
    {
        return delegate.toString();
    }
}

