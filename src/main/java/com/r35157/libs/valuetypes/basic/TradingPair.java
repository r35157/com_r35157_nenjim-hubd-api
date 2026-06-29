package com.r35157.libs.valuetypes.basic;

public record TradingPair(
        CurrencyType base,  // The thing you are buying or selling.
        CurrencyType quote  // The currency/unit used to price the base asset.
) { }
