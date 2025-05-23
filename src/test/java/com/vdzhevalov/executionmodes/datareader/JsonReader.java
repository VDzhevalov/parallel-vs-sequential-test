package com.vdzhevalov.executionmodes.datareader;

import io.qameta.allure.internal.shadowed.jackson.core.type.TypeReference;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonReader {
    public String searchQuery;
    public String expectedTitle;

    private static String resourcePath = "/test-data/MainPageData.json";

    public static void setResourcePath(String path) {
        resourcePath = path;
    }

public static class Element {
    public String element;
}

    public static String getDataFromJson(String element) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = JsonReader.class.getResourceAsStream(resourcePath);
        List<Map<String, String>> list = mapper.readValue(is, new TypeReference<List<Map<String, String>>>() {});

        Map<String, String> resultMap = list.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return resultMap.get(element);
    }

    public static String getElementValue(String element){
        String s="Exception mapper.readValue: ";
        try {
            s = getDataFromJson(element);
        }catch (Exception e){
            s += e.getMessage();
        }
        return s;
    }
}
