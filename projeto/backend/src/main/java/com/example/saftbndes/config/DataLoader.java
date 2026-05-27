package com.example.saftbndes.config;

import com.example.saftbndes.repository.DesembolsoRepository;
import com.example.saftbndes.service.DesembolsoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DesembolsoService service;

    @Autowired
    private DesembolsoRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            System.out.println("[DataLoader] Banco ja possui dados. Pulando carga do CSV.");
            return;
        }

        var resource = new ClassPathResource("desembolsos-mensais-reduzido.csv");

        if (!resource.exists()) {
            System.out.println("[DataLoader] CSV reduzido nao encontrado em src/main/resources/");
            return;
        }

        var arquivo = resource.getFile();
        System.out.println("[DataLoader] Carregando CSV: " + arquivo.getName() + " (" + (arquivo.length() / 1024 / 1024) + " MB)");

        String resultado = service.carregarCSV(arquivo.getAbsolutePath(), 0);
        System.out.println("[DataLoader] Resultado: " + resultado);
    }
}
