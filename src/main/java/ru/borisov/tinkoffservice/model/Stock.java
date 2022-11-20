package ru.borisov.tinkoffservice.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Stock {
    private String ticker;
    private String figi;
    private String name;
    private String type;
    private Currency currency;
    private String source;

}
