package ru.borisov.tinkoffservice.service;

import ru.borisov.tinkoffservice.dto.FigiesDto;
import ru.borisov.tinkoffservice.dto.StocksDto;
import ru.borisov.tinkoffservice.dto.StocksPricesDto;
import ru.borisov.tinkoffservice.dto.TickersDto;
import ru.borisov.tinkoffservice.model.Stock;

public interface StockService {
    Stock getStockByTicker(String ticker);
    StocksDto getStocksByTickers(TickersDto tickers);
    StocksPricesDto getPrices(FigiesDto figiesDto);
}
