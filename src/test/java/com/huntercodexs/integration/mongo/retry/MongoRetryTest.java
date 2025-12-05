package com.huntercodexs.integration.mongo.retry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.retry.support.RetryTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MongoRetryTest {

    RetryTemplate retryTemplate;
    MongoRetry mongoRetry;

    @SuppressWarnings("unchecked")
    CrudRepository<TestEntity, String> repository;

    static class TestEntity {
        String id;
        String name;
        String desc;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        @Override public String toString() { return "TestEntity{id=" + id + ",name=" + name + ",desc=" + desc + "}"; }
    }

    @BeforeEach
    void setup() {
        retryTemplate = new RetryTemplate();
        mongoRetry = new MongoRetry(retryTemplate);
        repository = mock(CrudRepository.class);
    }

    @Test
    void add_saves_entity_with_retry() {
        TestEntity input = new TestEntity();
        input.setId("1");
        input.setName("A");

        when(repository.save(input)).thenReturn(input);

        TestEntity saved = mongoRetry.add(repository, input);

        assertNotNull(saved);
        assertEquals("1", saved.getId());
        verify(repository, times(1)).save(input);
    }

    @Test
    void findById_returns_entity_when_present() {
        TestEntity e = new TestEntity();
        e.setId("2");
        e.setName("B");

        when(repository.findById("2")).thenReturn(Optional.of(e));

        TestEntity found = mongoRetry.findById(repository, "2");

        assertNotNull(found);
        assertEquals("B", found.getName());
        verify(repository, times(1)).findById("2");
    }

    @Test
    void findById_returns_null_when_absent() {
        when(repository.findById("x")).thenReturn(Optional.empty());

        TestEntity found = mongoRetry.findById(repository, "x");

        assertNull(found);
        verify(repository, times(1)).findById("x");
    }

    @Test
    void findAll_returns_iterable() {
        TestEntity e1 = new TestEntity(); e1.setId("1");
        TestEntity e2 = new TestEntity(); e2.setId("2");

        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));

        Iterable<TestEntity> all = mongoRetry.findAll(repository);

        assertNotNull(all);
        assertEquals(2, ((java.util.Collection<?>) all).size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void deleteById_executes_and_returns_true() {
        doNothing().when(repository).deleteById("3");

        boolean result = mongoRetry.deleteById(repository, "3");

        assertTrue(result);
        verify(repository, times(1)).deleteById("3");
    }

    @Test
    void updateById_returns_false_when_newData_is_null() {
        boolean updated = mongoRetry.updateById(repository, "4", null);
        assertFalse(updated);
        verify(repository, times(0)).findById(any());
    }

    @Test
    void updateById_updates_existing_entity_copying_non_null_fields() {
        TestEntity existing = new TestEntity();
        existing.setId("5");
        existing.setName("old");
        existing.setDesc("keep");

        TestEntity newData = new TestEntity();
        newData.setId("DIFFERENT"); // should be ignored
        newData.setName("new");
        newData.setDesc(null); // null should be ignored

        when(repository.findById("5")).thenReturn(Optional.of(existing));
        ArgumentCaptor<TestEntity> captor = ArgumentCaptor.forClass(TestEntity.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean updated = mongoRetry.updateById(repository, "5", newData);

        assertTrue(updated);
        verify(repository).save(captor.capture());
        TestEntity saved = captor.getValue();

        assertEquals("5", saved.getId());       // id not overwritten
        assertEquals("new", saved.getName());   // non-null copied
        assertEquals("keep", saved.getDesc());  // null ignored
    }

    @Test
    void updateById_returns_false_when_entity_not_found() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        TestEntity newData = new TestEntity();
        newData.setName("x");

        boolean updated = mongoRetry.updateById(repository, "missing", newData);

        assertFalse(updated);
        verify(repository, times(1)).findById("missing");
        verify(repository, times(0)).save(any());
    }
}