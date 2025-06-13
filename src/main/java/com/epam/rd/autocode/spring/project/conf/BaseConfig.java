package com.epam.rd.autocode.spring.project.conf;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfig{

    @Bean
    public ModelMapper mapper(){
        return new ModelMapper();
    }
}
