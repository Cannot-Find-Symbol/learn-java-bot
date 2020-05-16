package org.learn_java.data.repositories;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.learn_java.Config;

public abstract class SimpleRepository<T> {
    protected DSLContext dslContext = Config.getDslContext();
}