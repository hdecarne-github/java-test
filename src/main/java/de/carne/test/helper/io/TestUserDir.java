/*
 * Copyright (c) 2018-2020 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.test.helper.io;

import java.util.Objects;

/**
 * Test utility class providing {@code user.dir} related functions.
 */
public final class TestUserDir {

	private static final String PROPERTY_FILE_SEPARATOR = "file.separator";
	private static final String PROPERTY_USER_DIR = "user.dir";

	private TestUserDir() {
		// prevent instantiation
	}

	/**
	 * Gets the current {@code user.dir} system property value.
	 * 
	 * @return the current {@code user.dir} system property value.
	 */
	public static String getUserDir() {
		return safeGetProperty(PROPERTY_USER_DIR);
	}

	/**
	 * Changes the {@code user.dir} system property to the given value.
	 *
	 * @param userDir the absolute or relative {@code user.dir} system property to set.
	 * @return the newly set {@code user.dir} system property.
	 */
	public static String changeUserDir(String userDir) {
		String newUserDir;

		if (userDir.length() > 0 && "/\\".indexOf(userDir.charAt(0)) < 0) {
			String oldUserDir = safeGetProperty(PROPERTY_USER_DIR);
			int oldUserDirLength = oldUserDir.length();

			if (oldUserDirLength > 0 && "/\\".indexOf(oldUserDir.charAt(oldUserDirLength - 1)) < 0) {
				newUserDir = oldUserDir + safeGetProperty(PROPERTY_FILE_SEPARATOR) + userDir;
			} else {
				newUserDir = oldUserDir + userDir;
			}
		} else {
			newUserDir = userDir;
		}
		System.setProperty(PROPERTY_USER_DIR, newUserDir);
		return newUserDir;
	}

	private static String safeGetProperty(String property) {
		return Objects.requireNonNull(System.getProperty(property));
	}

}
