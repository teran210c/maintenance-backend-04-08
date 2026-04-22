package com.pegatron.maintenance.config;

import com.pegatron.maintenance.model.ChecklistTemplate;
import com.pegatron.maintenance.repository.ChecklistTemplateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateDataLoader {

    @Bean
    CommandLineRunner loadTemplates(ChecklistTemplateRepository repository) {
        return args -> {

            if (repository.count() == 0) {

                ChecklistTemplate t1 = new ChecklistTemplate();
                t1.setModuleName("PRINTER");
                t1.setItemName("Clean stencil");

                ChecklistTemplate t2 = new ChecklistTemplate();
                t2.setModuleName("PRINTER");
                t2.setItemName("Check squeegee pressure");

                ChecklistTemplate t3 = new ChecklistTemplate();
                t3.setModuleName("PRINTER");
                t3.setItemName("Verify board alignment");

                repository.save(t1);
                repository.save(t2);
                repository.save(t3);

                System.out.println("Checklist templates loaded.");
            }

        };
    }
}