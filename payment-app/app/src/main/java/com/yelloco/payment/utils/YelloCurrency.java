package com.yelloco.payment.utils;

import android.util.Log;

public enum YelloCurrency {
    AED("AED", 784, 2, "United Arab Emirates", "Dirham"),
    AUD("AUD", 36, 2, "Australia", "Australian Dollar"),
    BRL("BRL", 986, 2, "Brazil", "Brazilian Real"),
    BYR("BYR", 974, 0, "Belarus", "Belarussian Ruble"),
    BGN("BGN", 975, 2, "Bulgaria", "Bulgarian Lev"),
    CAD("CAD", 124, 2, "Canada", "Canadian Dollar"),
    DKK("DKK", 208, 2, "Denmark", "Danish Krone"),
    EUR("EUR", 978, 2, "European Union", "Euro"),
    CNY("CNY", 156, 2, "China", "Yuan Renminbi"),
    CZK("CZK", 203, 2, "Czech Republic", "Czech Koruna"),
    HKD("HKD", 344, 2, "Hong Kong", "Hong Kong Dollar"),
    HUF("HUF", 348, 2, "Hungary", "Forint"),
    HRK("HRK", 191, 2, "Croatia/Hrvatska", "Croatian Kuna"),
    ISK("ISK", 352, 0, "Iceland", "Iceland Krona"),
    ILS("ILS", 376, 2, "New Israeli", "Sheqel"),
    MDL("MDL", 498, 2, "Moldova, Republic of Moldovan", "Leu"),
    MYR("MYR", 458, 2, "Malaysia", "Malaysian Ringgit"),
    NOK("NOK", 578, 2, "Norway", "Norwegian Krone"),
    NZD("NZD", 554, 2, "New Zealand", "New Zealand Dollar"),
    JPY("JPY", 392, 0, "Japan", "Yen"),
    LVL("LVL", 428, 2, "Latvia", "Latvian lats"),
    //(now EUR)
    LTL("LTL", 440, 2, "Lithuania", "Lithuanian Litas"),
    //(now EUR)
    PHP("PHP", 608, 2, "Philippines", "Philippine Peso"),
    PLN("PLN", 985, 2, "Poland", "Zloty"),
    ROL("ROL", 642, 2, "Romania", "Leu"),
    RON("RON", 946, 2, "Romania", "new Leu"),
    RUB("RUB", 810, 2, "Russian Federation", "Russian Ruble"),
    RUR("RUR", 643, 2, "Russian Federation", "Russian Ruble"),
    SEK("SEK", 752, 2, "Sweden", "Swedish Krona"),
    SKK("SKK", 703, 2, "Slovak Republic", "Slovak Koruna"),
    //(now EUR)
    TRY("TRY", 949, 2, "Turkey", "Yeni Türk Liras"),
    //on 1 January 2005 New Turkish Lira replaced Turkish Lira (TRL)	TRY	949
    CHF("CHF", 756, 2, "Switzerland", "Swiss Franc"),
    UAH("UAH", 980, 2, "Ukraine", "Hryvnia"),
    GBP("GBP", 826, 2, "United Kingdom", "Pound Sterling"),
    USS("USS", 998, 2, "United States", "US Dollar"),
    //r (Same day)
    USN("USN", 997, 2, "United States", "US Dollar"),
    //r (Next day)
    USD("USD", 840, 2, "USA", "US Dollar"),
    //out of USA
    VND("VND", 704, 0, "Viet Nam", "Dong"),
    TND("TND", 788, 3, "Tunisia", "Tunisian Dinar");

    private static final String TAG = YelloCurrency.class.getSimpleName();

    private String countryName;
    private Integer numericCode;
    private String alphabeticCode;
    private String currencyName;
    private Integer currencyExponent;

    YelloCurrency(String alphabeticCode, Integer numericCode, Integer exponent, String countryName,
            String currencyName) {
        this.alphabeticCode = alphabeticCode;
        this.numericCode = numericCode;
        this.countryName = countryName;
        this.currencyName = currencyName;
        this.currencyExponent = exponent;
    }

    public static YelloCurrency getByCurrencyAlphabeticCode(String currencyAlphabeticCode) {
        for (YelloCurrency currency : YelloCurrency.values()) {
            if (currency.getAlphabeticCode().equalsIgnoreCase(currencyAlphabeticCode)) {
                return currency;
            }
        }
        return null;
    }

    public static YelloCurrency getByCurrencyNumericCode(int currencyNumericCode) {
        for (YelloCurrency currency : YelloCurrency.values()) {
            if (currency.getNumericCode() == currencyNumericCode) {
                return currency;
            }
        }
        return null;
    }

    public static YelloCurrency getByCurrencyNumericCode(String currencyNumericCode) {
        if (currencyNumericCode == null || currencyNumericCode.isEmpty()) {
            return null;
        }

        int numericCodeInt = Integer.parseInt(currencyNumericCode);
        return YelloCurrency.getByCurrencyNumericCode(numericCodeInt);
    }

    /**
     * Looks up the currency in the currency table
     *
     * @param code is either numerical or alphabetic currency code
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.length() < 2) {
            return false;
        }

        try {
            if (code.matches("[0-9]+")) {
                // It is a numeric code
                YelloCurrency currency = YelloCurrency.getByCurrencyNumericCode(code);
                if (currency != null) {
                    return true;
                }
            } else {
                // It is a currency code
                YelloCurrency currency = YelloCurrency.getByCurrencyAlphabeticCode(code);
                if (currency != null) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse currency code code");
        }
        return false;
    }

    public String getCountryName() {
        return countryName;
    }

    public Integer getNumericCode() {
        return numericCode;
    }

    public String getAlphabeticCode() {
        return alphabeticCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public Integer getCurrencyExponent() {
        return currencyExponent;
    }

    @Override
    public String toString() {
        return "YelloCurrency{" +
                "countryName='" + countryName + '\'' +
                ", numericCode=" + numericCode +
                ", alphabeticCode='" + alphabeticCode + '\'' +
                ", currencyName='" + currencyName + '\'' +
                ", currencyExponent=" + currencyExponent +
                '}';
    }
}