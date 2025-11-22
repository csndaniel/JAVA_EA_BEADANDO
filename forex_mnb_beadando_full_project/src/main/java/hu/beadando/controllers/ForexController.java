package hu.beadando.controllers;

import hu.beadando.models.ForexAccount;
import hu.beadando.models.Position;
import hu.beadando.services.ForexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ForexController {

    private final ForexService forexService;

    @Autowired
    public ForexController(ForexService forexService) {
        this.forexService = forexService;
    }

    @GetMapping("/forex-account")
    public String account(Model model) {
        ForexAccount account = forexService.getAccountInfo();
        model.addAttribute("account", account);
        return "forex-account";
    }

    @GetMapping("/forex-aktar")
    public String aktuArForm(Model model) {
        model.addAttribute("instruments", forexService.getSupportedInstruments());
        return "forex-aktar";
    }

    @PostMapping("/forex-aktar")
    public String aktuArResult(@RequestParam("instrument") String instrument,
                               Model model) {
        model.addAttribute("instruments", forexService.getSupportedInstruments());
        double price = forexService.getCurrentPrice(instrument);
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("price", price);
        return "forex-aktar";
    }

    @GetMapping("/forex-histar")
    public String histArForm(Model model) {
        model.addAttribute("instruments", forexService.getSupportedInstruments());
        model.addAttribute("granularities", forexService.getSupportedGranularities());
        return "forex-histar";
    }

    @PostMapping("/forex-histar")
    public String histArResult(@RequestParam("instrument") String instrument,
                               @RequestParam("granularity") String granularity,
                               Model model) {
        model.addAttribute("instruments", forexService.getSupportedInstruments());
        model.addAttribute("granularities", forexService.getSupportedGranularities());
        List<Double> prices = forexService.getHistoricPrices(instrument, granularity);
        model.addAttribute("selectedInstrument", instrument);
        model.addAttribute("selectedGranularity", granularity);
        model.addAttribute("prices", prices);
        return "forex-histar";
    }

    @GetMapping("/forex-nyit")
    public String nyitForm(Model model) {
        model.addAttribute("instruments", forexService.getSupportedInstruments());
        return "forex-nyit";
    }

    @PostMapping("/forex-nyit")
    public String nyit(@RequestParam("instrument") String instrument,
                       @RequestParam("units") int units) {
        forexService.openPosition(instrument, units);
        return "redirect:/forex-poz";
    }

    @GetMapping("/forex-poz")
    public String pozicioLista(Model model) {
        List<Position> openPositions = forexService.getOpenPositions();
        model.addAttribute("positions", openPositions);
        return "forex-poz";
    }

    @GetMapping("/forex-zar")
    public String zarForm(Model model) {
        model.addAttribute("tradeIds", forexService.getOpenTradeIds());
        return "forex-zar";
    }

    @PostMapping("/forex-zar")
    public String zar(@RequestParam("tradeId") long tradeId) {
        forexService.closePosition(tradeId);
        return "redirect:/forex-poz";
    }
}
