package plugon.lib.configuration.local.rdbms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import plugon.lib.configuration.ConfigurationApi.GetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.GetPropertiesResponse;
import plugon.lib.configuration.ConfigurationApi.Property;
import plugon.lib.configuration.ConfigurationApi.PropertyType;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesRequest;
import plugon.lib.configuration.ConfigurationApi.SetPropertiesResponse;
import plugon.lib.configuration.ConfigurationServiceProxy;
import plugon.lib.configuration.ConfigurationUtils;
import plugon.lib.configuration.local.dao.ConfigurationDao;
import plugon.lib.configuration.local.domain.ConfigurationValue;
import plugon.lib.configuration.local.domain.ValueType;

@RunWith(SpringJUnit4ClassRunner.class)

//ApplicationContext will be loaded from "classpath:/plugon/lib/configuration/local/ConfigurationServiceRepositoryUnitTest-context.xml"
@ContextConfiguration

@TransactionConfiguration(transactionManager="plugon.lib.configuration.TransactionManager", defaultRollback=false)
@Transactional("ConfigurationDB")
public class ConfigurationServiceRepositoryUnitTest {

	@Autowired
	private ConfigurationServiceProxy service;
	
	@Autowired
	private ConfigurationDao dao;
	
	@Before
	public void doSetup() {
		ConfigurationValue str = new ConfigurationValue();
		str.setFilename("UNIT-TEST");
		str.setPropertyName("str1");
		str.setStringValue("v1");
		str.setValueType(ValueType.STRING);
		dao.persist(str);

		ConfigurationValue integer = new ConfigurationValue();
		integer.setFilename("UNIT-TEST");
		integer.setPropertyName("int1");
		integer.setStringValue("1001");
		integer.setValueType(ValueType.INTEGER);
		dao.persist(integer);

		ConfigurationValue longer = new ConfigurationValue();
		longer.setFilename("UNIT-TEST");
		longer.setPropertyName("long1");
		longer.setStringValue("1001001001");
		longer.setValueType(ValueType.LONG);
		dao.persist(longer);
		
		ConfigurationValue day = new ConfigurationValue();
		day.setFilename("UNIT-TEST");
		day.setPropertyName("day1");
		day.setStringValue("2010-01-21");
		day.setValueType(ValueType.DAY);
		dao.persist(day);
		
		ConfigurationValue date = new ConfigurationValue();
		date.setFilename("UNIT-TEST");
		date.setPropertyName("date1");
		date.setStringValue("2010-01-21 22:09:01 +0100");
		date.setValueType(ValueType.DATE);
		dao.persist(date);

		// this will be ignored since it's missing the TimeZone
		ConfigurationValue date2 = new ConfigurationValue();
		date2.setFilename("UNIT-TEST");
		date2.setPropertyName("date2");
		date2.setStringValue("2010-01-21 22:09:22");
		date2.setValueType(ValueType.DATE);
		dao.persist(date2);
	}
	
	@After
	public void doTeardown() {
		List<ConfigurationValue> result = dao.loadByFilename("UNIT-TEST");
		for( ConfigurationValue value : result ) {
			dao.delete(value);
		}
	}
	
	@Test
	public void testAutoWire() throws Exception {
		assertNotNull(dao);
		assertNotNull(service);
	}

	@Test
	public void testGetFileNames() throws Exception {
		List<String> filenames = dao.listFilenames();
		assertNotNull(filenames);
		assertEquals(1,filenames.size());
		assertEquals("UNIT-TEST", filenames.get(0));
	}
	
	@Test
	public void testSetProperties() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(6, response.getPropertiesList().size());
		
		List<Property> newList = new ArrayList<Property>();
		int i = 0;
		for( Property value : response.getPropertiesList()) {
			if (i > 0) { // skip the first property
				newList.add(Property.newBuilder(value).build());
			}
			i++;
		}
		Property.Builder newProperty = Property.newBuilder();
		newProperty.setName("newProperty");
		newProperty.setType(PropertyType.STRING);
		newProperty.setValue("newValue");
		newList.add(newProperty.build());
		
		SetPropertiesRequest.Builder setrequest = SetPropertiesRequest.newBuilder();
		setrequest.setFilename("UNIT-TEST");
		setrequest.addAllProperties(newList);
		SetPropertiesResponse setresponse = service.setProperties(setrequest.build());
		assertEquals(SetPropertiesResponse.ErrorCode.SUCCESS, setresponse.getErrorCode());
		
		request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response2 = service.getProperties(request.build());
		assertNotNull(response2);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response2.getErrorCode());
		assertNotNull(response2.getPropertiesList());
		assertEquals(6, response2.getPropertiesList().size());
		
		assertEquals(ConfigurationUtils.getJavaProperties(response2.getPropertiesList()).get("newProperty"), "newValue");
	}
	
	@Test
	public void testFetchFile() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("UNIT-TEST");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(6, response.getPropertiesList().size());

		Properties props = ConfigurationUtils.getJavaProperties(response.getPropertiesList());
		assertNotNull(props);
		
		assertEquals("v1", props.get("str1"));
		assertEquals(Integer.parseInt("1001"), props.get("int1"));
		assertEquals(Long.parseLong("1001001001"), props.get("long1"));
		
		SimpleDateFormat dayF = new SimpleDateFormat("yyyy-MM-dd");
		Date day = dayF.parse("2010-01-21");
		assertEquals(day, props.get("day1"));
		
		SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		Date date = dateF.parse("2010-01-21 22:09:01 +0100");
		assertEquals(date, props.get("date1"));
		
		assertEquals(5, props.entrySet().size());
	}

	@Test
	public void testFetchFileNotFound() throws Exception {
		GetPropertiesRequest.Builder request = GetPropertiesRequest.newBuilder();
		request.setFilename("gugus");
		GetPropertiesResponse response = service.getProperties(request.build());
		assertNotNull(response);
		assertEquals(GetPropertiesResponse.ErrorCode.SUCCESS, response.getErrorCode());
		assertNotNull(response.getPropertiesList());
		assertEquals(0, response.getPropertiesList().size());
	}

}