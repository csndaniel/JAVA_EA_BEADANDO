package hu.beadando.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Rate {

    private LocalDate date;
    private String currency;
    private BigDecimal value;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
