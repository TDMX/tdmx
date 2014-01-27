/**
 * 
 */
package org.tdmx.lib.control.datasource;

/**
 * For each call of {@link javax.sql.DataSource#getConnection()} a DynamicPartitionedDataSource will
 * request the DatabaseConnectionInfo for the connection. It will manage a ConnectionPool per
 * DatabaseConnectionInfo ( unique schema ).
 *
 * @author Peter
 *
 */
public interface DataSourceConfigurationProvider {

	/**
	 * Get DatabaseConnectionInfo for a partition. 
	 * 
	 * NOTE: called with NULL during Hibernate startup.
	 * 
	 * @param partitionId
	 * @return
	 */
	public DatabaseConnectionInfo getPartitionInfo( String partitionId );
	
}
