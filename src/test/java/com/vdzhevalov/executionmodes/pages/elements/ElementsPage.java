package com.vdzhevalov.executionmodes.pages.elements;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.textsInAnyOrder;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.$;

public class ElementsPage {
    SelenideElement leftPannel = $("div.left-pannel div.accordion");
    SelenideElement elementsPannel = leftPannel.$$(".element-group").findBy(text("Elements"));
    ElementsCollection buttons = elementsPannel.$$(".element-list.collapse.show .btn");

    @Step
    public ElementsPage headerShouldHaveImgAsLink(String link, String src) {
        new Header().headerShouldHaveImgAsLink(link, src);
        return this;
    }



    @Step
    public ElementsPage lefPannelElementsShouldHaveItems(String... groupsName){
        buttons.shouldHave(textsInAnyOrder(groupsName));
        return this;
    }

    @Step
    public ElementsPage openTextBoxMenu(){
        buttons.findBy(text("Text Box")).click();
        return this;
    }

    @Step
    public ElementsPage fillTexBoxes(String fullName, String email, String currentAddress, String permanentAddress){
        $("#userName").setValue(fullName);
        $("#userEmail").setValue(email);
        $("#currentAddress").setValue(currentAddress);
        $("#permanentAddress").setValue(permanentAddress);

        $("#userName").shouldHave(value(fullName));
        $("#userEmail").shouldHave(value(email));
        $("#currentAddress").shouldHave(value(currentAddress));
        $("#permanentAddress").shouldHave(value(permanentAddress));
        return this;
    }
}
