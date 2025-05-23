package com.vdzhevalov.executionmodes.pages;

import com.codeborne.selenide.ElementsCollection;
import io.qameta.allure.Step;
import com.vdzhevalov.executionmodes.pages.elements.ElementsPage;
import com.vdzhevalov.executionmodes.pages.elements.Header;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.textsInAnyOrder;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class MainPage {

    public MainPage openMainPage() {
        open("/");
        return this;
    }

    private ElementsCollection getAllCards(){
        return $("div.category-cards").$$("div.card");
    }

    @Step("there should be {0} categories")
    public MainPage theCategoriesShouldBe(int amount){
        getAllCards().shouldBe(size(amount));
        return this;
    }

    @Step
    public MainPage theCategoriesShouldHaveText(String... texts){
        getAllCards().shouldHave(textsInAnyOrder(texts));
        return this;
    }

    @Step
    public MainPage headerShouldHaveImgAsLink(String link, String src) {
        new Header().headerShouldHaveImgAsLink(link, src);
        return this;
    }

    @Step
    public void homeBannerShouldHaveImgAsLink(String link, String src, String alt) {
        $("div.home-banner")
                .$("a").shouldHave(href(link))
                .$("img").shouldHave(attribute("src", src))
                .shouldHave(attribute("alt", alt));
    }

    @Step
    public ElementsPage goToElementsPage(){
        getAllCards().filterBy(text("Elements")).get(0).click();
        return new ElementsPage();
    }
}