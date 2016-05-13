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

import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Mail Source module.
 *
 * @author Amol
 */
@ConfigurationProperties
public class MailSourceProperties {

	/**
	 * Mail connection URL for connection to Mail server e.g.
	 * 'imaps://username:password@imap.server.com:993/Inbox'
	 */
	private String mailUrl;
	
	/**
	 * Protocol to be used for receiving mail e.g. 'imaps' or 'pop3'
	 */
	private String protocol;

	/**
	 * Set to true to mark email as read.
	 */
	private boolean markAsRead = false;

	/**
	 * Set to true to delete email after download.
	 */
	private boolean delete = false;

	/**
	 * Set to true to use IdleImap Configuration.
	 */
	private boolean idleImap = false;

	/**
	 * JavaMail properties as a new line delimited string of name-value pairs, e.g.
	 * 'foo=bar\n baz=car'.
	 */
	private Properties javaMailProperties = new Properties();

	/**
	 * Configure a SpEL expression to select messages
	 */
	private String expression = "true";
	
	/**
	 * @return the markAsRead
	 */
	public boolean isMarkAsRead() {
		return this.markAsRead;
	}

	/**
	 * @param markAsRead the markAsRead to set
	 */
	public void setMarkAsRead(boolean markAsRead) {
		this.markAsRead = markAsRead;
	}

	/**
	 * @return the delete
	 */
	public boolean isDelete() {
		return this.delete;
	}

	/**
	 * @param delete the delete to set
	 */
	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	/**
	 * @return the idleImap
	 */
	public boolean isIdleImap() {
		return this.idleImap;
	}

	/**
	 * @param idleImap the idleImap to set
	 */
	public void setIdleImap(boolean idleImap) {
		this.idleImap = idleImap;
	}

	/**
	 * @return the javaMailProperties
	 */
	@NotNull
	public Properties getJavaMailProperties() {
		return this.javaMailProperties;
	}

	/**
	 * @param javaMailProperties the javaMailProperties to set
	 */
	public void setJavaMailProperties(Properties javaMailProperties) {
		this.javaMailProperties = javaMailProperties;
	}

	/**
	 * @return the mailUrl
	 */
	@NotNull
	public String getMailUrl() {
		return mailUrl;
	}

	/**
	 * @param mailUrl the mailUrl to set
	 */
	public void setMailUrl(String mailUrl) {
		this.mailUrl = mailUrl;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

}
