/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.gpfdist.sink;

import com.codahale.metrics.Meter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Processor;
import org.springframework.cloud.stream.app.gpfdist.sink.support.GreenplumLoad;
import org.springframework.cloud.stream.app.gpfdist.sink.support.NetworkUtils;
import org.springframework.cloud.stream.app.gpfdist.sink.support.RuntimeContext;
import org.springframework.data.hadoop.util.net.HostInfoDiscovery;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.StringUtils;
import org.springframework.util.concurrent.SettableListenableFuture;
import reactor.Environment;
import reactor.core.processor.RingBufferProcessor;
import reactor.io.buffer.Buffer;

import java.util.Date;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Gpfdist related {@code MessageHandler}.
 *
 * @author Janne Valkealahti
 */
public class GpfdistMessageHandler extends AbstractGpfdistMessageHandler {

	private final Log log = LogFactory.getLog(GpfdistMessageHandler.class);

	private final int port;
	private final int flushCount;
	private final int flushTime;
	private final int batchTimeout;
	private final int batchCount;
	private final int batchPeriod;
	private final String delimiter;
	private GreenplumLoad greenplumLoad;
	private Processor<Buffer, Buffer> processor;
	private GpfdistServer gpfdistServer;
	private TaskScheduler sqlTaskScheduler;
	private final TaskFuture taskFuture = new TaskFuture();
	private int rateInterval = 0;
	private Meter meter =  null;
	private int meterCount = 0;
	private final HostInfoDiscovery hostInfoDiscovery;

	/**
	 * Instantiates a new gpfdist message handler.
	 *
	 * @param port the port
	 * @param flushCount the flush count
	 * @param flushTime the flush time
	 * @param batchTimeout the batch timeout
	 * @param batchCount the batch count
	 * @param batchPeriod the batch period
	 * @param delimiter the delimiter
	 * @param hostInfoDiscovery the host info discovery
	 */
	public GpfdistMessageHandler(int port, int flushCount, int flushTime, int batchTimeout, int batchCount,
			int batchPeriod, String delimiter, HostInfoDiscovery hostInfoDiscovery) {
		super();
		this.port = port;
		this.flushCount = flushCount;
		this.flushTime = flushTime;
		this.batchTimeout = batchTimeout;
		this.batchCount = batchCount;
		this.batchPeriod = batchPeriod;
		this.delimiter = StringUtils.hasLength(delimiter) ? delimiter : null;
		this.hostInfoDiscovery = hostInfoDiscovery;
	}

	@Override
	protected void doWrite(Message<?> message) throws Exception {
		Object payload = message.getPayload();
		if (payload instanceof String) {
			String data = (String)payload;
			if (delimiter != null) {
				processor.onNext(Buffer.wrap(data+delimiter));
			} else {
				processor.onNext(Buffer.wrap(data));
			}
			if (meter != null) {
				if ((meterCount++ % rateInterval) == 0) {
					meter.mark(rateInterval);
					log.info("METER: 1 minute rate = " + meter.getOneMinuteRate() + " mean rate = " + meter.getMeanRate());
				}
			}
		} else {
			throw new MessageHandlingException(message, "message not a String");
		}
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Environment.initializeIfEmpty().assignErrorJournal();
		processor = RingBufferProcessor.create(false);
	}

	@Override
	protected void doStart() {
		try {
			log.info("Creating gpfdist protocol listener on port=" + port);
			gpfdistServer = new GpfdistServer(processor, port, flushCount, flushTime, batchTimeout, batchCount);
			gpfdistServer.start();
			log.info("gpfdist protocol listener running on port=" + gpfdistServer.getLocalPort());
		} catch (Exception e) {
			throw new RuntimeException("Error starting protocol listener", e);
		}

		if (greenplumLoad != null) {
			log.info("Scheduling gpload task with batchPeriod=" + batchPeriod);

			final RuntimeContext context = new RuntimeContext(
					NetworkUtils.getGPFDistUri(hostInfoDiscovery.getHostInfo().getAddress(), gpfdistServer.getLocalPort()));

			sqlTaskScheduler.schedule((new FutureTask<Void>(new Runnable() {
				@Override
				public void run() {
					boolean taskValue = true;
					try {
						while(!taskFuture.interrupted) {
							try {
								greenplumLoad.load(context);
							} catch (Exception e) {
								log.error("Error in load", e);
							}
							Thread.sleep(batchPeriod*1000);
						}
					} catch (Exception e) {
						taskValue = false;
					}
					taskFuture.set(taskValue);
				}
			}, null)), new Date());

		} else {
			log.info("Skipping gpload tasks because greenplumLoad is not set");
		}
	}

	@Override
	protected void doStop() {
		if (greenplumLoad != null) {
			taskFuture.interruptTask();
			try {
				long now = System.currentTimeMillis();
				// wait a bit more than batch period
				Boolean value = taskFuture.get(batchTimeout + batchPeriod + 2, TimeUnit.SECONDS);
				log.info("Stopping, got future value " + value + " from task which took "
						+ (System.currentTimeMillis() - now) + "ms");
			} catch (Exception e) {
				log.warn("Got error from task wait value which may indicate trouble", e);
			}
		}

		try {
			processor.onComplete();
			gpfdistServer.stop();
		} catch (Exception e) {
			log.warn("Error shutting down protocol listener", e);
		}
	}

	/**
	 * Sets the sql task scheduler.
	 *
	 * @param sqlTaskScheduler the new sql task scheduler
	 */
	public void setSqlTaskScheduler(TaskScheduler sqlTaskScheduler) {
		this.sqlTaskScheduler = sqlTaskScheduler;
	}

	/**
	 * Sets the greenplum load.
	 *
	 * @param greenplumLoad the new greenplum load
	 */
	public void setGreenplumLoad(GreenplumLoad greenplumLoad) {
		this.greenplumLoad = greenplumLoad;
	}

	/**
	 * Sets the rate interval.
	 *
	 * @param rateInterval the new rate interval
	 */
	public void setRateInterval(int rateInterval) {
		this.rateInterval = rateInterval;
		if (rateInterval > 0) {
			meter = new Meter();
		}
	}

	private static class TaskFuture extends SettableListenableFuture<Boolean> {

		boolean interrupted = false;

		@Override
		protected void interruptTask() {
			interrupted = true;
		}
	}
}
