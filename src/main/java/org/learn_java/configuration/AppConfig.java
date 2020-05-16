package org.learn_java.configuration;

import org.learn_java.commands.Code;
import org.learn_java.commands.Format;
import org.learn_java.commands.Info;
import org.learn_java.data.repositories.InfoRepository;
import org.learn_java.event.listeners.code_block.CodeBlockListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Bean
  public Code Code() {
    return new Code();
  }

  @Bean
  public Format Format() {
    return new Format();
  }

  @Bean
  public Info Info(InfoRepository repository) {
    return new Info(repository);
  }

  @Bean
  public Config Config() {
    return new Config();
  }

  @Bean
  public CodeBlockListener CodeBlockListener() {
    return new CodeBlockListener();
  }
}
