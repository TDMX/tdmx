/*
 * TDMX - Trusted Domain Messaging eXchange
 * 
 * Enterprise B2B messaging between separate corporations via interoperable cloud service providers.
 * 
 * Copyright (C) 2014 Peter Klauser (http://tdmx.org)
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/.
 */

package org.tdmx.lib.control.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages a pooled DataSource per PartitionId. The PartitionId is shared with the DynamicDataSource via ThreadLocal
 * storage since the javax.sql.DataSource does not foresee passing in context.
 * 
 * TODO configure 2 dbs for 2 partitions
 * 
 * TODO unit test with hsql config
 * 
 * @author Peter
 * 
 */
public class DynamicDataSource implements javax.sql.DataSource {

	private static final Log log = LogFactory.getLog(DynamicDataSource.class);

	private DataSourceConfigurationProvider configurationProvider;
	private final Map<String, DatabaseConnectionInfo> partitionConnectionInfoMap = new ConcurrentHashMap<String, DatabaseConnectionInfo>();
	private final Map<DatabaseConnectionInfo, BasicDataSource> connectionDataSourceMap = new ConcurrentHashMap<DatabaseConnectionInfo, BasicDataSource>();

	private PartitionIdProvider partitionIdProvider;

	/**
	 * The PrintWriter to which log messages should be directed.
	 */
	protected PrintWriter logWriter = new PrintWriter(System.out);

	private synchronized void createDataSource(DatabaseConnectionInfo dci) throws SQLException {
		// race condition avoidance
		if (connectionDataSourceMap.get(dci) != null) {
			return;
		}
		BasicDataSource bds = new BasicDataSource();
		bds.setUrl(dci.getUrl());
		bds.setDriverClassName(dci.getDriverClassname());
		bds.setUsername(dci.getUsername());
		bds.setPassword(dci.getPassword());
		bds.setMaxActive(100);
		bds.setMinIdle(0);
		bds.setInitialSize(1);
		bds.setMinEvictableIdleTimeMillis(60000);
		bds.setLogWriter(logWriter);
		connectionDataSourceMap.put(dci, bds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getConnection()
	 */
	@Override
	public Connection getConnection() throws SQLException {
		if (getPartitionIdProvider() == null) {
			throw new SQLException("No partitionIdProvider.");
		}
		String partitionId = getPartitionIdProvider().getPartitionId();
		DatabaseConnectionInfo ci = getConfigurationProvider().getPartitionInfo(partitionId);
		if (ci == null) {
			log.warn("No DatabaseConnectionInfo provided for partitionId " + partitionId);
		}
		DatabaseConnectionInfo existingCi = partitionConnectionInfoMap.get(partitionId);
		if (existingCi == null) {
			partitionConnectionInfoMap.put(partitionId, ci);
			log.info("First connection use for partitionId " + partitionId);
		} else if (!existingCi.equals(ci)) {
			// there's been a change of DB connection for a partition
			partitionConnectionInfoMap.put(partitionId, ci);
			log.warn("DatabasePartition change for partitionId " + partitionId);
		}
		BasicDataSource bds = connectionDataSourceMap.get(ci);
		if (bds == null) {
			createDataSource(ci);
			bds = connectionDataSourceMap.get(ci);
		}
		if (bds == null) {
			throw new SQLException("Unable to create BasicDataSource for " + ci);
		}
		return bds.getConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException("DynamicDataSource is not a wrapper.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLogWriter()
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return logWriter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
	 */
	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		this.logWriter = logWriter;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#setLoginTimeout(int)
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// This method isn't supported by the PoolingDataSource returned by
		// the createDataSource
		throw new UnsupportedOperationException("Not supported by DynamicDataSource");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.CommonDataSource#getLoginTimeout()
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		// This method isn't supported by the PoolingDataSource returned by
		// the createDataSource
		throw new UnsupportedOperationException("Not supported by DynamicDataSource");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		// This method isn't supported by the PoolingDataSource returned by
		// the createDataSource
		throw new UnsupportedOperationException("Not supported by DynamicDataSource");
	}

	public DataSourceConfigurationProvider getConfigurationProvider() {
		return configurationProvider;
	}

	public void setConfigurationProvider(DataSourceConfigurationProvider configurationProvider) {
		this.configurationProvider = configurationProvider;
	}

	public PartitionIdProvider getPartitionIdProvider() {
		return partitionIdProvider;
	}

	public void setPartitionIdProvider(PartitionIdProvider partitionIdProvider) {
		this.partitionIdProvider = partitionIdProvider;
	}

}
