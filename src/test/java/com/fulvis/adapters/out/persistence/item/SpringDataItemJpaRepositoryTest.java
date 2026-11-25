package com.fulvis.adapters.out.persistence.item;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SpringDataItemJpaRepositoryTest {

    @Test
    void givenFindAllByIdsForUpdateThenItDeclaresPessimisticWriteLock() throws NoSuchMethodException {
        Method method = SpringDataItemJpaRepository.class.getMethod("findAllByIdsForUpdate", Collection.class);

        Lock lock = method.getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
