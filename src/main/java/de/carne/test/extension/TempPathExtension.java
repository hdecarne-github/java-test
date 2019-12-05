/*
 * Copyright (c) 2018-2019 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.test.extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import de.carne.boot.logging.Log;
import de.carne.nio.file.FileUtil;
import de.carne.test.api.io.TempDir;
import de.carne.test.api.io.TempFile;

/**
 * Extension that provides access to temporary directory and files during test execution.
 */
public class TempPathExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver {

	private static final Log LOG = new Log();

	private static final Namespace EXTENSION_NAMESPACE = Namespace.create(TempPathExtension.class);

	private static final Set<Class<?>> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(Path.class, File.class));

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		injectFields(context, null, ReflectionUtils::isStatic);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		injectFields(context, context.getRequiredTestInstance(), ReflectionUtils::isNotStatic);
	}

	private void injectFields(ExtensionContext context, @Nullable Object testInstance, Predicate<Field> predicate) {
		AnnotationUtils.findAnnotatedFields(context.getRequiredTestClass(), TempDir.class, predicate)
				.forEach(field -> injectField(context, testInstance, field));
		AnnotationUtils.findAnnotatedFields(context.getRequiredTestClass(), TempFile.class, predicate)
				.forEach(field -> injectField(context, testInstance, field));
	}

	@SuppressWarnings("squid:S3011")
	private void injectField(ExtensionContext context, @Nullable Object testInstance, @Nullable Field field) {
		LOG.debug("Injecting field: {0}", field);

		Field checkedField = checkField(field);

		try {
			if (checkedField.getAnnotation(TempDir.class) != null) {
				checkedField.set(testInstance, getTempDirField(context, checkedField));
			} else {
				checkedField.set(testInstance, getTempFileField(context, checkedField));
			}
		} catch (IllegalAccessException e) {
			ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean supported = parameterContext.isAnnotated(TempDir.class) || parameterContext.isAnnotated(TempFile.class);

		if (supported && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException("Cannot inject Constructor parameters");
		}
		return supported;
	}

	@Override
	public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Parameter parameter = parameterContext.getParameter();

		LOG.debug("Resolving parameter: {0}", parameter);

		Parameter checkedParameter = checkParameter(parameterContext.getParameter());

		return (checkedParameter.getAnnotation(TempDir.class) != null
				? getTempDirParameter(extensionContext, checkedParameter)
				: getTempFileParameter(extensionContext, checkedParameter));
	}

	private Field checkField(@Nullable Field field) {
		Objects.requireNonNull(field);

		Class<?> fieldType = field.getType();

		if (!SUPPORTED_TYPES.contains(fieldType)) {
			throw new ExtensionConfigurationException("Unsupported field type: " + fieldType);
		}
		if (ReflectionUtils.isPrivate(field)) {
			throw new ExtensionConfigurationException("Cannot inject private field: " + field);
		}
		return ReflectionUtils.makeAccessible(field);
	}

	private Parameter checkParameter(Parameter parameter) {
		Class<?> parameterType = parameter.getType();

		if (!SUPPORTED_TYPES.contains(parameterType)) {
			throw new ExtensionConfigurationException("Unsupported parameter type: " + parameterType);
		}
		return parameter;
	}

	private Object getTempDirField(ExtensionContext context, Field field) {
		Path tempDir = context.getStore(EXTENSION_NAMESPACE).getOrComputeIfAbsent(field,
				key -> createTempDirResource(context.getTestClass().get().getSimpleName()), TempDirResource.class)
				.getPath();

		LOG.debug("Set temporary directory: {0} = {1}", field, tempDir);

		return (field.getType().equals(Path.class) ? tempDir : tempDir.toFile());
	}

	private Object getTempDirParameter(ExtensionContext context, Parameter parameter) {
		Path tempDir = context.getStore(EXTENSION_NAMESPACE).getOrComputeIfAbsent(parameter,
				key -> createTempDirResource(context.getTestClass().get().getSimpleName()), TempDirResource.class)
				.getPath();

		LOG.debug("Resolved temporary directory: {0} = {1}", parameter, tempDir);

		return (parameter.getType().equals(Path.class) ? tempDir : tempDir.toFile());
	}

	private static TempDirResource createTempDirResource(String prefix) {
		Path tempDir;

		try {
			tempDir = Files.createTempDirectory(prefix);
		} catch (IOException e) {
			throw new ExtensionConfigurationException("Failed to create temporary directory", e);
		}
		return new TempDirResource(LOG, tempDir);
	}

	private static class TempDirResource implements CloseableResource {

		private final Log log;
		private final Path tempDir;

		public TempDirResource(Log log, Path tempDir) {
			this.log = log;
			this.tempDir = tempDir;
		}

		@Override
		public void close() throws IOException {
			this.log.debug("Deleting temporary directory: ''{0}''...", this.tempDir);

			FileUtil.delete(this.tempDir);
		}

		public Path getPath() {
			return this.tempDir;
		}

	}

	private Object getTempFileField(ExtensionContext context, Field field) {
		Path tempDir = context.getStore(EXTENSION_NAMESPACE)
				.getOrComputeIfAbsent(context.getRequiredTestClass().getSimpleName(),
						TempPathExtension::createTempDirResource, TempDirResource.class)
				.getPath();
		Path tempFile = context.getStore(EXTENSION_NAMESPACE).getOrComputeIfAbsent(field,
				key -> createTempFileResource(tempDir, context.getTestClass().get().getSimpleName(),
						Objects.requireNonNull(field.getAnnotation(TempFile.class)).content()),
				TempFileResource.class).getPath();

		LOG.debug("Set temporary file: {0} = {1}", field, tempDir);

		return (field.getType().equals(Path.class) ? tempFile : tempFile.toFile());
	}

	private Object getTempFileParameter(ExtensionContext context, Parameter parameter) {
		Path tempDir = context.getStore(EXTENSION_NAMESPACE)
				.getOrComputeIfAbsent(context.getRequiredTestClass().getSimpleName(),
						TempPathExtension::createTempDirResource, TempDirResource.class)
				.getPath();
		Path tempFile = context.getStore(EXTENSION_NAMESPACE)
				.getOrComputeIfAbsent(parameter,
						key -> createTempFileResource(tempDir, context.getTestClass().get().getSimpleName(),
								Objects.requireNonNull(parameter.getAnnotation(TempFile.class)).content()),
						TempFileResource.class)
				.getPath();

		LOG.debug("Resolved temporary: file {0} = {1}", parameter, tempDir);

		return (parameter.getType().equals(Path.class) ? tempFile : tempFile.toFile());
	}

	private static TempFileResource createTempFileResource(Path tempDir, String prefix, byte[] content) {
		Path tempFile;

		try {
			tempFile = Files.createTempFile(tempDir, prefix, "");
			Files.write(tempFile, content, StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new ExtensionConfigurationException("Failed to create temporary file", e);
		}
		return new TempFileResource(LOG, tempFile);
	}

	private static class TempFileResource implements CloseableResource {

		private final Log log;
		private final Path tempFile;

		public TempFileResource(Log log, Path tempFile) {
			this.log = log;
			this.tempFile = tempFile;
		}

		@Override
		public void close() throws IOException {
			this.log.debug("Deleting temporary file ''{0}''...", this.tempFile);

			FileUtil.delete(this.tempFile);
		}

		public Path getPath() {
			return this.tempFile;
		}

	}

}
