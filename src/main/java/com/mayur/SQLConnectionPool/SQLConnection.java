package com.mayur.SQLConnectionPool;

import com.mayur.ConnectionPool.Connection;

import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class SQLConnection implements Connection<java.sql.Connection> {

    private boolean inUse;
    private long lastUsedTime;
    private java.sql.Connection connection;

    public SQLConnection(final Driver driver, Properties connectionProperties) throws SQLException {
        this.validateProperties(connectionProperties);
        Properties info = new java.util.Properties();
        info.put("user", connectionProperties.getProperty("username"));
        info.put("password", connectionProperties.getProperty("password"));
        this.connection = driver.connect(connectionProperties.getProperty("jdbcUrl"), info);
        this.inUse = false;
        this.lastUsedTime = System.currentTimeMillis();
    }

    private void validateProperties(Properties connectionProperties) {
        if(!connectionProperties.containsKey("driverClassName") || !connectionProperties.containsKey("jdbcUrl") || !connectionProperties.containsKey("username") || !connectionProperties.containsKey("password")) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public java.sql.Connection getConnection() {
        return connection;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
        if(!inUse) {
            lastUsedTime = System.currentTimeMillis();
        }
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }


    @Override
    public void releaseConnection() throws SQLException {
        connection.close();
    }
}
