package io.katharsis.resource.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.katharsis.core.internal.registry.ResourceRegistryImpl;
import io.katharsis.core.internal.repository.information.ResourceRepositoryInformationImpl;
import io.katharsis.errorhandling.exception.ResourceNotFoundInitializationException;
import io.katharsis.module.ModuleRegistry;
import io.katharsis.resource.annotations.JsonApiResource;
import io.katharsis.resource.information.ResourceInformation;
import io.katharsis.resource.mock.models.Task;

public class ResourceRegistryTest {

	public static final String TEST_MODELS_URL = "https://service.local";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ResourceRegistry resourceRegistry;

	private ModuleRegistry moduleRegistry;

	@Before
	public void resetResourceRegistry() {
		moduleRegistry = new ModuleRegistry();
		resourceRegistry = new ResourceRegistryImpl(moduleRegistry, new ConstantServiceUrlProvider(TEST_MODELS_URL));
	}

	@Test
	public void onExistingTypeShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry tasksEntry = resourceRegistry.getEntry("tasks");
		assertThat(tasksEntry).isNotNull();
	}

	private <T> RegistryEntry newRegistryEntry(Class<T> repositoryClass, String path) {
		return new RegistryEntry(new ResourceRepositoryInformationImpl(null, path, new ResourceInformation(moduleRegistry.getTypeParser(), Task.class, path, null, null)), null, null);
	}

	@Test
	public void testGetSeriveUrlProvider() {
		assertThat(resourceRegistry.getServiceUrlProvider().getUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void testGetServiceUrl() {
		assertThat(resourceRegistry.getServiceUrlProvider().getUrl()).isEqualTo(TEST_MODELS_URL);
	}

	@Test
	public void onExistingClassShouldReturnEntry() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		RegistryEntry tasksEntry = resourceRegistry.findEntry(Task.class);
		assertThat(tasksEntry).isNotNull();
	}

	@Test
	public void onExistingTypeShouldReturnUrl() {
		RegistryEntry entry = resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		String resourceUrl = resourceRegistry.getResourceUrl(entry.getResourceInformation());
		assertThat(resourceUrl).isEqualTo(TEST_MODELS_URL + "/tasks");
	}

	@Test
	public void onNonExistingTypeShouldReturnNull() {
		RegistryEntry entry = resourceRegistry.getEntry("nonExistingType");
		assertThat(entry).isNull();
	}

	@Test
	public void onNonExistingClassShouldThrowException() {
		expectedException.expect(ResourceNotFoundInitializationException.class);
		resourceRegistry.findEntry(Long.class);
	}

	@Test(expected = ResourceNotFoundInitializationException.class)
	public void onNonExistingClassShouldReturnNull() {
		resourceRegistry.findEntry(Long.class);
	}

	@Test
	public void onResourceClassReturnCorrectClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(Task$Proxy.class).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isNotNull();
		assertThat(clazz).hasAnnotation(JsonApiResource.class);
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectParentInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task$Proxy resource = new Task$Proxy();

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(resource.getClass()).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test
	public void onResourceClassReturnCorrectInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));
		Task resource = new Task();

		// WHEN
		Class<?> clazz = resourceRegistry.findEntry(resource.getClass()).getResourceInformation().getResourceClass();

		// THEN
		assertThat(clazz).isEqualTo(Task.class);
	}

	@Test(expected = ResourceNotFoundInitializationException.class)
	public void onResourceClassReturnNoInstanceClass() {
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, "tasks"));

		// WHEN
		resourceRegistry.findEntry(Object.class);
	}

	@Test
	public void onResourceGetEntryWithBackUp() {
		String taskType = Task.class.getAnnotation(JsonApiResource.class).type();
		resourceRegistry.addEntry(Task.class, newRegistryEntry(Task.class, taskType));

		// WHEN
		RegistryEntry registryEntry = resourceRegistry.findEntry(Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);

		// WHEN
		registryEntry = resourceRegistry.findEntry(Task.class);

		// THEN
		assertNotNull(registryEntry);
		assertNotNull(registryEntry.getResourceInformation().getResourceType(), taskType);
	}

	public static class Task$Proxy extends Task {
	}

}
