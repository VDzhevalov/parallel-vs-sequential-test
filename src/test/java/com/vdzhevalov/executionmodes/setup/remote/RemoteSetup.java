package com.vdzhevalov.executionmodes.setup.remote;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Attachment;
import io.qameta.allure.selenide.AllureSelenide;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeOptions;

public abstract class RemoteSetup {
    protected final static String BASE_URL = "https://demoqa.com";

    public ChromeOptions chromeSetup(){
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--headless=new");
        return options;
    }

    public void selenideSetup(ChromeOptions options){
        Configuration.browserCapabilities = options;
        selenideSetup();
    }

    public void selenideSetup(){
        Configuration.baseUrl = BASE_URL;
        Configuration.browserSize = "1920x1080";
        Configuration.browser = "chrome";
        Configuration.remote = "http://selenium-hub:4444/wd/hub";
    }

    public void setupAll(){
        selenideSetup(chromeSetup());
        allureSetup();
    }

    public void allureSetup(){
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(true));
    }

    @Attachment(value = "Screenshot", type = "image/png")
    public byte[] screenshot() {
        return ((TakesScreenshot) WebDriverRunner.getWebDriver())
                .getScreenshotAs(OutputType.BYTES);
    }

}
