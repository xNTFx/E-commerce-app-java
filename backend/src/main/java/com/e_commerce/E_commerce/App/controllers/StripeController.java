package com.e_commerce.E_commerce.App.controllers;

import com.e_commerce.E_commerce.App.service.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/stripe")
public class StripeController {

    private final StripeService stripeService;

    @Autowired
    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @GetMapping("/config")
    public Map<String, String> getConfig() {
        return stripeService.getStripeConfig();
    }

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent() throws StripeException {
        return stripeService.createPaymentIntent();
    }
}
