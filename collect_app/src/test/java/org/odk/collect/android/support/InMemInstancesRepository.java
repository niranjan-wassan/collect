package org.odk.collect.android.support;

import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class InMemInstancesRepository implements InstancesRepository {

    List<Instance> instances;

    public InMemInstancesRepository(List<Instance> instances) {
        this.instances = new ArrayList<>(instances);
    }

    public InMemInstancesRepository() {
        this.instances = new ArrayList<>();
    }

    @Override
    public Instance get(Long databaseId) {
        for (Instance instance : instances) {
            if (instance.getId().equals(databaseId)) {
                return instance;
            }
        }

        return null;
    }

    @Override
    public Instance getOneByPath(String instancePath) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getAbsoluteInstanceFilePath().equals(instancePath)) {
                result.add(instance);
            }
        }

        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<Instance> getAllNotDeleted() {
        return instances.stream()
                .filter(instance -> instance.getDeletedDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Instance> getAllByStatus(String... status) {
        List<String> statuses = Arrays.asList(status);
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (statuses.contains(instance.getStatus())) {
                result.add(instance);
            }
        }

        return result;
    }

    @Override
    public List<Instance> getAllByFormId(String formId) {
        List<Instance> result = new ArrayList<>();

        for (Instance instance : instances) {
            if (instance.getJrFormId().equals(formId)) {
                result.add(instance);
            }
        }

        return result;
    }

    @Override
    public List<Instance> getAllNotDeletedByFormIdAndVersion(String formId, String version) {
        return instances.stream().filter(instance -> {
            return Objects.equals(instance.getJrFormId(), formId)
                    && Objects.equals(instance.getJrVersion(), version)
                    && instance.getDeletedDate() == null;
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        instances.removeIf(instance -> instance.getId().equals(id));
    }

    @Override
    public void deleteAll() {
        instances.clear();
    }

    @Override
    public void save(Instance instance) {
        instances.add(instance);
    }

    public void removeInstanceById(Long databaseId) {
        for (int i = 0; i < instances.size(); i++) {
            if (instances.get(i).getId().equals(databaseId)) {
                instances.remove(i);
                return;
            }
        }
    }
}
