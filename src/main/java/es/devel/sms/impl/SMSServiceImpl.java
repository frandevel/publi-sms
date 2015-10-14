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

package es.devel.sms.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import es.devel.sms.SmsService;
import es.devel.sms.exception.SmsGatewayNotConfiguredException;
import es.devel.sms.model.SMSMessage;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SMSServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SMSServiceImpl.class);

    private static final String API_BASE_URL = "https://api.gateway360.com/api/3.0";
    private static final String SEND_URL = "/sms/send";
    private static final String BALANCE_URL = "/account/get-balance";

    private String phonePrefix;
    private String customizedSmsSenderName;
    private String apiKey;

    public SMSServiceImpl(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void sendSms(Collection<SMSMessage> messages) throws SmsGatewayNotConfiguredException {
        validateSmsGatewayConfiguration();

        if (messages.isEmpty()) {
            throw new IllegalArgumentException("No hay mensajes SMS que enviar");
        }
        logger.info("Iniciando envio de mensajes:");
        logger.info("Custom sender name configured: " + this.customizedSmsSenderName);
        logger.info("Number of messages to be sent: " + messages.size());

        Client client = Client.create();
        WebResource webResource = client.resource(API_BASE_URL + SEND_URL);

        ClientResponse response;

        SMSMessage[] messagesAsArray = messages.toArray(new SMSMessage[messages.size()]);

        SmsHolder smsHolder = new SmsHolder(apiKey, messagesAsArray);
        try {
            response = webResource.header("Content-Type", "application/json")
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, buildJsonMessage(smsHolder));
            logger.info("Response from email sending: {}", response);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write SMS Message to JSON");
        }
    }

    private String buildJsonMessage(SmsHolder smsHolder) throws IOException {
        return new ObjectMapper().writeValueAsString(smsHolder);
    }

    private class SmsHolder {

        @JsonProperty("api_key")
        private String apiKey;
        @JsonProperty("report_url")
        private String reportUrl;
        private int concat;
        private int fake;
        private SMSMessage[] messages;

        public SmsHolder(String apiKey, SMSMessage[] messages) {
            this.apiKey = apiKey;
            this.messages = messages;
            this.concat = 0;
            this.reportUrl = "";
            this.fake = 0;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getReportUrl() {
            return reportUrl;
        }

        public void setReportUrl(String reportUrl) {
            this.reportUrl = reportUrl;
        }

        public int getConcat() {
            return concat;
        }

        public void setConcat(int concat) {
            this.concat = concat;
        }

        public SMSMessage[] getMessages() {
            return messages;
        }

        public void setMessages(SMSMessage[] messages) {
            this.messages = messages;
        }
    }

    public void sendSms(String messageText, String... recipientMobileNumbers) throws SmsGatewayNotConfiguredException {

        validateSmsGatewayConfiguration();

        if (recipientMobileNumbers.length == 0) {
            throw new IllegalArgumentException("No se ha especificado ningún destinatario del mensaje SMS");
        }

        if ("".equalsIgnoreCase(messageText)) {
            throw new IllegalArgumentException("El texto del mensaje está vacío");
        }

        List<SMSMessage> messages = new ArrayList<>(1);
        for (String recipientMobileNumber : recipientMobileNumbers) {
            messages.add(new SMSMessage(customizedSmsSenderName, phonePrefix + recipientMobileNumber, messageText));
        }

        sendSms(messages);
    }

    public double checkCredits() throws SmsGatewayNotConfiguredException {
        validateSmsGatewayConfiguration();

        Client client = Client.create();
        WebResource webResource = client.resource(API_BASE_URL + BALANCE_URL);
        String tokenObject = getTokenAsJson();
        ClientResponse response = webResource.header("Content-Type", "application/json").post(ClientResponse.class, tokenObject);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode resultRoot = mapper.readTree(response.getEntity(String.class));
            Double credits = resultRoot.path("result").path("balance").asDouble();
            logger.info("SMS Balance: "+ credits + " / Output: " + resultRoot.toString());
            return credits;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read JSON data");
        }
    }

    private void validateSmsGatewayConfiguration() throws SmsGatewayNotConfiguredException {
        if (apiKey == null || "".equals(apiKey)) {
            throw new SmsGatewayNotConfiguredException("Token has not been provided. Please initialize the bean with a provided API token");
        }
        if (this.phonePrefix == null || this.phonePrefix.equals("")) {
            throw new SmsGatewayNotConfiguredException("Phone prefix has not been configured. Please provide a phone prefix in the format of '+34', according to you country code");
        }
    }

    public void setCustomizedSmsSenderName(String customizedSmsSenderName) {
        this.customizedSmsSenderName = customizedSmsSenderName;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    private String getTokenAsJson() {
        try {
            return "{\"api_key\":" + new ObjectMapper().writeValueAsString(apiKey) + "}";
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to JSON data");
        }
    }

}
