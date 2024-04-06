package com.mayur.SQLConnectionPool;

import com.mayur.ConnectionPool.Connection;
import com.mayur.ConnectionPool.ConnectionPool;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public class SQLConnectionPoolImpl implements ConnectionPool<java.sql.Connection> {

    private final Driver driver;
    private final int minimumIdleConnections;
    private int maximumPoolSize;
    private final long maxIdleTime;
    private final Properties connectionProperties;
    private boolean isOpen;

    private CopyOnWriteArrayList<Connection<java.sql.Connection>> pool = new CopyOnWriteArrayList<>();

    public SQLConnectionPoolImpl(final String driverClassName, final String jdbcUrl,
                                 final String username, final String password, final int minimumIdleConnections, final int maximumPoolSize, long maxIdleTime) throws Exception {
        if(maximumPoolSize < 0 || minimumIdleConnections < 0 || minimumIdleConnections > maximumPoolSize) {
            throw new IllegalArgumentException();
        }

        Class c = Class.forName(driverClassName);
        this.driver = (Driver) c.newInstance();
        this.connectionProperties = new Properties();
        this.connectionProperties.put("driverClassName", driverClassName);
        this.connectionProperties.put("jdbcUrl",jdbcUrl);
        this.connectionProperties.put("username",username);
        this.connectionProperties.put("password",password);
        this.minimumIdleConnections = minimumIdleConnections;
        this.maximumPoolSize = maximumPoolSize;
        this.maxIdleTime = maxIdleTime;
        this.isOpen = true;

        for(int i=0; i<minimumIdleConnections; i++) {
            synchronized (this) {
                Connection<java.sql.Connection> newConnection = new SQLConnection(this.driver, this.connectionProperties);
                this.pool.add(newConnection);
            }
        }
    }

    @Override
    public java.sql.Connection getConnection() throws Exception {
        if(!this.isOpen) {
            throw new IllegalStateException();
        }
        for (Connection<java.sql.Connection> connection : this.pool) {
            if (!connection.isInUse()) {
                connection.setInUse(true);
                return connection.getConnection();
            }
        }
        // If all connections are in use, create a new one
        if(this.pool.size() < this.maximumPoolSize) {
            synchronized (this.pool) {
                if (this.pool.size() < this.maximumPoolSize) {
                    Connection<java.sql.Connection> newConnection = new SQLConnection(this.driver, this.connectionProperties);
                    newConnection.setInUse(true);
                    this.pool.add(newConnection);
                    return newConnection.getConnection();
                }
            }
        }
        return null;
    }

    @Override
    public java.sql.Connection getConnection(long timeout) throws Exception {
        long currrentTimeout = System.currentTimeMillis() + timeout;
        do {
            if(this.pool.size() < this.maximumPoolSize) {
                java.sql.Connection connection = this.getConnection();
                if(this.getConnection() != null) {
                    return connection;
                }
            }
            wait(5000);
        } while(System.currentTimeMillis() <= currrentTimeout);
        return null;
    }

    @Override
    public void releaseConnection(java.sql.Connection connection) {
        if(!this.isOpen) {
            throw new IllegalStateException();
        }
        for (Connection<java.sql.Connection> poolConnection : this.pool) {
            if (poolConnection.getConnection().equals(connection)) {
                poolConnection.setInUse(false);
                break;
            }
        }
    }

    @Override
    public void removeUnusedConnections() {
        if(!this.isOpen) {
            return;
        }
        for (Connection<java.sql.Connection> poolConnection : this.pool) {
            if (!poolConnection.isInUse() && System.currentTimeMillis() - poolConnection.getLastUsedTime() > this.maxIdleTime && pool.size() > minimumIdleConnections) {
                synchronized (this.pool) {
                    if (!poolConnection.isInUse() && System.currentTimeMillis() - poolConnection.getLastUsedTime() > this.maxIdleTime  && pool.size() > minimumIdleConnections) {
                        pool.remove(poolConnection);
                        try {
                            poolConnection.getConnection().close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @Override
    public int getCurrentActiveThreadsCount() {
        if(!this.isOpen) {
            throw new IllegalStateException();
        }
        return this.pool.size();
    }

    @Override
    public int getCurrentIdleThreadsCount() {
        if(!this.isOpen) {
            throw new IllegalStateException();
        }
        int currentIdleThreadsCount = 0;
        for (Connection<java.sql.Connection> connection : this.pool) {
            if (!connection.isInUse()) {
                currentIdleThreadsCount++;
            }
        }
        return currentIdleThreadsCount;
    }

    @Override
    public int getMaxPoolSizeCount() {
        return this.maximumPoolSize;
    }

    @Override
    public void setMaxPoolSizeCount(int maximumPoolSize) {
        if(maximumPoolSize < 0 || maximumPoolSize < this.minimumIdleConnections)
            throw new IllegalArgumentException();
        this.maximumPoolSize = maximumPoolSize;
    }

    public void close() throws SQLException {
        this.isOpen = false;
        for (Connection<java.sql.Connection> poolConnection : this.pool) {
            poolConnection.getConnection().close();
        }
        pool.clear();
    }

    @Override
    public void reOpen() throws Exception {
        this.isOpen = true;
        for(int i=0; i<minimumIdleConnections; i++) {
            this.getConnection();
        }
    }
}
