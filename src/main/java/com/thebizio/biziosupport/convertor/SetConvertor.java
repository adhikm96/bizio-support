package com.thebizio.biziosupport.convertor;

import javax.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SetConvertor implements AttributeConverter<Set<String>, String> {

	@Override
	public String convertToDatabaseColumn(Set<String> attribute) {
		return attribute == null ? null : String.join(",", attribute);
	}

	@Override
	public Set<String> convertToEntityAttribute(String dbData) {
		return dbData.equals("") ? Collections.emptySet() : new HashSet<>(Arrays.asList(dbData.split(",")));
	}

}
