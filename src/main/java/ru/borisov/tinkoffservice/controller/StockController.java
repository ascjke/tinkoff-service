package ru.borisov.tinkoffservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.borisov.tinkoffservice.dto.FigiesDto;
import ru.borisov.tinkoffservice.dto.StocksDto;
import ru.borisov.tinkoffservice.dto.StocksPricesDto;
import ru.borisov.tinkoffservice.dto.TickersDto;
import ru.borisov.tinkoffservice.model.Stock;
import ru.borisov.tinkoffservice.service.StockService;

@RestController
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/stocks/{ticker}")
    public Stock getStock(@PathVariable String ticker) {
        return stockService.getStockByTicker(ticker);
    }

    @PostMapping("/stocks/getStocksByTickers")
    public StocksDto getStocksByTickers(@RequestBody TickersDto tickersDto) {
        return stockService.getStocksByTickers(tickersDto);
    }

    @PostMapping("/prices")
    public StocksPricesDto getPrices(@RequestBody FigiesDto figiesDto) {
        return stockService.getPrices(figiesDto);
    }
}
