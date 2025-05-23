package com.vdzhevalov.executionmodes.tests.parallelforks.permethod.inline;

import com.vdzhevalov.executionmodes.pages.MainPage;
import com.vdzhevalov.executionmodes.setup.remote.RemotePerMethodSetup;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Tag("parallelFork_PER_METHOD_Inline")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(SAME_THREAD)
public class InlineParallelForksPerMethodOneByOneTests2 extends RemotePerMethodSetup {

    static final String DESCRIPTION = "ParallelFork ExecutionMode.SAME_THREAD; TestInstance.Lifecycle.PER_METHOD; Inline";
    MainPage mainPage = new MainPage();

    @Feature("MainPage Header")
    @Story("MainPage Header Img")
    @Description(DESCRIPTION)
    @Test
    void headerTest2() {
        mainPage.openMainPage()
                .headerShouldHaveImgAsLink("https://demoqa.com","https://demoqa.com/images/Toolsqa.jpg");
        screenshot();
    }

    @Feature("MainPage HomeBanner")
    @Story("HomeBanner Img Link Alt")
    @Description(DESCRIPTION)
    @Test
    void homeBannerTest2() {
        mainPage.openMainPage()
                .homeBannerShouldHaveImgAsLink("https://www.toolsqa.com/selenium-training","https://demoqa.com/images/WB.svg", "Selenium Online Training");
        screenshot();
    }

    @Feature("MainPage Categories")
    @Description(DESCRIPTION)
    @Test
    void categoriesAmountTest2() {
        mainPage.openMainPage()
                .theCategoriesShouldBe(6);
        screenshot();
    }

    @Feature("MainPage Categories")
    @Description(DESCRIPTION)
    @Test
    void categoriesContentTest2() {
        mainPage.openMainPage()
                .theCategoriesShouldHaveText("Elements", "Forms", "Alerts, Frame & Windows", "Widgets", "Interactions", "Book Store Application");
        screenshot();
    }

    @Feature("ElementsPage Header")
    @Description(DESCRIPTION)
    @Test
    void elementsPageHeaderTest2() {
        mainPage.openMainPage()
                .goToElementsPage().headerShouldHaveImgAsLink("https://demoqa.com","https://demoqa.com/images/Toolsqa.jpg");
        screenshot();
    }

    @Feature("ElementsPage Left Panel")
    @Description(DESCRIPTION)
    @Test
    void leftPanelTest2() {
        mainPage.openMainPage()
                .goToElementsPage().lefPannelElementsShouldHaveItems("Text Box", "Check Box", "Radio Button", "Web Tables", "Buttons", "Links", "Broken Links - Images", "Upload and Download", "Dynamic Properties");
        screenshot();
    }

    @Feature("Login Page")
    @ParameterizedTest
    @CsvSource({
            "fullName1, email1, currentAddress1, permanentAddress1",
            "fullName2, email2, currentAddress2, permanentAddress2",
            "fullName3, email3, currentAddress3, permanentAddress3",})
    void testInputFields2(String fullName, String email, String currentAddress, String permanentAddress){
        mainPage.openMainPage()
                .goToElementsPage()
                .openTextBoxMenu()
                .fillTexBoxes(fullName, email, currentAddress, permanentAddress);
        screenshot();
    }


    @Feature("Demonstration of a failed test")
    @Description(DESCRIPTION)
    @Test
    void iWillFail2() {
        mainPage.openMainPage()
                .theCategoriesShouldBe(5);
        screenshot();
    }
}
