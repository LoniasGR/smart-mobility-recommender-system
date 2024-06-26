package gr.iccs.smart.mobility.geojson;

import gr.iccs.smart.mobility.geojson.jackson.CrsType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Crs implements Serializable{

	private CrsType type = CrsType.name;
	private Map<String, Object> properties = new HashMap<>();

	public CrsType getType() {
		return type;
	}

	public void setType(CrsType type) {
		this.type = type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Crs crs)) {
			return false;
		}
        if (!Objects.equals(properties, crs.properties)) {
			return false;
		}
		return Objects.equals(type, crs.type);
	}

	@Override
	public int hashCode() {
		int result = type != null ? type.hashCode() : 0;
		result = 31 * result + (properties != null ? properties.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Crs{" + "type='" + type + '\'' + ", properties=" + properties + '}';
	}
}
