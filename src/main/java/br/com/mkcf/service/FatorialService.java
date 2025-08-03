package br.com.mkcf.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class FatorialService {

    @Cacheable(value = "fatoriais", key = "#n", unless = "#result == null")
    public BigInteger calcular(int n) {
        if (n < 0) throw new IllegalArgumentException("n deve ser >= 0");

        BigInteger resultado = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            resultado = resultado.multiply(BigInteger.valueOf(i));
        }
        return resultado;
    }
}
