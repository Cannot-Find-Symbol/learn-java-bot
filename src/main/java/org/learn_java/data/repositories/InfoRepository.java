package org.learn_java.data.repositories;

import org.learn_java.data.dto.InfoDTO;

import static org.learn_java.db.autogen.learnjava.tables.Info.INFO;

import java.util.List;

public class InfoRepository extends SimpleRepository<InfoDTO> {


    public void add(InfoDTO item) {
        dslContext.insertInto(INFO, INFO.TAG_NAME, INFO.MESSAGE).values(item.getTagName(), item.getMessage()).execute();
    }

    public int update(InfoDTO item) {
        return dslContext.update(INFO).set(INFO.MESSAGE, item.getMessage()).where(INFO.TAG_NAME.eq(item.getTagName())).execute();
    }

    public int remove(String tagName) {
        return dslContext.delete(INFO).where(INFO.TAG_NAME.eq(tagName)).execute();
    }

    public List<InfoDTO> getAll() {
        return dslContext.selectFrom(INFO).fetchInto(InfoDTO.class);
    }


    public InfoDTO findByName(String name) {
        return dslContext.selectFrom(INFO).where(INFO.TAG_NAME.eq(name)).fetchOptional().map(e -> e.into(InfoDTO.class)).orElse(null);

    }

}
