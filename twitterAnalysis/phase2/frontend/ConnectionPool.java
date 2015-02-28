package model;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {

	 private static ConnectionPool instance = null;
	 private HikariDataSource dataSource = null;
 
	 private ConnectionPool(){
		 HikariConfig config = new HikariConfig();
	     config.setMaximumPoolSize(150);
	     config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
	     config.addDataSourceProperty("port", 3306);
	     config.addDataSourceProperty("serverName", "localhost");
	     config.addDataSourceProperty("user", "root");
	     config.addDataSourceProperty("password", "root");
	     config.addDataSourceProperty("url", "jdbc:mysql://localhost:3306/tweet");
	     dataSource = new HikariDataSource(config);
	 }
	 
	 public static ConnectionPool getInstance (){
         if(instance == null){
             instance = new ConnectionPool();
         }
		 return instance;
	 }
	 
	 public Connection getConnection() throws SQLException{
		 return dataSource.getConnection();
	 }

}
