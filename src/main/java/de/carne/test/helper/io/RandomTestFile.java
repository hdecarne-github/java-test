/*
 * Copyright (c) 2018-2021 Holger de Carne and contributors, All Rights Reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

import de.carne.nio.file.attribute.FileAttributes;
import de.carne.util.logging.Log;

/**
 * Test file of random data and optionally random size.
 */
public class RandomTestFile extends TestFile {

	private static final Log LOG = new Log();

	private static final int MIN_RANDOM_FILE_SIZE = 1;
	private static final int MAX_RANDOM_FILE_SIZE = 8192;

	@SuppressWarnings("squid:S2245")
	private final Random random = new Random();
	private final String fileName;
	private final int fileSize;

	/**
	 * Constructs a new {@linkplain RandomTestFile} instance.
	 * <p>
	 * The generated file has a random size and is not empty.
	 * </p>
	 *
	 * @param dir the directory to generate the file in.
	 * @param fileName the file name to use.
	 */
	public RandomTestFile(Path dir, String fileName) {
		this(dir, fileName, -1);
	}

	/**
	 * Constructs a new {@linkplain RandomTestFile} instance.
	 *
	 * @param dir the directory to generate the file in.
	 * @param fileName the file name to use.
	 * @param fileSize the file size to generate.
	 */
	public RandomTestFile(Path dir, String fileName, int fileSize) {
		super(dir);
		this.fileName = fileName;
		this.fileSize = (fileSize >= 0 ? fileSize
				: MIN_RANDOM_FILE_SIZE + this.random.nextInt(MAX_RANDOM_FILE_SIZE - MIN_RANDOM_FILE_SIZE + 1));
	}

	@SuppressWarnings("squid:S3725")
	@Override
	protected Path getFilePath(Path fileDir) throws IOException {
		Path file = fileDir.resolve(this.fileName);

		if (!Files.exists(file)) {
			LOG.info("Generatoring random file ''{0}'' of size ''{1}''...", file, this.fileSize);

			Files.createDirectories(file.getParent(), FileAttributes.userDirectoryDefault(file));
			try (OutputStream fileStream = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
				byte[] randomBytes = new byte[this.fileSize];

				this.random.nextBytes(randomBytes);
				fileStream.write(randomBytes);
			}
		}
		return file;
	}

}
