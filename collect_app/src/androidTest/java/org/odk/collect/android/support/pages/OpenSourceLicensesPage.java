package org.odk.collect.android.support.pages;

class OpenSourceLicensesPage extends Page<OpenSourceLicensesPage> {

    @Override
    public OpenSourceLicensesPage assertOnPage() {
        asyncAssertText("Open Source Licenses");
        checkIfWebViewActivityIsDisplayed();
        return this;
    }
}
