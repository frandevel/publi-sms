/*
 * Copyright 2013 Francisco Serrano Pons
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.devel.sms.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

public class SMSMessage implements Serializable {

    private String from;
    private String to;
    private String text;
    @JsonProperty("send_at")
    private String sendAt;

    public SMSMessage(String from, String to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.sendAt = "";
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSendAt() {
        return sendAt;
    }

    public void setSendAt(String sendAt) {
        this.sendAt = sendAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SMSMessage{");
        sb.append("from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append(", sendAt='").append(sendAt).append('\'');
        sb.append('}');
        return sb.toString();
    }

}