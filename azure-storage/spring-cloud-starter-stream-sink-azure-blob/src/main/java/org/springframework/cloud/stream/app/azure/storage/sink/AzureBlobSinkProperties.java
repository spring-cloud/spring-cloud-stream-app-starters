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
 * Configuration properties for the Azure Blob Sink module.
 *
 * @author Kyle Dunn
 */
@ConfigurationProperties("azure.blob")
public class AzureBlobSinkProperties {

    /**
     * The default Azure endpoint protocol.
     */
    private String defaultEndpointsProtocol = "http";

    /**
     * The Azure Storage Account name.
     */
    private String account;

    /**
     * The Azure Storage Account key.
     */
    private String key;

    /**
     * The Azure Storage Container name.
     */
    private String container;

    /**
     * The Azure Storage Blob name.
     */
    private String blob;

    /**
     * Create a container if it doesn't already exist.
     */
    private boolean createContainer = true;

    /**
     * Set container access policy to public.
     */
    private boolean publicPermission = true;
    
    /**
     * Specify if using an CloudAppendBlob
     */
    private boolean appendOnly = true;

    /**
     * Specify whether to silently overwrite 
     * existing an CloudAppendBlob with the same name
     */
    private boolean overwriteExistingAppend = false;

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

    public boolean getAutoCreateContainer() {
        return autoCreateContainer;
    }

    // Automatically create the container
    public void setAutoCreateContainer(Boolean b) {
        this.autoCreateContainer = b;
    }

    public boolean getPublicPermission() {
        return publicPermission;
    }

    // Include public access in the permissions object 
    public void setPublicPermission(Boolean b) {
        this.publicPermission = b;
    }

    public boolean getAppendOnly() {
        return appendOnly;
    }

    public void setAppendOnly(Boolean appendOnly) {
        this.appendOnly = appendOnly;
    }

    public boolean getOverwiteExistingAppend() {
        return overwriteExistingAppend;
    }

    public void setOverwriteExistingAppend(Boolean overwrite) {
        this.overwriteExistingAppend = overwrite;
    }
}
