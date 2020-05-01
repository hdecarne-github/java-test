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
package de.carne.test.test.helper.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.carne.test.api.io.TempDir;
import de.carne.test.extension.io.TempPathExtension;
import de.carne.test.helper.io.RandomTestFile;
import de.carne.test.helper.io.RemoteTestFile;
import de.carne.test.helper.io.TestFile;

/**
 * Test {@linkplain RemoteTestFile} class.
 */
@ExtendWith(TempPathExtension.class)
class TestFileTest {

	private static final String README_MD_URL = "https://raw.githubusercontent.com/hdecarne/java-test/master/README.md";
	private static final String README_MD_CHECKSUM = "a9aa388ce43ac566a843efe4e0429df95ba85db60e0086aa19a123f1e8e5cc3a";
	private static final String README_MD_FILE_NAME = "README.md";
	private static final int README_MD_SIZE = 641;
	private static final String README_TXT_FILE_NAME = "README.txt";

	@Test
	void testRemoteTestFile1(@TempDir Path dir) throws IOException {
		TestFile testFile1 = new RemoteTestFile(dir, README_MD_URL);

		Assertions.assertEquals(README_MD_SIZE, Files.size(testFile1.getPath()));
		Assertions.assertEquals(README_MD_SIZE, testFile1.getFile().length());
		Assertions.assertEquals(README_MD_FILE_NAME, testFile1.getPath().getFileName().toString());
	}

	@Test
	void testRemoteTestFile2(@TempDir Path dir) throws IOException {
		TestFile testFile2 = new RemoteTestFile(dir, README_MD_URL, README_TXT_FILE_NAME, README_MD_CHECKSUM);

		Assertions.assertEquals(README_MD_SIZE, Files.size(testFile2.getPath()));
		Assertions.assertEquals(README_MD_SIZE, testFile2.getFile().length());
		Assertions.assertEquals(README_TXT_FILE_NAME, testFile2.getPath().getFileName().toString());
	}

	private static final String RANDOM_FILE_NAME = "random.dat";
	private static final int RANDOM_FILE_SIZE = 1234;

	@Test
	void testRandomTestFile1(@TempDir Path dir) throws IOException {
		TestFile testFile1 = new RandomTestFile(dir, RANDOM_FILE_NAME, RANDOM_FILE_SIZE);

		Assertions.assertEquals(RANDOM_FILE_NAME, testFile1.getPath().getFileName().toString());
		Assertions.assertEquals(RANDOM_FILE_SIZE, Files.size(testFile1.getPath()));
	}

	@Test
	void testRandomTestFile2(@TempDir Path dir) throws IOException {
		TestFile testFile2 = new RandomTestFile(dir, RANDOM_FILE_NAME);

		Assertions.assertEquals(RANDOM_FILE_NAME, testFile2.getPath().getFileName().toString());
		Assertions.assertTrue(Files.size(testFile2.getPath()) > 0);
	}

}
