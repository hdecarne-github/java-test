/*
 * Copyright (c) 2018 Holger de Carne and contributors, All Rights Reserved.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import de.carne.boot.check.Check;
import de.carne.boot.logging.Log;
import de.carne.nio.file.FileUtil;

/**
 * {@linkplain ParameterResolver} that provides access to a single temporary directory available during test execution.
 */
public class TempPathParameterResolver implements ParameterResolver, AfterAllCallback {

	private static final Log LOG = new Log();

	private static final Namespace EXTENSION_NAMESPACE = Namespace.create(TempPathParameterResolver.class);
	private static final String TEMP_PATH_KEY = "TempPath";

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.getParameter().getType().equals(TempPath.class);
	}

	@Override
	public @Nullable Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Optional<@Nullable ExtensionContext> optionalParentExtensionContext = extensionContext.getParent();

		if (!optionalParentExtensionContext.isPresent()) {
			throw new ParameterResolutionException("Parent extension context missing");
		}

		Store store = optionalParentExtensionContext.get().getStore(EXTENSION_NAMESPACE);
		Object tempPathObject = store.get(TEMP_PATH_KEY);

		if (tempPathObject == null) {
			try {
				Path tempPath = Files.createTempDirectory(extensionContext.getTestClass().get().getSimpleName());

				LOG.info("Created temporary path ''{0}'' for test execution", tempPath);

				tempPathObject = new TempPath(tempPath);
			} catch (IOException e) {
				throw new ParameterResolutionException("", e);
			}
			store.put(TEMP_PATH_KEY, tempPathObject);
		}
		return Check.isInstanceOf(tempPathObject, TempPath.class);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		Store store = context.getStore(EXTENSION_NAMESPACE);
		Object tempPathObject = store.get(TEMP_PATH_KEY);

		if (tempPathObject != null) {
			Path tempPath = Check.isInstanceOf(tempPathObject, TempPath.class).get();

			LOG.info("Deleting temporary path ''{0}''...", tempPath);

			FileUtil.delete(tempPath);
		}
	}

}
