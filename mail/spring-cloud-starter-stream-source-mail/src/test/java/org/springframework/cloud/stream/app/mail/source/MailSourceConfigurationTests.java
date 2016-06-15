/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.mail.source;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.stream.app.test.mail.PoorMansMailServer;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.mail.transformer.MailToStringTransformer;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for Mail Source Configuration.
 *
 * @author Amol
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MailSourceConfigurationTests.MailSourceApplication.class)
@DirtiesContext
@TestPropertySource(properties = { "mail.mark-as-read=true", "mail.delete=false",
		"mail.java-mail-properties=mail.imap.socketFactory.fallback=true\\n mail.store.protocol=imap\\n mail.debug=true" })
public abstract class MailSourceConfigurationTests {

	@Autowired
	protected Source source;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected MailSourceProperties properties;

	@Autowired
	protected BeanFactory beanFactory;

	private static PoorMansMailServer.MailServer MAIL_SERVER;

	protected static void startMailServer(PoorMansMailServer.MailServer mailServer)
			throws InterruptedException {
		MAIL_SERVER = mailServer;
		System.setProperty("test.mail.server.port", "" + MAIL_SERVER.getPort());
		int n = 0;
		while (n++ < 100 && (!MAIL_SERVER.isListening())) {
			Thread.sleep(10);
		}
		assertTrue(n < 100);
	}

	@AfterClass
	public static void cleanup() {
		System.clearProperty("test.mail.server.port");
		MAIL_SERVER.stop();
	}

	@IntegrationTest({ "mail.url=imap://user:pw@localhost:${test.mail.server.port}/INBOX" })
	public static class ImapPassTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.imap(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertEquals("foo\r\n", received.getPayload());
		}

	}

	@IntegrationTest({ "mail.url=imap://user:pw@localhost:${test.mail.server.port}/INBOX", "mail.charset=cp1251"})
	public static class ImapFailTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.imap(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertTrue(!received.getPayload().equals("Test Mail"));

			MailToStringTransformer mailToStringTransformer = this.beanFactory.getBean(MailToStringTransformer.class);
			assertEquals("cp1251", TestUtils.getPropertyValue(mailToStringTransformer, "charset"));
		}

	}

	@IntegrationTest({ "mail.url=pop3://user:pw@localhost:${test.mail.server.port}/INBOX" })
	public static class Pop3PassTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.pop3(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertEquals("foo\r\n", received.getPayload());
		}

	}

	@IntegrationTest({ "mail.url=pop3://user:pw@localhost:${test.mail.server.port}/INBOX" })
	public static class Pop3FailTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.pop3(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertTrue(!received.getPayload().equals("Test Mail"));
		}

	}

	@IntegrationTest({ "mail.idle-imap=true", "mail.url=imap://user:pw@localhost:${test.mail.server.port}/INBOX" })
	public static class ImapIdlePassTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.imap(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertEquals("foo\r\n", received.getPayload());
		}

	}

	@IntegrationTest({ "mail.idle-imap=true", "mail.url=imap://user:pw@localhost:${test.mail.server.port}/INBOX" })
	public static class ImapIdleFailTests extends MailSourceConfigurationTests {

		@BeforeClass
		public static void startImapServer() throws Throwable {
			startMailServer(PoorMansMailServer.imap(0));
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = this.messageCollector.forChannel(source.output()).poll(10, TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertTrue(!received.getPayload().equals("Test Mail"));
		}

	}

	@SpringBootApplication
	public static class MailSourceApplication {

	}

}
