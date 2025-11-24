package com.huntercodexs.integration.retry.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

@Component
public class MongoRetry {

    private static final Logger log = LoggerFactory.getLogger(MongoRetry.class);

    private final RetryTemplate template;

    public MongoRetry(@Qualifier("mongoRetryTemplateIntegration") RetryTemplate mongoRetryTemplateIntegration) {
        this.template = mongoRetryTemplateIntegration;
    }

    public <T, R extends CrudRepository<T, ?>> T add(R repository, T entity) {
        return template.execute(context -> {
            log.info("Trying to save entity {} attempt #{}", entity, context.getRetryCount() + 1);
            return repository.save(entity);
        });
    }

    public <T, ID, R extends CrudRepository<T, ID>> T findById(R repository, ID id) {
        return template.execute(context -> {
            log.info("Trying to find by id {} attempt #{}", id, context.getRetryCount() + 1);
            return repository.findById(id).orElse(null);
        });
    }

    public <T, R extends CrudRepository<T, ?>> Iterable<T> findAll(R repository) {
        return template.execute(context -> {
            log.info("Trying to find all attempt #{}", context.getRetryCount() + 1);
            return repository.findAll();
        });
    }

    public <T, ID, R extends CrudRepository<T, ID>> boolean deleteById(R repository, ID id) {
        template.execute(context -> {
            log.info("Trying to delete by id {} attempt #{}", id, context.getRetryCount() + 1);
            repository.deleteById(id);
            return true;
        });
        return true;
    }

    public <T, ID, R extends CrudRepository<T, ID>> boolean updateById(R repository, ID id, T newData) {
        if (newData == null) {
            log.warn("New data is null; nothing to update.");
            return false;
        }
        return template.execute(context -> {
            log.info("Trying to update id {} attempt #{}", id, context.getRetryCount() + 1);
            return doUpdate(repository, id, newData);
        });
    }

    private <T, ID> boolean doUpdate(CrudRepository<T, ID> repository, ID id, T newData) {
        return repository.findById(id)
                .map(existing -> {
                    BeanUtils.copyProperties(newData, existing, mergeIgnoreProperties(newData));
                    repository.save(existing);
                    return true;
                })
                .orElse(false);
    }

    private String[] mergeIgnoreProperties(Object src) {
        Set<String> ignore = new HashSet<>();
        ignore.add("id");
        for (PropertyDescriptor pd : BeanUtils.getPropertyDescriptors(src.getClass())) {
            try {
                Object val = pd.getReadMethod() != null ? pd.getReadMethod().invoke(src) : null;
                if (val == null) {
                    ignore.add(pd.getName());
                }
            } catch (Exception e) {
                ignore.add(pd.getName());
            }
        }
        return ignore.toArray(new String[0]);
    }

}
