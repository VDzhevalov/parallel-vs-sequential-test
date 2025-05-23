package com.vdzhevalov.executionmodes.setup.remote;

import org.junit.jupiter.api.BeforeEach;

public abstract class RemotePerMethodSetup extends RemoteSetup {

    protected static String categoriesResourcePath = "test-data/categories.csv";
    protected static String elementGroupItemResourcePath = "test-data/element-group-items.csv";
    @BeforeEach
    public void tearUp(){
        setupAll();
    }
}
