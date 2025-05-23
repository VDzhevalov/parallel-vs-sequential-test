package com.vdzhevalov.executionmodes.pages.elements;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.href;
import static com.codeborne.selenide.Selenide.$;

public class Header {

    public Header headerShouldHaveImgAsLink(String link, String src) {
        $("header")
                .$("a").shouldHave(href(link))
                .$("img").shouldHave(attribute("src", src));
        return this;
    }
}
