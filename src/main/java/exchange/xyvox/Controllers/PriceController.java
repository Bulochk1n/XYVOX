package exchange.xyvox.Controllers;

import exchange.xyvox.Services.CoinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/xyvox/api/v1")
public class PriceController {
    private final CoinService coinService;
    public PriceController(CoinService coinService) {
        this.coinService = coinService;
    }
    @GetMapping("/price") //BTC, ETH, etc
    public Map<String, Double> getPrice(@RequestParam String symbol) {
        Double price = coinService.getCoinBySymbol(symbol).getPrice();
        System.out.println(symbol + ": " + price);
        return Collections.singletonMap("price", price != null ? price : 0.0);
    }
}

