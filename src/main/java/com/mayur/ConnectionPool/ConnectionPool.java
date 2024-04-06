package com.mayur.ConnectionPool;

import java.sql.SQLException;

public interface ConnectionPool<T> {
    T getConnection() throws Exception;
    T getConnection(final long timeout) throws Exception;
    void releaseConnection(T connection) throws Exception;
    int getCurrentActiveThreadsCount();
    int getCurrentIdleThreadsCount();
    int getMaxPoolSizeCount();
    void setMaxPoolSizeCount(int maximumPoolSize);
    void removeUnusedConnections();
    void close() throws SQLException;
    void reOpen() throws Exception;
}
