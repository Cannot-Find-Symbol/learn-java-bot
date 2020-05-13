package org.learn_java.data;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.learn_java.Config;

public abstract class SimpleRepository<T> implements Repository<T> {
    protected DSLContext dslContext = Config.getDslContext();
}