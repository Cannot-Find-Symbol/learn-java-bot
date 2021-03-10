package org.learn_java.bot.configuration;

import org.learn_java.bot.data.dtos.InfoDTO;
import org.learn_java.bot.data.dtos.SpamDTO;
import org.learn_java.bot.data.dtos.WarnDTO;
import org.learn_java.bot.data.entities.Info;
import org.learn_java.bot.data.entities.Spam;
import org.learn_java.bot.data.entities.Warn;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class Mapper {
    private ModelMapper mapper;

    public Mapper() {
        this.mapper = new ModelMapper();
    }

    public ModelMapper getMapper() {
        return this.mapper;
    }

    public SpamDTO mapToSpamDTO(Spam spam){
        return mapper.map(spam, SpamDTO.class);
    }

    public WarnDTO mapToWarnDTO(Warn warn){
        return mapper.map(warn, WarnDTO.class);
    }

    public InfoDTO mapToInfoDTO(Info info){
        return mapper.map(info, InfoDTO.class);
    }

}
