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

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Parameter class providing access to a temporary directory created automatically on first access and deleted after the
 * test has been finished.
 */
public final class TempPath implements Supplier<Path> {

	private final Path path;

	TempPath(Path path) {
		this.path = path;
	}

	@Override
	public Path get() {
		return this.path;
	}

}
