package com.vdzhevalov.executionmodes.datareader;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.provider.Arguments;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class UserData {
    public String fullName;
    public String email;
    public String currentAddress;
    public String permanentAddress;

    private static String resourcePath = "/test-data/users2.json";

    public static Stream<Arguments> getDataFromJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = UserData.class.getResourceAsStream(resourcePath);
        List<UserData> data = Arrays.asList(mapper.readValue(is, UserData[].class));
        return data.stream().map(g -> Arguments.of(g.fullName, g.email, g.currentAddress,g.permanentAddress));
    }
}
