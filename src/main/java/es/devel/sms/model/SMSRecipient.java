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

import es.devel.sms.exception.InvalidSmsRecipentDataException;

import java.io.Serializable;

public class SMSRecipient implements Serializable {

    private static final long serialVersionUID = 5789511704681845325L;
    private String name;
    private String mobileNumber;

    public SMSRecipient(String name, String mobileNumber) throws InvalidSmsRecipentDataException {
        this.name = name;
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}