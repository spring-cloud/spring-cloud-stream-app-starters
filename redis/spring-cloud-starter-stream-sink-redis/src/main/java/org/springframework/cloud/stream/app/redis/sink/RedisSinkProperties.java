/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.stream.app.redis.sink;

import java.util.Arrays;
import java.util.Collections;

import javax.validation.constraints.AssertTrue;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.expression.Expression;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

/**
 * Used to configure those Redis Sink module options that are not related to connecting to Redis.
 *
 * @author Eric Bottard
 * @author Mark Pollack
 */
@ConfigurationProperties("redis")
public class RedisSinkProperties {

	private final SpelExpressionParser parser = new SpelExpressionParser();

	/**
	 * A SpEL expression to use for topic.
	 */
	private Expression topicExpression;

	/**
	 * A SpEL expression to use for queue.
	 */
	private Expression queueExpression;

	/**
	 * A SpEL expression to use for storing to a key.
	 */
	private Expression keyExpression;

	/**
	 * A literal key name to use when storing to a key.
	 */
	private String key;

	/**
	 * A literal queue name to use when storing in a queue.
	 */
	private String queue;

	/**
	 * A literal topic name to use when publishing to a topic.
	 */
	private String topic;

	public void setKey(String key) {
		this.key = key;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Expression keyExpression() {
		return key != null ? new LiteralExpression(key) : keyExpression;
	}

	public void setKeyExpression(Expression keyExpression) {
		this.keyExpression = keyExpression;
	}

	public Expression queueExpression() {
		return queue != null ? new LiteralExpression(queue) : queueExpression;
	}

	public void setQueueExpression(Expression queueExpression) {
		this.queueExpression = queueExpression;
	}

	public Expression topicExpression() {
		return topic != null ? new LiteralExpression(topic) : topicExpression;
	}

	public void setTopicExpression(Expression topicExpression) {
		this.topicExpression = topicExpression;
	}

	/*default*/ boolean isKey() {
		return StringUtils.hasText(key) || keyExpression != null;
	}

	/*default*/ boolean isQueue() {
		return StringUtils.hasText(queue) || queueExpression != null;
	}

	/*default*/ public boolean isTopic() {
		return StringUtils.hasText(topic) || topicExpression != null;
	}

	public Expression getTopicExpression() {
		return topicExpression;
	}

	public Expression getQueueExpression() {
		return queueExpression;
	}

	public Expression getKeyExpression() {
		return keyExpression;
	}

	public String getKey() {
		return key;
	}

	public String getQueue() {
		return queue;
	}

	public String getTopic() {
		return topic;
	}


	// The javabean property name is what will be reported in case of violation. Make it meaningful
	@AssertTrue(message = "Exactly one of 'queue', 'queueExpression', 'key', 'keyExpression', "
			+ "'topic' and 'topicExpression' must be set")
	public boolean isMutuallyExclusive() {
		Object[] props = new Object[]{queue, queueExpression, key, keyExpression, topic, topicExpression};
		return (props.length - 1) == Collections.frequency(Arrays.asList(props), null);
	}

}
