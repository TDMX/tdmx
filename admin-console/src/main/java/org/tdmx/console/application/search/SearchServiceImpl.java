package org.tdmx.console.application.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.HttpProxyDO;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.visit.Traversal;
import org.tdmx.console.application.domain.visit.TraversalContextHolder;
import org.tdmx.console.application.domain.visit.TraversalFunction;
import org.tdmx.console.application.search.FieldDescriptor.DomainObjectType;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.service.ObjectRegistry;


/**
 * /DomainObjectType:fieldname operator matchValue
 *  
 * @author Peter
 *
 */
public class SearchServiceImpl implements SearchService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private ObjectRegistry objectRegistry;
	
	private List<SearchableObjectField> allFields = new ArrayList<>();
	private static List<FieldDescriptor> allDescriptors = new ArrayList<>();
	
	//TODO registry
	private static final class HttpProxySO {
		public static final FieldDescriptor HOSTNAME	 	= new FieldDescriptor(DomainObjectType.HttpProxy, "hostname", FieldType.String);
		public static final FieldDescriptor PORT 			= new FieldDescriptor(DomainObjectType.HttpProxy, "port", FieldType.Number);
		public static final FieldDescriptor TYPE 			= new FieldDescriptor(DomainObjectType.HttpProxy, "type", FieldType.Token);
		public static final FieldDescriptor USERNAME		= new FieldDescriptor(DomainObjectType.HttpProxy, "username", FieldType.String);
		public static final FieldDescriptor PROXIES			= new FieldDescriptor(DomainObjectType.HttpProxy, "proxies", FieldType.String);
	}
	
	private static final class ServiceProviderSO {
		public static final FieldDescriptor MAS_HOSTNAME 	= new FieldDescriptor(DomainObjectType.ServiceProvider, "mas.hostname", FieldType.String);
		public static final FieldDescriptor MAS_PORT 		= new FieldDescriptor(DomainObjectType.ServiceProvider, "mas.port", FieldType.Number);
		public static final FieldDescriptor MAS_PROXY 		= new FieldDescriptor(DomainObjectType.ServiceProvider, "mas.proxy", FieldType.String);
	}
	
	
	static {
		allDescriptors.add(HttpProxySO.HOSTNAME);
		allDescriptors.add(HttpProxySO.PORT);
		allDescriptors.add(HttpProxySO.TYPE);
		allDescriptors.add(HttpProxySO.USERNAME);
		allDescriptors.add(HttpProxySO.PROXIES);

		allDescriptors.add(ServiceProviderSO.MAS_HOSTNAME);
		allDescriptors.add(ServiceProviderSO.MAS_PORT);
		allDescriptors.add(ServiceProviderSO.MAS_PROXY);
	}
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	@Override
	public List<String> suggestion(String text) {
		// ":" => set of allDescriptor's objectType.alias 
		// ":"<token> => set of allDescriptor's objectType.alias matching token 
		
		// ":"<valid-alias>. => list of objectType.alias descriptors   
		// ":"<valid-alias>.<token> => list of objectType.alias descriptors with fieldName matching token  
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchCriteria parse(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<DomainObject> search(SearchCriteria criteria) {
		Set<DomainObject> resultSet = new HashSet<>();
		
		Iterator<SearchableObjectField> fields = allFields.iterator();
		
		while( fields.hasNext()) {
			SearchableObjectField field = fields.next();
			match(field, criteria, resultSet);
		}
		return resultSet;
	}

	@Override
	public void update( DomainObjectChangesHolder objects ) {
		initialize();
	}

	public void initialize() {
		List<SearchableObjectField> holder = new ArrayList<>();
		Traversal.traverse( getObjectRegistry().getHttpProxies(), holder, new TraversalFunction<HttpProxyDO, List<SearchableObjectField>>() {

			@Override
			public void visit(HttpProxyDO object,
					TraversalContextHolder<List<SearchableObjectField>> holder) {
				
				holder.getResult().add(sof(object, HttpProxySO.HOSTNAME, object.getHostname()));
				holder.getResult().add(sof(object, HttpProxySO.PORT, object.getPort()));
				holder.getResult().add(sof(object, HttpProxySO.TYPE, object.getType()));
				holder.getResult().add(sof(object, HttpProxySO.USERNAME, object.getUsername()));
			}
		});
		
		Traversal.traverse( getObjectRegistry().getServiceProviders(), holder, new TraversalFunction<ServiceProviderDO, List<SearchableObjectField>>() {

			@Override
			public void visit(ServiceProviderDO object,
					TraversalContextHolder<List<SearchableObjectField>> holder) {
				
				holder.getResult().add(sof(object, ServiceProviderSO.MAS_HOSTNAME, object.getMasHostname()));
				holder.getResult().add(sof(object, ServiceProviderSO.MAS_PORT, object.getMasPort()));
				if ( object.getMasProxy() != null ) {
					holder.getResult().add(sof(object, ServiceProviderSO.MAS_PROXY, object.getMasProxy().getDescription()));
					holder.getResult().add(sof(object.getMasProxy(), HttpProxySO.PROXIES, object.getMasHostname()));
				}
			}
		});
		
		allFields = holder;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private SearchableObjectField sof( DomainObject object, FieldDescriptor descriptor, Object value ) {
		SearchableObjectField result = new SearchableObjectField(object, descriptor, value);
		
		//TODO normalize
		
		return result;
	}
	
	private void match( SearchableObjectField field, SearchCriteria criteria, Set<DomainObject> resultSet ) {
		if ( resultSet.contains(field.object) ) {
			
		}
		//TODO 
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

}
