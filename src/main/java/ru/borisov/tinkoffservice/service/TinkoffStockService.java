package ru.borisov.tinkoffservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import ru.borisov.tinkoffservice.dto.*;
import ru.borisov.tinkoffservice.exception.StockNotFoundException;
import ru.borisov.tinkoffservice.model.Currency;
import ru.borisov.tinkoffservice.model.Stock;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@EnableAsync
@Slf4j
public class TinkoffStockService implements StockService {

    private final OpenApi openApi;

    @Async
    public CompletableFuture<MarketInstrumentList> getMarketInstrumentByTicker(String ticker) {
        var context = openApi.getMarketContext();
        return context.searchMarketInstrumentsByTicker(ticker);
    }

    @Override
    public Stock getStockByTicker(String ticker) {
        var cf = getMarketInstrumentByTicker(ticker);
        var list = cf.join().getInstruments();
        if (list.isEmpty()) {
            throw new StockNotFoundException(String.format("Stock %S not found.", ticker));
        }

        var item = list.get(0);
        return  new Stock(
                item.getTicker(),
                item.getFigi(),
                item.getName(),
                item.getType().getValue(),
                Currency.valueOf(item.getCurrency().getValue()),
                "TINKOFF"
        );
    }

    @Override
    public StocksDto getStocksByTickers(TickersDto tickers) {
        List<CompletableFuture<MarketInstrumentList>> marketInstrument = new ArrayList<>();
        tickers.getTickers().forEach(ticker -> marketInstrument.add(getMarketInstrumentByTicker(ticker))); // каждый ticker передаем в асинхронный метод, почти мгновенно получаем ответы по всем ticker'ам
        List<Stock> stocks = marketInstrument.stream()
                .map(CompletableFuture::join) // ждем пока наш List заполнится полностью MarketInstrumentList'ами
                .map(mi -> {
                    if(!mi.getInstruments().isEmpty()) {
                        return mi.getInstruments().get(0);
                    }
                    return null;
                })
                .filter(el -> Objects.nonNull(el))
                .map(mi -> new Stock(
                        mi.getTicker(),
                        mi.getFigi(),
                        mi.getName(),
                        mi.getType().getValue(),
                        Currency.valueOf(mi.getCurrency().getValue()),
                        "TINKOFF"
                ))
                .collect(Collectors.toList());

        return new StocksDto(stocks);
    }

    @Async
    public CompletableFuture<Optional<Orderbook>> getOrderBookByFigi(String figi) {
        var orderBook = openApi.getMarketContext().getMarketOrderbook(figi, 0);
        log.info("Getting price {} from Tinkoff", figi);
        return orderBook;
    }

    @Override
    public StocksPricesDto getPrices(FigiesDto figiesDto) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<Optional<Orderbook>>> orderBooks = new ArrayList<>();
        figiesDto.getFigies().forEach(figi -> orderBooks.add(getOrderBookByFigi(figi)));
        var listPrices =  orderBooks.stream()
                .map(CompletableFuture::join)
                .map(ob -> ob.orElseThrow(() -> new StockNotFoundException("Stock not found")))
                .map(orderbook -> new StockPrice(
                        orderbook.getFigi(),
                        orderbook.getLastPrice().doubleValue()
                ))
                .collect(Collectors.toList());
        log.info("Time - {}", System.currentTimeMillis() - start);
        return new StocksPricesDto(listPrices);
    }
}
