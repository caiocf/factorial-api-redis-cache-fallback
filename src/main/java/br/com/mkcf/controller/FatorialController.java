package br.com.mkcf.controller;

import br.com.mkcf.service.FatorialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FatorialController {

    private final FatorialService service;

    public FatorialController(FatorialService service) {
        this.service = service;
    }

    @GetMapping("/fatorial/{n}")
    public ResponseEntity<?> calcular(@PathVariable int n) {
        try {
            BigInteger resultado = service.calcular(n);
            return ResponseEntity.ok(Map.of("n", n, "fatorial", String.valueOf(resultado)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
