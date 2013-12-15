package org.tdmx.console.application.service;

import java.util.List;

import org.tdmx.console.application.domain.DnsResolverListDO;
import org.tdmx.console.application.domain.validation.FieldError;

public interface DnsResolverService {

	public DnsResolverListDO lookup( String id );
	public List<DnsResolverListDO> search( String criteria );
	
	public void updateSystemResolverList();
	public List<FieldError> createOrUpdate( DnsResolverListDO resolverList );
	public void delete( DnsResolverListDO resolverList );
}
