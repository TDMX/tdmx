package plugon.lib.configuration.local.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.core.system.lang.StringUtils;

import plugon.lib.configuration.ConfigurationApi;

/**
 * A ConfigurationValue which belongs to a virtual property "file".
 * 
 * NOTE: the properties are unique by name only. The filename does not partition
 * property values ( this would require a composite pk )
 * 
 * @author Peter Klauser
 *
 */
@Entity
@Table(name="ConfigurationValue")
public class ConfigurationValue implements Serializable {

	private static Logger log = LoggerFactory.getLogger(ConfigurationValue.class);
	 
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	public static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static final int MAX_PROPERTYNAME_LEN = 128;
	public static final int MAX_FILENAME_LEN = 64;
	public static final int MAX_STRINGVALUE_LEN = 2000;
	
	private static final long serialVersionUID = -988419614813872556L;

	@Id
	@Column(length = MAX_PROPERTYNAME_LEN)
	private String propertyName;
	
	@Column(length = MAX_FILENAME_LEN, nullable = false)
	private String filename;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 16, nullable = false)
	private ValueType valueType;
	
	@Column(length = MAX_STRINGVALUE_LEN, nullable = false)
	private String stringValue;
	
	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}
	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = StringUtils.truncateToMaxLen(propertyName, MAX_PROPERTYNAME_LEN);
	}
	/**
	 * @return the stringValue
	 */
	public String getStringValue() {
		return stringValue;
	}
	/**
	 * @param stringValue the stringValue to set
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = StringUtils.truncateToMaxLen(stringValue, MAX_STRINGVALUE_LEN);
	}
	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return valueType;
	}
	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	/**
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @param filename the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = StringUtils.truncateToMaxLen(filename, MAX_FILENAME_LEN);
	}
	
	
	public void copyFrom(ConfigurationApi.Property property ) {
		if ( property == null ) {
			return;
		}
		if ( property.hasName() ) {
			setPropertyName( property.getName() );
		}
		if ( property.hasValue() ) {
			setStringValue(property.getValue());
		}
		if ( property.hasType() ) {
			setValueType(ValueType.mapFrom(property.getType()));
		}
	}
	
	public static ConfigurationValue mapFrom(String filename, ConfigurationApi.Property property ) {
		if ( property == null ) {
			return null;
		}
		ConfigurationValue c = new ConfigurationValue();
		c.copyFrom(property);
		c.setFilename(filename);
		return c;
	}

	public void copyTo(ConfigurationApi.Property.Builder property ) {
		if ( property == null ) {
			return;
		}
		if ( getPropertyName() != null ) {
			property.setName( getPropertyName() );
		}
		if ( getStringValue() != null ) {
			property.setValue( getStringValue() );
		}
		if ( getValueType() != null ) {
			property.setType( ValueType.mapFrom(getValueType()));
		}
	}
	
	public static ConfigurationApi.Property.Builder mapFrom(String filename, ConfigurationValue property ) {
		if ( property == null ) {
			return null;
		}
		ConfigurationApi.Property.Builder c = ConfigurationApi.Property.newBuilder();
		property.copyTo(c);
		property.setFilename(filename);
		return c;
	}

	
}
