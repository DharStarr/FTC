package net.forthecrown.log;

import com.mojang.serialization.Dynamic;

public interface LogDataFixer {
    <S> Dynamic<S> update(Dynamic<S> dynamic);
}