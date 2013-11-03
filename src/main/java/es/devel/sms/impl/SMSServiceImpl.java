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

import es.devel.sms.SmsService;
import es.devel.sms.exception.InvalidSmsRecipentDataException;
import es.devel.sms.exception.SMSServiceException;
import es.devel.sms.exception.SmsGatewayNotConfiguredException;
import es.devel.sms.model.SMSMessage;
import es.devel.sms.model.SMSRecipient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SMSServiceImpl implements SmsService {

    public static final String SOURCE_ADDRESS_PARAM_NAME = "SA";
    private static final String DESTINATION_ADDRESS_PARAM_NAME = "DA";
    private static final String HTTP_VERSION = "HTTPV3";
    private static final String MESSAGE_PARAM_NAME = "M";
    private static final String PASSWORD_PARAM_NAME = "PWD";
    private static final String ROUTE_DEFAULT_VALUE = "2";
    private static final String ROUTE_PARAM_NAME = "R";
    private static final String SMS_GATEWAY_URL = "http://sms1.gateway360.com/api/push/";
    private static final String SMS_CREDIT_CHECK_URL = "http://www.smspubli.com/api/others/get_credits.php";
    private static final String STATUS_NO_CREDITS_LEFT = "OK:-5";
    private static final String USERNAME_PARAM_NAME = "UN";
    private static final String VERSION_PARAM_NAME = "V";

    private static final int STATUS_ERROR = 300;

    private static final Logger logger = LoggerFactory.getLogger(SMSServiceImpl.class);
    private static final String STATUS_INVALID_RECIPIENT = "OK:-3";

    private String phonePrefix;
    private String customizedSmsSenderName;
    private String smsUser;
    private String smsPassword;

    @Override
    public void sendSms(Collection<SMSMessage> messagesArg, Collection<SMSRecipient> recipientListArg) throws SmsGatewayNotConfiguredException, SMSServiceException {
        validateSmsGatewayConfiguration();

        if (messagesArg.isEmpty()) {
            throw new SMSServiceException("No hay mensajes SMS que enviar");
        }
        if (recipientListArg.isEmpty()) {
            throw new SMSServiceException("No se han especificado destinatarios de los mensajes SMS a enviar");
        }
        try {
            logger.info("Iniciando envio de mensajes:");
            logger.info("Remitente configurado: " + this.customizedSmsSenderName);
            logger.info("Usuario para envio de SMS's: " + this.smsUser);

            PostMethod post = new PostMethod(SMS_GATEWAY_URL);
            HttpClient client = new HttpClient();

            logger.info("Numero total de destinatarios: " + recipientListArg.size());

            for (SMSRecipient smsRecipient : recipientListArg) {
                for (SMSMessage smsMessage : messagesArg) {
                    post.addParameter(VERSION_PARAM_NAME, HTTP_VERSION);
                    post.addParameter(USERNAME_PARAM_NAME, smsUser);
                    post.addParameter(PASSWORD_PARAM_NAME, smsPassword);
                    post.addParameter(ROUTE_PARAM_NAME, ROUTE_DEFAULT_VALUE);

                    if (customizedSmsSenderName != null && !customizedSmsSenderName.equals("")) {
                        post.addParameter(SOURCE_ADDRESS_PARAM_NAME, customizedSmsSenderName);
                    } else {
                        post.addParameter(SOURCE_ADDRESS_PARAM_NAME, "");
                    }
                    post.addParameter(DESTINATION_ADDRESS_PARAM_NAME, "34" + smsRecipient.getMobileNumber());
                    post.addParameter(MESSAGE_PARAM_NAME, smsMessage.getMessageText());

                    logger.info("Todo listo para enviar el mensaje: " + smsRecipient.getMobileNumber() + " / " + smsRecipient.getName());
                    logger.info("Texto del mensaje: " + smsMessage.getMessageText());
                    int status = client.executeMethod(post);
                    logger.info("Proceso de envío de SMS ejecutado");

                    if (status >= STATUS_ERROR) {
                        logger.info("Error de envio: " + status);
                        throw new SMSServiceException("Ha habido problemas con la pasarela SMS");
                    }

                    String response = post.getResponseBodyAsString();
                    logger.info("Respuesta del servidor: " + response);

                    if (response.contains("bloqueada")) {
                        throw new SMSServiceException("La cuenta está bloqueada");
                    }

                    if (response.contains(STATUS_NO_CREDITS_LEFT)) {
                        throw new SMSServiceException("La cuenta se ha quedado sin créditos");
                    }

                    if (response.contains(STATUS_INVALID_RECIPIENT)) {
                        throw new SMSServiceException("Destinatario inválido: " + smsRecipient.getMobileNumber());
                    }

                    if (!response.contains("OK")) {
                        throw new SMSServiceException("Se ha producido un error en el envío del SMS (" + smsRecipient.getMobileNumber() + "): " + response);
                    }
                }
            }
        } catch (Exception ex) {
            throw new SMSServiceException(ex);
        }
    }

    public void sendSms(String messageArg, String... recipientArg) throws SMSServiceException, SmsGatewayNotConfiguredException, InvalidSmsRecipentDataException {

        validateSmsGatewayConfiguration();

        if (recipientArg.length == 0) {
            throw new InvalidSmsRecipentDataException("No se ha especificado ningún destinatario del mensaje SMS");
        }

        if (messageArg.equalsIgnoreCase("")) {
            throw new SMSServiceException("El texto del mensaje está vacío");
        }

        SMSMessage message = new SMSMessage(messageArg);

        List<SMSMessage> messages = new ArrayList<SMSMessage>(1);
        List<SMSRecipient> recipients = new ArrayList<SMSRecipient>(1);

        for (String s : recipientArg) {
            SMSRecipient recipient = new SMSRecipient("", s);
            recipients.add(recipient);
        }

        messages.add(message);
        sendSms(messages, recipients);
    }

    public double checkCredits() throws SmsGatewayNotConfiguredException, SMSServiceException {
        try {
            validateSmsGatewayConfiguration();

            PostMethod post = new PostMethod(SMS_CREDIT_CHECK_URL);
            HttpClient client = new HttpClient();

            post.addParameter(USERNAME_PARAM_NAME, this.smsUser);
            post.addParameter(PASSWORD_PARAM_NAME, this.smsPassword);

            int status = client.executeMethod(post);

            if (status > STATUS_ERROR) {
                throw new SMSServiceException("No se ha podido contactar con la pasarela SMS para consultar los créditos");
            }

            String response = post.getResponseBodyAsString();
            if (response != null && !response.equals("")) {
                return Double.valueOf(response);
            } else {
                throw new SMSServiceException("Ha ocurrido un error inesperado al consultar los créditos SMS disponibles");
            }
        } catch (HttpException ex) {
            throw new SMSServiceException("Ha habido un error HTTP durante la comprobación de creditos de la cuenta SMS: " + ex.toString());
        } catch (IOException ex) {
            throw new SMSServiceException("Ha habido un error E/S durante la comprobación de creditos de la cuenta SMS: " + ex.toString());
        }
    }

    private void validateSmsGatewayConfiguration() throws SmsGatewayNotConfiguredException {
        if (this.smsUser == null || this.smsUser.equals("")) {
            throw new SmsGatewayNotConfiguredException("No se ha configurado el usuario de la pasarela SMS");
        }
        if (this.smsPassword == null || this.smsPassword.equals("")) {
            throw new SmsGatewayNotConfiguredException("No se ha configurado la clave de la pasarela SMS");
        }
        if (this.phonePrefix == null || this.phonePrefix.equals("")) {
            throw new SmsGatewayNotConfiguredException("No se ha configurado el prefijo telefonico para el envio de SMS's a través de la pasarela");
        }
    }

    public void setCustomizedSmsSenderName(String customizedSmsSenderName) {
        this.customizedSmsSenderName = customizedSmsSenderName;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public void setSmsPassword(String smsPassword) {
        this.smsPassword = smsPassword;
    }

    public void setSmsUser(String smsUser) {
        this.smsUser = smsUser;
    }

}