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
package org.springframework.cloud.stream.app.azure.storage.sink;

import static org.springframework.integration.handler.LoggingHandler.Level.*;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.integration.handler.LoggingHandler;

/**
 * Configuration properties for the Wasb Sink module.
 *
 * @author Kyle Dunn
 */
@ConfigurationProperties
public class WasbSinkProperties {

    /**
     * The name to use.
     */
    @Value("${spring.application.name:wasb.sink}")
    private String name;

    /**
     * The level at which to log messages.
     */
    private LoggingHandler.Level level = INFO;

    /**
     * The default Azure endpoint protocol.
     */
    private String defaultEndpointsProtocol = "http";

    /**
     * The Azure Storage Account name.
     */
    private String accountName;

    /**
     * The Azure Storage Account key.
     */
    private String accountKey;

    /**
     * The Azure Storage Container name.
     */
    private String containerName;

    /**
     * The Azure Storage Blob name.
     */
    private String blobName;

    /**
     * Set container access policy to public.
     */
    private Boolean autoCreateContainer = true;

    /**
     * Create a container if it doesn't already exist.
     */
    private Boolean publicPermission = true;
    
    /**
     * Specify if using an CloudAppendBlob
     */
    private Boolean appendOnly = true;

    /**
     * Specify whether to silently overwrite 
     * existing an CloudAppendBlob with the same name
     */
    private Boolean overwriteExistingAppend;

    @NotBlank
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public LoggingHandler.Level getLevel() {
        return level;
    }

    public void setLevel(LoggingHandler.Level level) {
        this.level = level;
    }

    public String getDefaultEndpointsProtocol() {
        return defaultEndpointsProtocol;
    }

    public void setDefaultEndpointsProtocol(String p) {
        this.defaultEndpointsProtocol = p;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String n) {
        this.accountName = n;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String k) {
        this.accountKey = k;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String c) {
        this.containerName = c;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String b) {
        this.blobName = b;
    }

    public Boolean getAutoCreateContainer() {
        return autoCreateContainer;
    }

    // Automatically create the container
    public void setAutoCreateContainer(Boolean b) {
        this.autoCreateContainer = b;
    }

    public Boolean getPublicPermission() {
        return publicPermission;
    }

    // Include public access in the permissions object 
    public void setPublicPermission(Boolean b) {
        this.publicPermission = b;
    }

    public Boolean getAppendOnly() {
        return appendOnly;
    }

    public void setAppendOnly(Boolean appendOnly) {
        this.appendOnly = appendOnly;
    }

    public Boolean getOverwiteExistingAppend() {
        return overwriteExistingAppend;
    }

    public void setOverwriteExistingAppend(Boolean overwrite) {
        this.overwriteExistingAppend = overwrite;
    }
}
