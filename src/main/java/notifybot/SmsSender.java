/*
 *  NotifyBot
 *  Copyright (C) 2019 John Nahlen
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package notifybot;

import com.twilio.sdk.*;
import com.twilio.sdk.resource.factory.*;
import com.twilio.sdk.resource.instance.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class SmsSender {
	// Find your Account Sid and Auth Token at twilio.com/console
	private String ACCOUNT_SID = null;
	private String AUTH_TOKEN = null;
	private String FROM_PHONE_NUMBER = null;
	
	public SmsSender(String accountSid,String authToken,String fromPhoneNumber) {
		this.ACCOUNT_SID = accountSid;
		this.AUTH_TOKEN = authToken;
		this.FROM_PHONE_NUMBER = fromPhoneNumber;
	}

	public String sendSms(String toPhoneNumber, String msgString) throws Exception {
		TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

		// Build the parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("From", FROM_PHONE_NUMBER));
		params.add(new BasicNameValuePair("To", toPhoneNumber));
		params.add(new BasicNameValuePair("Body", msgString));

		MessageFactory messageFactory = client.getAccount().getMessageFactory();
		Message message = messageFactory.create(params);
		return message.getSid();
	}
}