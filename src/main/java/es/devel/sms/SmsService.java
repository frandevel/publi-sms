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

package es.devel.sms;

import es.devel.sms.exception.InvalidSmsRecipentDataException;
import es.devel.sms.exception.SMSServiceException;
import es.devel.sms.exception.SmsGatewayNotConfiguredException;
import es.devel.sms.model.SMSMessage;
import es.devel.sms.model.SMSRecipient;
import java.util.Collection;

public interface SmsService {

    void sendSms(Collection<SMSMessage> messages, Collection<SMSRecipient> recipients) throws SMSServiceException, SmsGatewayNotConfiguredException;

    void sendSms(String message, String... recipient) throws SMSServiceException, SmsGatewayNotConfiguredException, InvalidSmsRecipentDataException;

    double checkCredits() throws SmsGatewayNotConfiguredException, SMSServiceException;

}
