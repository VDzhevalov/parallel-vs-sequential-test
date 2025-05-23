package com.vdzhevalov.executionmodes.tests.parallelforks.permethod.readfromfile;

import com.vdzhevalov.executionmodes.datareader.CsvReader;
import com.vdzhevalov.executionmodes.datareader.JsonReader;
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
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Tag("parallelFork_PER_METHOD_FromFile")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@Execution(SAME_THREAD)
public class ReadFromFileParallelForksPerMethodOneByOneTests2 extends RemotePerMethodSetup {
    static final String DESCRIPTION = "ParallelFork ExecutionMode.SAME_THREAD; TestInstance.Lifecycle.PER_METHOD";
    MainPage mainPage = new MainPage();

    @Feature("MainPage Header")
    @Story("MainPage Header Img")
    @Description(DESCRIPTION)
    @Test
    void headerTest2() {
        mainPage.openMainPage()
                .headerShouldHaveImgAsLink(JsonReader.getElementValue("Header link"),JsonReader.getElementValue("Header src"));
        screenshot();
    }

    @Feature("MainPage HomeBanner")
    @Story("HomeBanner Img Link Alt")
    @Description(DESCRIPTION)
    @Test
    void homeBannerTest2() {
        mainPage.openMainPage()
                .homeBannerShouldHaveImgAsLink(JsonReader.getElementValue("HomeBanner link"),JsonReader.getElementValue("HomeBanner src"), JsonReader.getElementValue("HomeBanner alt"));
        screenshot();
    }

    @Feature("MainPage Categories")
    @Description(DESCRIPTION)
    @Test
    void categoriesAmountTest2() {
        mainPage.openMainPage()
                .theCategoriesShouldBe(CsvReader.getDataCountFromCsv(categoriesResourcePath));
        screenshot();
    }

    @Feature("MainPage Categories")
    @Description(DESCRIPTION)
    @Test
    void categoriesContentTest2() {
        mainPage.openMainPage()
                .theCategoriesShouldHaveText(CsvReader.getDataFromCsv(categoriesResourcePath));
        screenshot();
    }

    @Feature("ElementsPage Header")
    @Description(DESCRIPTION)
    @Test
    void elementsPageHeaderTest2() {
        mainPage.openMainPage()
                .goToElementsPage().headerShouldHaveImgAsLink(JsonReader.getElementValue("Header link"),JsonReader.getElementValue("Header src"));
        screenshot();
    }

    @Feature("ElementsPage Left Panel")
    @Description(DESCRIPTION)
    @Test
    void leftPanelTest2() {
        mainPage.openMainPage()
                .goToElementsPage().lefPannelElementsShouldHaveItems(CsvReader.getDataFromCsv(elementGroupItemResourcePath));
        screenshot();
    }

    @Feature("Login Page")
    @ParameterizedTest
    @MethodSource("com.vdzhevalov.executionmodes.datareader.UserData#getDataFromJson")
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
