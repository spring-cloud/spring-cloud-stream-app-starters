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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.app.test.mail.PoorMansMailServer;
import org.springframework.cloud.stream.app.test.mail.PoorMansMailServer.ImapServer;
import org.springframework.cloud.stream.app.test.mail.PoorMansMailServer.Pop3Server;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for Mail Source Configuration.
 *
 * @author Amol
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MailSourceConfigurationTests.MailSourceApplication.class)
@DirtiesContext
@WebIntegrationTest(randomPort = true)
public abstract class MailSourceConfigurationTests {

	@Autowired
	protected Source source;

	@Autowired
	protected MessageCollector messageCollector;

	@Autowired
	protected MailSourceProperties properties;

	
	@IntegrationTest({ "protocol=imap", "port=50000", "username=user", "password=pw",
			"host=localhost", "folder=INBOX", "mark-as-read=true", "delete=false",
			"java-mail-properties=mail.imap.socketFactory.fallback=true,mail.store.protocol=imap,mail.debug=true" })
	public static class ImapPassTests extends MailSourceConfigurationTests {
		private final static ImapServer imapServer = PoorMansMailServer.imap(50000);
		
		@BeforeClass
		public static void startImapServer() throws Throwable {
			int n = 0;
			while (n++ < 100 && (!imapServer.isListening())) {
				Thread.sleep(100);
			}
			assertTrue(n < 100);
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = messageCollector.forChannel(source.output()).poll(100,
					TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertEquals("foo\r\n", received.getPayload());
		}

		@AfterClass
		public static void tearDown() {
			imapServer.stop();
		}
	}

	@IntegrationTest({ "protocol=imap", "port=50000", "username=user", "password=pw",
			"host=localhost", "folder=INBOX", "mark-as-read=true", "delete=false",
			"java-mail-properties=mail.imap.socketFactory.fallback=true,mail.store.protocol=imap,mail.debug=true" })
	public static class ImapFailTests extends MailSourceConfigurationTests {
		private final static ImapServer imapServer = PoorMansMailServer.imap(50000);
		
		@BeforeClass
		public static void startImapServer() throws Throwable {
			int n = 0;
			while (n++ < 100 && (!imapServer.isListening())) {
				Thread.sleep(100);
			}
			assertTrue(n < 100);
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = messageCollector.forChannel(source.output()).poll(100,
					TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertTrue(!received.getPayload().equals("Test Mail"));
		}

		@AfterClass
		public static void tearDown() {
			imapServer.stop();
		}
	}

	@IntegrationTest({ "protocol=pop3", "port=50001", "username=user", "password=pw",
			"host=localhost", "folder=INBOX", "mark-as-read=true", "delete=false",
			"java-mail-properties=mail.imap.socketFactory.fallback=true,mail.store.protocol=imap,mail.debug=true" })
	public static class Pop3PassTests extends MailSourceConfigurationTests {
		private final static Pop3Server pop3Server = PoorMansMailServer.pop3(50001);

		@BeforeClass
		public static void startImapServer() throws Throwable {
			int n = 0;
			while (n++ < 100 && (!pop3Server.isListening())) {
				Thread.sleep(100);
			}
			assertTrue(n < 100);
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = messageCollector.forChannel(source.output()).poll(100,
					TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertEquals("foo\r\n", received.getPayload());
		}

		@AfterClass
		public static void tearDown() {
			pop3Server.stop();
		}
	}

	@IntegrationTest({ "protocol=pop3", "port=50001", "username=user", "password=pw",
			"host=localhost", "folder=INBOX", "mark-as-read=true", "delete=false",
			"java-mail-properties=mail.imap.socketFactory.fallback=true,mail.store.protocol=imap,mail.debug=true" })
	public static class Pop3FailTests extends MailSourceConfigurationTests {
		private final static Pop3Server pop3Server = PoorMansMailServer.pop3(50001);

		@BeforeClass
		public static void startImapServer() throws Throwable {
			int n = 0;
			while (n++ < 100 && (!pop3Server.isListening())) {
				Thread.sleep(100);
			}
			assertTrue(n < 100);
		}

		@Test
		public void testSimpleTest() throws Exception {

			Message<?> received = messageCollector.forChannel(source.output()).poll(100,
					TimeUnit.SECONDS);
			assertNotNull(received);
			assertThat(received.getPayload(), Matchers.instanceOf(String.class));
			assertTrue(!received.getPayload().equals("Test Mail"));
		}

		@AfterClass
		public static void tearDown() {
			pop3Server.stop();
		}
	}

	@SpringBootApplication
	public static class MailSourceApplication {

	}

}