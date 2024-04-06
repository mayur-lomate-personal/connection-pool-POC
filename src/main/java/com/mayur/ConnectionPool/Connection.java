package com.mayur.ConnectionPool;

import java.util.Properties;

public interface Connection<T> {
    T getConnection();
    void releaseConnection() throws Exception;
    boolean isInUse();
    void setInUse(boolean inUse);
    long getLastUsedTime();
}
