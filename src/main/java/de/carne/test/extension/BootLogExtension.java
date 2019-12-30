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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extension that ensures a minimal logging during test execution.
 */
public class BootLogExtension implements BeforeAllCallback {

	private static final String PROPERTY_LOGGING_CONFIG = "java.util.logging.config.file";
	private static final String CONFIG_BOOT = "logging-boot.properties";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		// Make sure a minimal logging configuration is set (may be overridden later)
		if (System.getProperty(PROPERTY_LOGGING_CONFIG) == null) {
			System.setProperty(PROPERTY_LOGGING_CONFIG, CONFIG_BOOT);
		}
	}

}
