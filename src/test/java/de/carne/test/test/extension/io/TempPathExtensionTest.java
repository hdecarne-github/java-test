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
package de.carne.test.test.extension.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import de.carne.nio.file.FileUtil;
import de.carne.test.annotation.io.TempDir;
import de.carne.test.annotation.io.TempFile;
import de.carne.test.extension.io.TempPathExtension;

/**
 * Test {@linkplain TempPathExtension} class.
 */
@ExtendWith(TempPathExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
class TempPathExtensionTest {

	private static final String TEST_FILE1 = "file1.tmp";

	private static final byte[] TEST_CONTENT = { (byte) 0x00, (byte) 0xff };

	@SuppressWarnings("null")
	@TempDir
	static Path sharedTempDir;

	@SuppressWarnings("null")
	@TempDir
	Path tempDirField1;
	@SuppressWarnings("null")
	@TempDir
	File tempDirField2;

	@SuppressWarnings("null")
	@TempFile(content = { (byte) 0x00, (byte) 0xff })
	Path tempFileField1;
	@SuppressWarnings("null")
	@TempFile
	File tempFileField2;

	@Test
	void testSharedTempDir1stAccess() throws IOException {
		Path testFile1 = TempPathExtensionTest.sharedTempDir.resolve(TEST_FILE1);

		Assertions.assertFalse(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));

		FileUtil.touch(testFile1);

		Assertions.assertTrue(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));
	}

	@Test
	void testSharedTempDir2ndtAccess() {
		Path testFile1 = TempPathExtensionTest.sharedTempDir.resolve(TEST_FILE1);

		Assertions.assertTrue(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));
	}

	@Test
	void testTempDir(@TempDir Path tempDir) {
		Assertions.assertTrue(Files.isDirectory(tempDir, LinkOption.NOFOLLOW_LINKS));
		Assertions.assertTrue(Files.isDirectory(this.tempDirField1, LinkOption.NOFOLLOW_LINKS));
	}

	@Test
	void testTempDir(@TempDir File tempDir) {
		Assertions.assertTrue(tempDir.isDirectory());
		Assertions.assertTrue(this.tempDirField2.isDirectory());
	}

	@Test
	void testTempFile(@TempFile(content = { (byte) 0x00, (byte) 0xff }) Path tempFile) throws IOException {
		Assertions.assertTrue(Files.isRegularFile(tempFile, LinkOption.NOFOLLOW_LINKS));
		Assertions.assertArrayEquals(TEST_CONTENT, Files.readAllBytes(tempFile));
		Assertions.assertTrue(Files.isRegularFile(this.tempFileField1, LinkOption.NOFOLLOW_LINKS));
		Assertions.assertArrayEquals(TEST_CONTENT, Files.readAllBytes(this.tempFileField1));
	}

	@Test
	void testTempFile(@TempFile File tempFile) {
		Assertions.assertTrue(tempFile.isFile());
		Assertions.assertTrue(this.tempFileField2.isFile());
	}

}
