/*
 * Copyright (c) 2018-2022 Holger de Carne and contributors, All Rights Reserved.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Base class for all kinds of test files.
 */
public abstract class TestFile {

	private final Path dir;

	protected TestFile(Path dir) {
		this.dir = dir;
	}

	protected abstract Path getFilePath(Path fileDir) throws IOException;

	/**
	 * Gets the test file's path.
	 *
	 * @return the test file's path.
	 * @throws IOException if an I/O error occurs.
	 */
	public Path getPath() throws IOException {
		return getFilePath(this.dir);
	}

	/**
	 * Gets the test file.
	 *
	 * @return the test file.
	 * @throws IOException if an I/O error occurs.
	 */
	public File getFile() throws IOException {
		return getPath().toFile();
	}

}
