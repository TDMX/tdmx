/**
 *   Copyright 2010 Peter Klauser
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
*/
package org.tdmx.lib.control.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.tdmx.lib.control.dao.DatabasePartitionDao;
import org.tdmx.lib.control.domain.DatabasePartition;
import org.tdmx.lib.control.domain.DatabaseType;

/**
 * @author Peter Klauser
 *
 */
public class DatabasePartitionServiceRepositoryImpl implements DatabasePartitionService {

	private static Logger log = LoggerFactory.getLogger(DatabasePartitionServiceRepositoryImpl.class);
	 
	private DatabasePartitionDao databasePartitionDao;
	
	private long cacheTimeoutMillis = 900000;
	
	private Map<DatabaseType, Map<String,List<DatabasePartition>>> dptsMap = null;
	private Map<String,DatabasePartition> dpidMap = null;
	private long cacheLoadTimestamp = 0;
	
	@Override
	@Transactional(value="ControlDB")
	public void createOrUpdate(DatabasePartition partition) {
		DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getPartitionId());
		if ( storedPartition == null ) {
			getDatabasePartitionDao().persist(partition);
		} else {
			assertSame( "dbType", storedPartition.getDbType(), partition.getDbType());
			assertSame( "segment", storedPartition.getSegment(), partition.getSegment());
			if ( storedPartition.getActivationTimestamp() != null ) {
				// active partitions cannot change some fields.
				assertSame( "sizeFactor", storedPartition.getSizeFactor(), partition.getSizeFactor());
				assertSame( "activationTimestamp", storedPartition.getActivationTimestamp(), partition.getActivationTimestamp());
				if ( storedPartition.getDeactivationTimestamp() != null ) {
					assertSame( "deactivationTimestamp", storedPartition.getDeactivationTimestamp(), partition.getDeactivationTimestamp());
				}
			}
			
			getDatabasePartitionDao().merge(partition);
		}
		clearCache();
	}

	/**
	 * @throws IllegalStateException if try to change immutable fields.
	 */
	private void assertSame( String field, Object target, Object actual ) {
		if ( target != null ) {
			if ( !target.equals(actual) ) {
				throw new IllegalStateException("Field " + field + " is immutable, and cannot be changed or removed.");
			}
		} else if ( actual != null ) {
			throw new IllegalStateException("Field " + field + " is immutable, and cannot be set.");
		}
	}
	
	/**
	 * @throws IllegalStateException if try to change immutable fields.
	 */
	private void assertSame( String field, int target, int actual ) {
		if ( target != actual ) {
			throw new IllegalStateException("Field " + field + " is immutable, and cannot be changed.");
		}
	}
	
	@Override
	@Transactional(value="ControlDB")
	public void delete(DatabasePartition partition) {
		DatabasePartition storedPartition = getDatabasePartitionDao().loadById(partition.getPartitionId());
		if ( storedPartition != null ) {
			getDatabasePartitionDao().delete(storedPartition);
		} else {
			log.warn("Unable to find DatabasePartition to delete with id " + partition.getPartitionId());
		}
		clearCache();
	}

	@Override
	@Transactional(value="ControlDB",readOnly=true)
	public DatabasePartition findById(String partitionId) {
		conditionalRefreshCache();
		
		return dpidMap != null ? dpidMap.get(partitionId) : null;
	}

	@Override
	@Transactional(value="ControlDB",readOnly=true)
	public List<DatabasePartition> findByTypeAndSegment(DatabaseType type,
			String segment) {
		conditionalRefreshCache();
		
		if ( dptsMap != null ) {
			Map<String,List<DatabasePartition>> segmentMap = dptsMap.get(type);
			if ( segmentMap != null ) {
				List<DatabasePartition> segmentList = segmentMap.get(segment);
				if ( segmentList != null ) {
					return segmentList;
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional(value="ControlDB",readOnly=true)
	public List<DatabasePartition> findByType(DatabaseType type) {
		conditionalRefreshCache();
		List<DatabasePartition> result = new ArrayList<>();
		if ( dptsMap != null ) {
			Map<String,List<DatabasePartition>> segmentMap = dptsMap.get(type);
			if ( segmentMap != null ) {
				Collection<List<DatabasePartition>> segmentList = segmentMap.values();
				if ( segmentList != null ) {
					for( List<DatabasePartition> list : segmentList ) {
						result.addAll(list);
					}
				}
			}
		}
		return Collections.unmodifiableList(result);
	}


	private synchronized void clearCache() {
		cacheLoadTimestamp = 0;
	}

	private void conditionalRefreshCache() {
		if ( System.currentTimeMillis() - cacheLoadTimestamp > getCacheTimeoutMillis() ) {
			loadCache();
		}
	}
	
	private synchronized void loadCache() {
		cacheLoadTimestamp = System.currentTimeMillis();
		
		
		List<DatabasePartition> list = getDatabasePartitionDao().loadAll();
		
		Map<String, DatabasePartition> localIdMap = new HashMap<>();
		Map<DatabaseType, Map<String,List<DatabasePartition>>> localTypeSegmentMap = new HashMap<>();
		
		for( DatabasePartition partition : list ) {
			localIdMap.put(partition.getPartitionId(), partition);
			
			Map<String,List<DatabasePartition>> typeMap = localTypeSegmentMap.get(partition.getDbType());
			if ( typeMap == null ) {
				typeMap = new HashMap<>();
				localTypeSegmentMap.put(partition.getDbType(), typeMap);
			}
			List<DatabasePartition> segmentPartitions = typeMap.get(partition.getSegment());
			if ( segmentPartitions == null ) {
				segmentPartitions = new ArrayList<>();
				typeMap.put(partition.getSegment(), segmentPartitions);
			}
			segmentPartitions.add(partition);
		}
		
		dpidMap = Collections.unmodifiableMap(localIdMap);
		dptsMap = Collections.unmodifiableMap(localTypeSegmentMap);
	}
	
	
	
	

	
	public DatabasePartitionDao getDatabasePartitionDao() {
		return databasePartitionDao;
	}


	public void setDatabasePartitionDao(DatabasePartitionDao databasePartitionDao) {
		this.databasePartitionDao = databasePartitionDao;
	}

	public long getCacheTimeoutMillis() {
		return cacheTimeoutMillis;
	}

	public void setCacheTimeoutMillis(long cacheTimeoutMillis) {
		this.cacheTimeoutMillis = cacheTimeoutMillis;
	}

}
