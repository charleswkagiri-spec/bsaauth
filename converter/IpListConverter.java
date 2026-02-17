package com.tangazoletu.spotcashesb.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class IpListConverter implements AttributeConverter<List<String>, String> {
    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(List<String> ipList) {
        if (ipList == null || ipList.isEmpty()) {
            return null;
        }
        return ipList.stream()
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .collect(Collectors.toList());
    }
}