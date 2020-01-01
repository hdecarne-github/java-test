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
package de.carne.test.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.boot.logging.Log;
import de.carne.io.IOUtil;
import de.carne.nio.file.attribute.FileAttributes;

/**
 * Test file downloaded via a URL.
 */
public final class RemoteTestFile extends TestFile {

	private static final Log LOG = new Log();

	private final String remoteUrl;
	private final @Nullable String localFileName;

	/**
	 * Construct's new {@linkplain RemoteTestFile}.
	 *
	 * @param dir the directory to download the file to.
	 * @param remoteUrl the {@linkplain URL} to download the file from.
	 */
	public RemoteTestFile(Path dir, String remoteUrl) {
		this(dir, remoteUrl, null);
	}

	/**
	 * Construct's new {@linkplain RemoteTestFile}.
	 *
	 * @param dir the directory to download the file to.
	 * @param remoteUrl the {@linkplain URL} to download the file from.
	 * @param localFileName the local file name to use.
	 */
	public RemoteTestFile(Path dir, String remoteUrl, @Nullable String localFileName) {
		super(dir);
		this.remoteUrl = remoteUrl;
		this.localFileName = localFileName;
	}

	@SuppressWarnings("squid:S3725")
	@Override
	public Path getFilePath(Path dir) throws IOException {
		URL remoteFile = new URL(this.remoteUrl);
		Path fileName = Paths.get(this.localFileName != null ? this.localFileName : remoteFile.getPath()).getFileName();
		Path localFile = dir.resolve(fileName).toAbsolutePath();

		if (!Files.exists(localFile)) {
			LOG.info("Downloading ''{0}'' to ''{1}''...", remoteFile, localFile);

			Files.createDirectories(localFile.getParent(), FileAttributes.userDirectoryDefault(localFile));
			try (InputStream remoteStream = remoteFile.openStream();
					OutputStream localStream = Files.newOutputStream(localFile, StandardOpenOption.CREATE_NEW)) {
				IOUtil.copyStream(localStream, remoteStream);
			}
		}
		return localFile;
	}

}
