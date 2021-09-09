package org.learn_java.bot.configuration;

import org.learn_java.ekmc.PistonService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class SpringConfiguration {
    @Bean
    public PistonService createPistonService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://emkc.org/api/v2/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(PistonService.class);
    }
}
