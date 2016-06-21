/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.trigger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.app.time.DateFormat;
import org.springframework.util.StringUtils;

@ConfigurationProperties("trigger")
public class TriggerPropertiesMaxMessagesDefaultOne implements TriggerProperties  {

    /***
     * Maximum messages per poll, -1 means infinity.
     */
    private long maxMessages = 1;

    /**
     * Fixed delay for periodic triggers.
     */
    private int fixedDelay = 1;
    /**
     * Initial delay for periodic triggers.
     */
    private int initialDelay = 0;
    /**
     * The TimeUnit to apply to delay values.
     */
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * The date value for the date trigger.
     */
    private String date;

    /**
     * Format for the date value.
     */
    private String dateFormat = TriggerConstants.DATE_FORMAT;

    /**
     * Cron expression value for the Cron Trigger.
     */
    private String cron;

    
    public long getMaxMessages() {
        return this.maxMessages;
    }

    
    public void setMaxMessages(long maxMessages) {
        this.maxMessages = maxMessages;
    }

    
    @Min(0)
    public int getInitialDelay() {
        return initialDelay;
    }

    
    public void setInitialDelay(int initialDelay) {
        this.initialDelay = initialDelay;
    }

    
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    
    public int getFixedDelay() {
        return fixedDelay;
    }

    
    public void setFixedDelay(int fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    
    public String getCron() {
        return this.cron;
    }

    
    public void setCron(String cron) {
        this.cron = cron;
    }

    
    public Date getDate() {
        if (StringUtils.hasText(this.date)) {
            try {
                return new SimpleDateFormat(this.dateFormat).parse(this.date);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException(e);
            }
        }
        else {
            return null;
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    @DateFormat
    public String getDateFormat() {
       return dateFormat;
    }

    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @AssertFalse
    public boolean isMutuallyExclusive() {
        return this.date != null && this.cron != null && this.fixedDelay != 1;
    }
}
