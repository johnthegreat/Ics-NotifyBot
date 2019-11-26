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
package notifybot.input;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import notifybot.NotifyBot;
import notifybot.NotifyBotUtils;
import notifybot.ServerConnection;
import notifybot.ThrowableHandler;
import notifybot.objects.UserNotificationPreference;

public class InputHandler {
	private final String pinPatternStr = "\\[(\\w{3,17}) has ((?:dis)?connected).]";
	private final Pattern pinPattern = Pattern.compile(pinPatternStr);
	
	private final String notificationPatternStr = "^Notification: (\\w{3,17}) has (arrived|departed).$";
	private final Pattern notificationPattern = Pattern.compile(notificationPatternStr);
	
	private final String personalTellPatternStr = "^(\\w{3,17}(?:\\(.*\\))?) tells you: (.*)$";
	private final Pattern personalTellPattern = Pattern.compile(personalTellPatternStr);
	
	private NotifyBot notifyBot;
	
	public InputHandler(NotifyBot notifyBot) {
		this.notifyBot = notifyBot;
	}
	
	public ParsedCommand parseAndHandleInput(String input) {
		ParsedCommand parsedCommand = ParsedCommand.COMMAND_UNKNOWN;
		boolean matched = false;
		
		matched = this.tryParsePin(input);
		parsedCommand = ParsedCommand.COMMAND_PIN;
		
		if (!matched) {
			matched = this.tryParseNotification(input);
			parsedCommand = ParsedCommand.COMMAND_NOTIFICATION;
		}
		
		if (!matched) {
			matched = this.tryParsePersonalTell(input);
			parsedCommand = ParsedCommand.COMMAND_TELL;
		}
		
		return parsedCommand;
	}
	
	private boolean tryParsePersonalTell(String input) {
		Matcher m = personalTellPattern.matcher(input);
		if (m.matches()) {
			final String username = ServerConnection.stripTags(m.group(1));
			String message = m.group(2).trim();
			
			// parse message
			
			if (message.equals("quit") && notifyBot.isUsernameAdmin(username)) {
				this.notifyBot.getServerConnection().write("quit");
				try {
					this.notifyBot.getServerConnection().getDataInputStream().close();
					this.notifyBot.getServerConnection().getOutputStream().close();
				} catch (IOException e) {
					ThrowableHandler.getInstance().handleThrowable(e);
				}
				System.exit(0);
			}
			
			if (message.equals("print")) {
				UserNotificationPreference userNotificationPreference = this.notifyBot.getRepository().getMapByUsername().get(username);
				if (userNotificationPreference == null) {
					System.out.println(String.format("No user preference found for %s.",username));
				} else {
					System.out.println(NotifyBotUtils.buildRequestsTable(userNotificationPreference));
				}
			}
			
			if (message.equals("version")) {
				this.notifyBot.getServerConnection().write(String.format("tell %s NotifyBot version: %s", username, NotifyBot.version));
			}
		}
		return false;
	}
	
	private boolean tryParseNotification(String input) {
		Matcher m = notificationPattern.matcher(input);
		if (m.matches()) {
			String username = m.group(1);
			boolean arrived = m.group(2).equals("arrived");
			
			this.notifyBot.checkEvents(username, arrived);
			return true;
		}
		return false;
	}
	
	private boolean tryParsePin(String input) {
		Matcher m = pinPattern.matcher(input);
		if (m.matches()) {
			String username = m.group(1);
			boolean connected = m.group(2).equals("connected");
			
			this.notifyBot.checkEvents(username, connected);
			return true;
		}
		return false;
	}
}
