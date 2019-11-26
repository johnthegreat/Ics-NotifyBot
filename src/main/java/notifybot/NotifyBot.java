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

import java.util.*;

import org.apache.commons.lang3.StringUtils;

import notifybot.input.InputHandler;
import notifybot.objects.Event;
import notifybot.objects.NotificationMethod;
import notifybot.objects.UserNotificationPreference;
import notifybot.objects.UserNotificationPreferenceRepository;
import notifybot.objects.UserNotificationRequest;

public class NotifyBot {
	public class KeepAliveTimer extends TimerTask {
		public void run() {
			NotifyBot.this.getServerConnection().write("date");
		}
	}
	
	//
	// Version string needs to be manually updated
	//
	public static final String version = "1.0 (2019-11-25)";
	
	private UserNotificationPreferenceRepository repository;
	private ServerConnection serverConnection;
	private Properties properties;
	private Timer actionTimer;
	private InputHandler inputHandler;
	private Set<String> adminUsernames;
	
	private Thread looperThread;
	
	public NotifyBot() {
		this.repository = new UserNotificationPreferenceRepository();
		this.inputHandler = new InputHandler(this);
		this.adminUsernames = new HashSet<>();
		this.actionTimer = new Timer();
	}
	
	public void setServerConnection(ServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}
	
	public ServerConnection getServerConnection() {
		return serverConnection;
	}
	
	public UserNotificationPreferenceRepository getRepository() {
		return repository;
	}
	
	public void issueKeepAliveTimer(int minutes) {
		final int MINUTE = 1000*60; // in ms
		actionTimer.scheduleAtFixedRate(new KeepAliveTimer(),0,MINUTE*minutes);
	}
	
	public void startLooperThread() {
		looperThread = new Thread(new Runnable() {
			public void run() {
				while(true) {
					String line = StringUtils.stripEnd(getServerConnection().read("\n\rfics%"), null);
					line = line.replace("\n\r","\n");
					line = StringUtils.stripStart(line,null);
					System.out.println(line);
					NotifyBot.this.parseLine(line);
				}
			}
		});
		looperThread.setUncaughtExceptionHandler(ThrowableHandler.getInstance().getUncaughtExceptionHandler());
		looperThread.start();
	}
	
	private void parseLine(String input) {
		this.inputHandler.parseAndHandleInput(input);
	}
	
	public void checkEvents(String username, boolean arrived) {
		Event requestedEvent = arrived ? Event.LOGIN : Event.LOGOUT;
		
		UserNotificationRequest[] requests = this.getRepository().getRequestsForUsername(username);
		for (UserNotificationRequest request : requests) {
			UserNotificationPreference preference = this.getRepository().getMapByUsername().get(request.getRequestingUsername());
			
			if (request.hasEvent(requestedEvent)) {
				NotificationMethod notificationMethod = request.getNotificationMethodForEvent(requestedEvent);
				String message = request.getMessageForEvent(requestedEvent);
				
				if (notificationMethod == NotificationMethod.TEXT) {
					String propTwilioEnable = properties.getProperty("twilioEnable");
					boolean enableTwilio = propTwilioEnable != null && propTwilioEnable.equals("true");
					if (enableTwilio) {
						if (preference.getPhoneNumber() != null) {
							SmsSender smsSender = new SmsSender(properties.getProperty("twilioAccountSid"), properties.getProperty("twilioAuthToken"), properties.getProperty("twilioFromPhoneNumber"));
							try {
								String messageSid = smsSender.sendSms(preference.getPhoneNumber(), message);
								System.out.println(messageSid);
							} catch (Exception e) {
								ThrowableHandler.getInstance().handleThrowable(e);
							}
						} else {
							System.err.println(String.format("No phone number provided. Text not sent to %s.",preference.getUsername()));
						}
					} else {
						System.out.println(String.format("Twilio support disabled. Text not sent to %s.",preference.getUsername()));
					}
				} else if (notificationMethod == NotificationMethod.NONE) {
					System.out.println(String.format("Faked sent notification to %s (NotificationMethod = NONE)", preference.getUsername()));
				}
				
				request.incrementNumTimesNotified();
				request.setLastTimeNotified(System.currentTimeMillis());
			}
		}
	}
	
	public void addAdmin(String username) {
		adminUsernames.add(username);
	}
	
	public boolean isUsernameAdmin(String username) {
		return adminUsernames.contains(username);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public Properties getProperties() {
		return properties;
	}
}
