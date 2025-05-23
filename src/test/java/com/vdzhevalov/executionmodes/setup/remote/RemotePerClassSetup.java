package com.vdzhevalov.executionmodes.setup.remote;

import com.codeborne.selenide.WebDriverRunner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class RemotePerClassSetup extends RemoteSetup {
    protected WebDriver driver;
    protected static String categoriesResourcePath = "test-data/categories.csv";
    protected static String elementGroupItemResourcePath = "test-data/element-group-items.csv";

    @BeforeAll
    public void tearUp() {
        try {
            driver =new RemoteWebDriver(new URL("http://selenium-hub:4444/wd/hub"), chromeSetup());
            driver.get(BASE_URL);
            WebDriverRunner.setWebDriver(driver);
            System.err.println("RemoteWebDriver для Selenium Grid UP: https://demoqa.com opened");
        }
        catch (MalformedURLException e) {
            System.err.println("Некоректна URL для Selenium Grid: " + e.getMessage());
        }
        catch (Exception e) {
            System.err.println("Щось пішло не так під час ініціалізації драйвера: " + e.getMessage());
        }
        finally {
            tearDown();
        }
        selenideSetup();
        allureSetup();
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
