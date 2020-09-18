package org.cloud.gateway.message;

public class Header implements Cloneable {
    private final HeaderName name;
    private final String value;

    public Header(HeaderName name, String value)
    {
        if (name == null) throw new NullPointerException("Header name cannot be null!");
        this.name = name;
        this.value = value;
    }

    public String getKey()
    {
        return name.getName();
    }

    public HeaderName getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (!name.equals(header.name)) return false;
        return !(value != null ? !value.equals(header.value) : header.value != null);

    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("%s: %s", name, value);
    }
}

