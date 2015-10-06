/*
 * Copyright 2013 JBoss Inc
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

import es.devel.sms.exception.SmsGatewayNotConfiguredException;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class SMSServiceImplTest {

    private static final String TOKEN = "";

    @Test
    @Ignore
    public void checkCredits_validState_intValueReturned() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);
        smsService.setCustomizedSmsSenderName("");
        smsService.setPhonePrefix("+34");

        // Act
        double credits = smsService.checkCredits();

        // Assert
        assertTrue("El valor de créditos es inválido", credits >= 0);

    }

    @Test
    @Ignore
    public void testSendSms() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);
        smsService.setCustomizedSmsSenderName("DEVEL");
        smsService.setPhonePrefix("+34");

        // Act
        smsService.sendSms("Test sms con la versión 3", "");

        // Assert

    }

    @Test(expected = SmsGatewayNotConfiguredException.class)
    @Ignore
    public void checkCredits_smsGatewayNotConfigured_exceptionIsThrown() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);

        // Act
        smsService.checkCredits();

        // Assert
        fail("No se ha lanzado la excepcion esperada");
    }

    @Test(expected = SmsGatewayNotConfiguredException.class)
    @Ignore
    public void checkCredits_noPreffixConfigured_exceptionIsThrown() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);
        smsService.setPhonePrefix("");

        // Act
        smsService.checkCredits();

        // Assert
        fail("No se ha lanzado la excepcion esperada");
    }

    @Test(expected = SmsGatewayNotConfiguredException.class)
    @Ignore
    public void checkCredits_noUserConfigured_exceptionIsThrown() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);

        // Act
        smsService.checkCredits();

        // Assert
        fail("No se ha lanzado la excepcion esperada");
    }

    @Test(expected = SmsGatewayNotConfiguredException.class)
    @Ignore
    public void checkCredits_noPasswordConfigured_exceptionIsThrown() throws Exception {
        // Arrange
        SMSServiceImpl smsService = new SMSServiceImpl(TOKEN);

        // Act
        smsService.checkCredits();

        // Assert
        fail("No se ha lanzado la excepcion esperada");
    }

}