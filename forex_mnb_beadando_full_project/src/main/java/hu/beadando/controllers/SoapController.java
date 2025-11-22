package hu.beadando.controllers;

import hu.beadando.models.Rate;
import hu.beadando.services.SoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SoapController {

    private final SoapService soapService;

    @Autowired
    public SoapController(SoapService soapService) {
        this.soapService = soapService;
    }

    @GetMapping("/soap")
    public String showForm() {
        return "soap";
    }

    @PostMapping("/soap")
    public String getRates(
            @RequestParam("currency") String currency,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        try {
            List<Rate> rates = soapService.getRates(currency, startDate, endDate);
            model.addAttribute("currency", currency);
            model.addAttribute("rates", rates);
            List<String> dates = rates.stream()
                    .map(r -> r.getDate().toString())
                    .collect(Collectors.toList());
            List<Double> values = rates.stream()
                    .map(r -> r.getValue().doubleValue())
                    .collect(Collectors.toList());
            model.addAttribute("dates", dates);
            model.addAttribute("values", values);
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt az MNB szolgáltatás elérésekor: " + e.getMessage());
        }

        return "soap";
    }
}
