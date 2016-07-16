package org.springframework.cloud.stream.app.yahooquotes.source;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.stream.annotation.Bindings;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vinicius Carvalho
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(YahooQuotesSourceIntegrationTests.YahooQuotesSourceApplicationTests.class)
@WebIntegrationTest(randomPort = true)
@DirtiesContext
public class YahooQuotesSourceIntegrationTests {


	@Autowired
	@Bindings(YahooQuotesSourceConfiguration.class)
	protected Source yahooSource;

	@Autowired
	protected MessageCollector messageCollector;

	@IntegrationTest({"yahoo.quotes.interval=1000","yahoo.quotes.symbols=MSFT,GOOGL,AAPL"})
	public static class IntegrationTests extends YahooQuotesSourceIntegrationTests {

		@Test
		public void receiveOne() throws Exception{
			Message<?> received = messageCollector.forChannel(yahooSource.output()).poll(5, TimeUnit.SECONDS);

			System.out.println("############" + received.getPayload());
		}

	}

	@SpringBootApplication
	public static class YahooQuotesSourceApplicationTests{

	}
}
