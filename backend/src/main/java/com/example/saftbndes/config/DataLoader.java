package com.example.saftbndes.config;

import com.example.saftbndes.service.DesembolsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DesembolsoService service;

    @Override
    public void run(String... args) throws Exception {
        String caminho = System.getenv("BNDES_CSV_PATH");
        if (caminho == null || caminho.isBlank()) {
            System.out.println("[DataLoader] Nenhum CSV configurado. Defina a variavel BNDES_CSV_PATH");
            System.out.println("[DataLoader] Ex: BNDES_CSV_PATH=C:/Users/Thiago/Downloads/desembolsos-mensais-reduzido.csv");
            return;
        }

        System.out.println("[DataLoader] Carregando CSV de: " + caminho);
        String resultado = service.carregarCSV(caminho, 50000);
        System.out.println("[DataLoader] Resultado: " + resultado);
    }
}
