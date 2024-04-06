package com.mayur.ConnectionPool.test;

import com.mayur.ConnectionPool.ConnectionPool;
import com.mayur.SQLConnectionPool.SQLConnectionPoolImpl;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;


public class SQLConnectionPoolTest {

    @Test
    public void sQLConnectionPoolImplTest() throws Exception {
        ConnectionPool<java.sql.Connection> connectionPool = new SQLConnectionPoolImpl("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/customer", "mayur","mayur123",1,5,1000);
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);

        Connection connection1 = connectionPool.getConnection();
        System.out.println("Got Connection 1");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);

        Connection connection2 = connectionPool.getConnection();
        System.out.println("Got Connection 2");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 2);

        connectionPool.releaseConnection(connection1);
        System.out.println("Connection 1 Released");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 2);

        synchronized (this) {
            this.wait(1500);
        }
        connectionPool.removeUnusedConnections();
        System.out.println("Removed Unused Connections");
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);

        System.out.println("After Timeout : 1500 ms");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);

        connectionPool.releaseConnection(connection2);
        System.out.println("Connection 2 Released");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);

        synchronized (this) {
            this.wait(1500);
        }
        connectionPool.removeUnusedConnections();
        System.out.println("Removed Unused Connections");

        System.out.println("After Timeout : 1500 ms");
        System.out.println("Active Connections : " + connectionPool.getCurrentActiveThreadsCount());
        Assert.assertEquals(connectionPool.getCurrentActiveThreadsCount(), 1);
    }
}
