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
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.io.Checksum;
import de.carne.io.ChecksumInputStream;
import de.carne.io.IOUtil;
import de.carne.io.NullOutputStream;
import de.carne.io.SHA256Checksum;
import de.carne.nio.file.attribute.FileAttributes;
import de.carne.text.HexBytes;
import de.carne.util.logging.Log;

/**
 * Test file downloaded via a URL.
 */
public final class RemoteTestFile extends TestFile {

	private static final Log LOG = new Log();

	private final String remoteUrl;
	private final @Nullable String localFileName;
	private final @Nullable String checksumValue;

	/**
	 * Construct's new {@linkplain RemoteTestFile}.
	 *
	 * @param dir the directory to download the file to.
	 * @param remoteUrl the {@linkplain URL} to download the file from.
	 */
	public RemoteTestFile(Path dir, String remoteUrl) {
		this(dir, remoteUrl, null, null);
	}

	/**
	 * Construct's new {@linkplain RemoteTestFile}.
	 *
	 * @param dir the directory to download the file to.
	 * @param remoteUrl the {@linkplain URL} to download the file from.
	 * @param localFileName the local file name to use.
	 */
	public RemoteTestFile(Path dir, String remoteUrl, @Nullable String localFileName) {
		this(dir, remoteUrl, localFileName, null);
	}

	/**
	 * Construct's new {@linkplain RemoteTestFile}.
	 *
	 * @param dir the directory to download the file to.
	 * @param remoteUrl the {@linkplain URL} to download the file from.
	 * @param localFileName the local file name to use (may be {@code null}).
	 * @param checksumValue the expected checksum of the downloaded file (may be {@code null}).
	 */
	public RemoteTestFile(Path dir, String remoteUrl, @Nullable String localFileName, @Nullable String checksumValue) {
		super(dir);
		this.remoteUrl = remoteUrl;
		this.localFileName = localFileName;
		this.checksumValue = checksumValue;
	}

	@Override
	public Path getFilePath(Path dir) throws IOException {
		URL remoteFile = new URL(this.remoteUrl);
		Path fileName = Paths.get(this.localFileName != null ? this.localFileName : remoteFile.getPath()).getFileName();
		Path localFile = dir.resolve(fileName).toAbsolutePath();

		downloadAndVerifyFile(localFile, remoteFile, true);
		return localFile;
	}

	@SuppressWarnings("squid:S3725")
	private void downloadAndVerifyFile(Path localFile, URL remoteFile, boolean retry) throws IOException {
		String localFileChecksumValue;

		if (!Files.exists(localFile)) {
			localFileChecksumValue = downloadFile(localFile, remoteFile);
		} else {
			localFileChecksumValue = checksumFile(localFile);
		}
		if (this.checksumValue == null) {
			LOG.notice("File verification skipped for ''{0}'' (checksum:{1})", localFile, localFileChecksumValue);
		} else if (localFileChecksumValue.equals(this.checksumValue)) {
			LOG.notice("File verification passed for ''{0}'' (checksum:{1})", localFile, localFileChecksumValue);
		} else if (retry) {
			LOG.warning("File verification failed for ''{0}'' (checksum:{1})", localFile, localFileChecksumValue);
			LOG.warning("Restarting download...");

			Files.delete(localFile);
			downloadAndVerifyFile(localFile, remoteFile, false);
		} else {
			String message = MessageFormat.format("Checksum mismatch for file ''{0}'' (excepted: {1}; actual: {2})",
					localFile, this.checksumValue, localFileChecksumValue);

			LOG.error(message);

			throw new IOException(message);
		}
	}

	private String downloadFile(Path localFile, URL remoteFile) throws IOException {
		LOG.notice("Downloading ''{0}'' to ''{1}''...", remoteFile, localFile);

		Checksum checksum = getChecksumInstance();

		Files.createDirectories(localFile.getParent(), FileAttributes.userDirectoryDefault(localFile));
		try (InputStream remoteStream = new ChecksumInputStream(remoteFile.openStream(), checksum);
				OutputStream localStream = Files.newOutputStream(localFile, StandardOpenOption.CREATE_NEW)) {
			IOUtil.copyStream(localStream, remoteStream);
		}
		return HexBytes.toStringL(checksum.getValue());
	}

	private String checksumFile(Path localFile) throws IOException {
		LOG.notice("Verifying already downloaded file ''{0}''...", localFile);

		Checksum checksum = getChecksumInstance();

		try (InputStream localFileStream = new ChecksumInputStream(
				Files.newInputStream(localFile, StandardOpenOption.READ), checksum);
				OutputStream nullStream = new NullOutputStream()) {
			IOUtil.copyStream(nullStream, localFileStream);
		}
		return HexBytes.toStringL(checksum.getValue());
	}

	private Checksum getChecksumInstance() throws IOException {
		Checksum checksum;

		try {
			checksum = SHA256Checksum.getInstance();
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Checksum algorithm not available", e);
		}
		return checksum;
	}

}
