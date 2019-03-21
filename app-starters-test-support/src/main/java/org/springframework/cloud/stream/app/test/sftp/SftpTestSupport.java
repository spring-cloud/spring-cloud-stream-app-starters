/*
 * Copyright 2015-2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.test.sftp;

import java.io.File;
import java.util.Arrays;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.file.FileSystemView;
import org.apache.sshd.common.file.nativefs.NativeFileSystemFactory;
import org.apache.sshd.common.file.nativefs.NativeFileSystemView;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.springframework.cloud.stream.app.test.file.remote.RemoteFileTestSupport;

/**
 * Provides an embedded SFTP Server for test cases.
 *
 * @author David Turanski
 * @author Gary Russell
 */
public class SftpTestSupport extends RemoteFileTestSupport {

	private static SshServer server;

	@Override
	public String prefix() {
		return "sftp";
	}

	@BeforeClass
	public static void createServer() throws Exception {
		server = SshServer.setUpDefaultServer();
		server.setPasswordAuthenticator(new PasswordAuthenticator() {


			@Override
			public boolean authenticate(String username, String password,
					org.apache.sshd.server.session.ServerSession session) {
				return true;
			}
		});
		server.setPort(port);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
		SftpSubsystem.Factory sftp = new SftpSubsystem.Factory();
		server.setSubsystemFactories(Arrays.<NamedFactory<Command>>asList(sftp));
		server.setFileSystemFactory(new NativeFileSystemFactory() {

			@Override
			public FileSystemView createFileSystemView(org.apache.sshd.common.Session session) {
				return new NativeFileSystemView(session.getUsername(), false) {
					@Override
					public String getVirtualUserDir() {
						return remoteTemporaryFolder.getRoot().getAbsolutePath();
					}
				};
			}

		});

		server.start();
	}

	@AfterClass
	public static void stopServer() throws Exception {
		server.stop();
		File hostkey = new File("hostkey.ser");
		if (hostkey.exists()) {
			hostkey.delete();
		}
	}

}
