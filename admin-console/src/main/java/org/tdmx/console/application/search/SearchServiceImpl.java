package org.tdmx.console.application.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.console.application.domain.CertificateAuthorityDO;
import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.domain.DomainObjectChangesHolder;
import org.tdmx.console.application.domain.DomainObjectFieldChanges;
import org.tdmx.console.application.domain.DomainObjectType;
import org.tdmx.console.application.domain.ServiceProviderDO;
import org.tdmx.console.application.domain.X509CertificateDO;
import org.tdmx.console.application.domain.visit.Traversal;
import org.tdmx.console.application.domain.visit.TraversalContextHolder;
import org.tdmx.console.application.domain.visit.TraversalFunction;
import org.tdmx.console.application.service.ObjectRegistry;


/**
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
	private Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

	//private static List<FieldDescriptor> allDescriptors = new ArrayList<>();

	//TODO CertificateAuthorities
	
	//TODO X509Certificates
	
	//TODO DsnResolverList
	
	//TODO SystemProxyList
	
	//TODO RootCAList

	//TODO search: text
	
	private ObjectRegistry objectRegistry;
	
	private SearchContext searchContext = new SearchContext();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------

	@Override
	public Set<DomainObject> search( DomainObjectType type, String text) {
		SearchCriteria criteria = parse(type,text);
		SearchResultSet resultSet = new SearchResultSet(criteria);
		
		Map<DomainObject, List<SearchableObjectField>> objectFieldMaps = searchContext.getObjectFieldMap(criteria.getType());
		
		Iterator<Entry<DomainObject, List<SearchableObjectField>>> objectFields = objectFieldMaps.entrySet().iterator();
		while( objectFields.hasNext()) {
			Entry<DomainObject, List<SearchableObjectField>> object = objectFields.next();
			resultSet.match(object.getKey(), object.getValue());
		}
		return resultSet.getResult();
	}

	@Override
	public List<SearchableObjectField> getSearchableFields(DomainObject object) {
		if ( object == null ) {
			return null;
		}
		DomainObjectType type = object.getType();
		Map<DomainObject, List<SearchableObjectField>> objectFieldMaps = searchContext.getObjectFieldMap(type);
		return objectFieldMaps.get(object);
	}

	@Override
	public void update( DomainObjectChangesHolder objects ) {

		for( DomainObject deletedObject : objects.deletedObjects ) {
			searchContext.removeObject(deletedObject);
		}
		for( DomainObject newObject : objects.newObjects ) {
			newObject.updateSearchFields(objectRegistry);
			searchContext.addOrUpdateObject(newObject);
		}
		for( Entry<DomainObject,DomainObjectFieldChanges> entry : objects.changedMap.entrySet() ) {
			DomainObject updatedObject = entry.getKey();
			updatedObject.updateSearchFields(objectRegistry);
			searchContext.addOrUpdateObject(updatedObject);
		}
	}

	public void initialize() {
		if ( objectRegistry == null ) {
			log.info("Initialization without objectRegistry.");
			return;
		}
		DomainObjectChangesHolder ch = new DomainObjectChangesHolder();
		
		Traversal.traverse( getObjectRegistry().getX509Certificates(), ch, new TraversalFunction<X509CertificateDO, DomainObjectChangesHolder>() {

			@Override
			public void visit(X509CertificateDO object, TraversalContextHolder<DomainObjectChangesHolder> holder) {
				holder.getResult().registerNew(object);
			}
		});
		
		Traversal.traverse( getObjectRegistry().getCertificateAutorities(), ch, new TraversalFunction<CertificateAuthorityDO, DomainObjectChangesHolder>() {

			@Override
			public void visit(CertificateAuthorityDO object, TraversalContextHolder<DomainObjectChangesHolder> holder) {
				holder.getResult().registerNew(object);
			}
		});
		
		Traversal.traverse( getObjectRegistry().getServiceProviders(), ch, new TraversalFunction<ServiceProviderDO, DomainObjectChangesHolder>() {

			@Override
			public void visit(ServiceProviderDO object, TraversalContextHolder<DomainObjectChangesHolder> holder) {
				holder.getResult().registerNew(object);
			}
		});
		
		// do the initial insert of all objects.
		update(ch);
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	private SearchCriteria parse( DomainObjectType type, String text) {
		SearchExpressionParser parser = new SearchExpressionParser(text);
		List<SearchExpression> expressions = new ArrayList<>();
		SearchExpression exp = null;
		while( (exp = parser.parseNext() ) != null ) {
			expressions.add(exp);
		}

		return new SearchCriteria(type, expressions);
	}

	public static class SearchContext {
		private Map<DomainObjectType, Map<DomainObject, List<SearchableObjectField>>> objectTypeMap = new HashMap<>();
		
		public Map<DomainObject, List<SearchableObjectField>> getObjectFieldMap(DomainObjectType type) {
			return objectTypeMap.get(type);
		}
		
		public SearchContext() {
			objectTypeMap.put(DomainObjectType.ServiceProvider, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
			objectTypeMap.put(DomainObjectType.X509Certificate, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
			objectTypeMap.put(DomainObjectType.BackgroundJob, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
			objectTypeMap.put(DomainObjectType.SystemPropertyList, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
			objectTypeMap.put(DomainObjectType.RootCAList, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
			objectTypeMap.put(DomainObjectType.DnsResolverList, new ConcurrentHashMap<DomainObject, List<SearchableObjectField>>());
		}
		
		public void removeObject( DomainObject object ) {
			objectTypeMap.get(object.getType()).remove(object);
		}
		
		public void addOrUpdateObject( DomainObject object ) {
			objectTypeMap.get(object.getType()).put(object, object.getSearchFields());
		}
	}
	
	public static class ObjectSearchContext {
		List<SearchableObjectField> searchFields = new ArrayList<>();
		
		public ObjectSearchContext() {
		}
		
		public void sof( DomainObject object, FieldDescriptor field, Calendar cal ) {
			if ( cal != null ) {
				add(new SearchableObjectField(object, field, cal));
			}
		}
		
		public void sof( DomainObject object, FieldDescriptor field, Number num ) {
			if ( num != null ) {
				add(new SearchableObjectField(object, field, num));
			}
		}
		
		public void sof( DomainObject object, FieldDescriptor field, String str ) {
			if ( str != null ) {
				add(new SearchableObjectField(object, field, str));
			}
		}
		
		public void sof( DomainObject object, FieldDescriptor field, Boolean bool ) {
			if ( bool != null ) {
				add(new SearchableObjectField(object, field, bool));
			}
		}
		
		private void add( SearchableObjectField sof ) {
			searchFields.add(sof);
		}
		
		public List<SearchableObjectField> getSearchFields() {
			return searchFields;
		}

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
