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

import notifybot.objects.Event;
import notifybot.objects.NotificationMethod;
import notifybot.objects.UserNotificationPreference;
import notifybot.objects.UserNotificationRequest;

import java.io.FileReader;
import java.util.Properties;

public class Main {
	public static void main(String[] args) throws Exception {
		NotifyBot notifyBot = new NotifyBot();
		
		String propertiesFilePath = args.length >= 1 ? args[0] : null;
		if (propertiesFilePath != null) {
			Properties properties = new Properties();
			properties.load(new FileReader(propertiesFilePath));
			notifyBot.setProperties(properties);
		} else {
			System.out.println(String.format("NotifyBot %s",NotifyBot.version));
			System.out.println();
			System.out.println("Usage: NotifyBot <path to config file>");
			System.out.println();
			System.exit(1);
		}
		
		notifyBot.setServerConnection(new ServerConnection());
		notifyBot.getServerConnection().setOutPrintStream(System.out);
		notifyBot.getServerConnection().setErrPrintStream(System.err);
		
		String host = notifyBot.getProperties().getProperty("host");
		int port = Integer.parseInt(notifyBot.getProperties().getProperty("port"));
		String username = notifyBot.getProperties().getProperty("username");
		String password = notifyBot.getProperties().getProperty("password");
		String adminListStr = notifyBot.getProperties().getProperty("adminList");
		if (adminListStr != null) {
			String[] admins = adminListStr.split(",");
			for(String admin : admins) {
				notifyBot.addAdmin(admin.trim());
			}
		}
		
		if (notifyBot.getServerConnection().connect(host, port, username, password)) {
			notifyBot.startLooperThread();
			notifyBot.getServerConnection().writeBatch(new String[] {
					"-ch 4",
					"-ch 53",
					"set pin 1",
					"set bell 0",
					"set seek 0",
					"set open 0",
					"set formula 0",
					String.format("set 1 NotifyBot v%s", NotifyBot.version),
					"set 2 Commands: version"
			});
			
			String keepAliveMinutesStr = notifyBot.getProperties().getProperty("keepAliveMinutes");
			if (keepAliveMinutesStr != null) {
				int keepAliveMinutes = 0;
				try {
					keepAliveMinutes = Integer.parseInt(keepAliveMinutesStr);
				} catch (NumberFormatException e) {
					keepAliveMinutes = 50;
				}
				if (keepAliveMinutes != 0) {
					notifyBot.issueKeepAliveTimer(keepAliveMinutes);
				}
			}
		}
		
		/*
		 * To use NotifyBot:
		 *
		 * Create a new instance of UserNotificationPreference with who we will be notifying upon an event.
		 * Create a new instance of UserNotificationRequest with the username we are looking for events on.
		 * On the UserNotificationRequest instance, specify the event details.
		 * Add UserNotificationRequest to UserNotificationPreference instance.
		 * Register the UserNotificationPreference with the NotifyBot instance.
		 */
		
		// Create UserNotificationPreference
		UserNotificationPreference userNotificationPreference = new UserNotificationPreference();
		userNotificationPreference.setUsername(""); // Set username
		userNotificationPreference.setPhoneNumber(""); // Set phone number
		
		// Create UserNotificationRequest
		UserNotificationRequest r1 = new UserNotificationRequest();
		r1.setRequestingUsername(userNotificationPreference.getUsername());
		r1.setUsername("mattuc"); // Change username
		// Register Event
		// Refer to Event enum
		r1.registerEvent(Event.LOGOUT, NotificationMethod.TEXT, "Notification: mattuc has departed."); // Change message to send
		
		// Add UserNotificationRequest to UserNotificationPreference instance.
		userNotificationPreference.getNotificationMapByUsername().put(r1.getUsername(), r1);
		
		// Register the UserNotificationPreference with the NotifyBot instance.
		notifyBot.getRepository().getMapByUsername().put(userNotificationPreference.getUsername(), userNotificationPreference);
	}
}
