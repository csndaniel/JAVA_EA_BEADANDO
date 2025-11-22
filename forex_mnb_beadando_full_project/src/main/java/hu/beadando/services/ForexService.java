package hu.beadando.services;

import hu.beadando.models.ForexAccount;
import hu.beadando.models.Position;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ForexService {

    private final Map<String, Double> instrumentPrices = new HashMap<>();
    private final Map<Long, Position> openPositions = new ConcurrentHashMap<>();
    private final AtomicLong tradeIdGenerator = new AtomicLong(1);

    public ForexService() {
        instrumentPrices.put("EUR_USD", 1.0850);
        instrumentPrices.put("GBP_USD", 1.2650);
        instrumentPrices.put("USD_JPY", 155.20);
        instrumentPrices.put("EUR_HUF", 395.50);
        instrumentPrices.put("USD_HUF", 365.30);
    }

    public List<String> getSupportedInstruments() {
        return new ArrayList<>(instrumentPrices.keySet());
    }

    public List<String> getSupportedGranularities() {
        return Arrays.asList("D", "H4", "H1");
    }

    public ForexAccount getAccountInfo() {
        ForexAccount acc = new ForexAccount();
        acc.setAccountId("DEMO-ACCOUNT-001");
        acc.setBalance(10000.0);
        acc.setEquity(10000.0 + openPositions.size() * 10);
        acc.setMarginUsed(openPositions.size() * 50.0);
        acc.setOpenPositions(openPositions.size());
        return acc;
    }

    public double getCurrentPrice(String instrument) {
        return instrumentPrices.getOrDefault(instrument, 0.0);
    }

    public List<Double> getHistoricPrices(String instrument, String granularity) {
        double base = getCurrentPrice(instrument);
        if (base <= 0) base = 1.0;
        List<Double> list = new ArrayList<>();
        Random rnd = new Random(42);
        for (int i = 0; i < 10; i++) {
            double change = (rnd.nextDouble() - 0.5) * (base * 0.01);
            list.add(Math.round((base + change) * 10000.0) / 10000.0);
        }
        return list;
    }

    public Position openPosition(String instrument, int units) {
        double price = getCurrentPrice(instrument);
        long id = tradeIdGenerator.getAndIncrement();
        Position p = new Position();
        p.setTradeId(id);
        p.setInstrument(instrument);
        p.setUnits(units);
        p.setOpenPrice(price);
        p.setOpenTime(LocalDateTime.now());
        p.setDirection(units >= 0 ? "LONG" : "SHORT");
        openPositions.put(id, p);
        return p;
    }

    public List<Position> getOpenPositions() {
        return new ArrayList<>(openPositions.values());
    }

    public List<Long> getOpenTradeIds() {
        return new ArrayList<>(openPositions.keySet());
    }

    public boolean closePosition(long tradeId) {
        return openPositions.remove(tradeId) != null;
    }
}
