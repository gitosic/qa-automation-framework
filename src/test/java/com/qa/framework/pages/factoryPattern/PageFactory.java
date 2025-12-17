package com.qa.framework.pages.factoryPattern;

import com.codeborne.selenide.Selenide;
import com.qa.framework.pages.BasePage;
import com.qa.framework.pages.DashboardPage;
import com.qa.framework.pages.LoginPage;

public class PageFactory {

    public static LoginPage createLoginPage() {
        return Selenide.page(LoginPage.class);
    }

    public static DashboardPage createDashboardPage() {
        return Selenide.page(DashboardPage.class);
    }

    public static <T extends BasePage> T createPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }
}