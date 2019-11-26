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
package notifybot.objects;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class UserNotificationRequest {
	private Integer identifier;
	private String requestingUsername;
	private String username;
	private Set<Event> events;
	private Map<Event, NotificationMethod> notificationMethodMap;
	private Map<Event, String> messageMap;
	private int numTimesNotified = 0;
	private Long lastTimeNotified = null;
	
	public UserNotificationRequest() {
		this.identifier = UserNotificationRequestManager.getInstance().getNextAvailableNumber();
		this.events = new HashSet<Event>();
		this.notificationMethodMap = new HashMap<Event, NotificationMethod>();
		this.messageMap = new HashMap<Event, String>();
	}
	
	public void registerEvent(Event event, NotificationMethod notificationMethod, String messageToSend) {
		this.events.add(event);
		this.notificationMethodMap.put(event, notificationMethod);
		this.messageMap.put(event, messageToSend);
	}
	
	public void updateNotificationMethodForEvent(Event event, NotificationMethod notificationMethod) {
		this.notificationMethodMap.put(event, notificationMethod);
	}
	
	public void updateMessageForEvent(Event event, String message) {
		this.messageMap.put(event, message);
	}
	
	public Set<Event> getEvents() {
		return Collections.unmodifiableSet(events);
	}
	
	public boolean hasEvent(Event event) {
		return this.events.contains(event);
	}
	
	public NotificationMethod getNotificationMethodForEvent(Event event) {
		return this.notificationMethodMap.get(event);
	}
	
	public String getMessageForEvent(Event event) {
		return this.messageMap.get(event);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getRequestingUsername() {
		return requestingUsername;
	}
	
	public void setRequestingUsername(String requestingUsername) {
		this.requestingUsername = requestingUsername;
	}
	
	public Long getLastTimeNotified() {
		return lastTimeNotified;
	}
	
	public void setLastTimeNotified(Long lastTimeNotified) {
		this.lastTimeNotified = lastTimeNotified;
	}
	
	public int getNumTimesNotified() {
		return numTimesNotified;
	}
	
	public void setNumTimesNotified(int numTimesNotified) {
		this.numTimesNotified = numTimesNotified;
	}
	
	public void incrementNumTimesNotified() {
		this.numTimesNotified++;
	}
	
	public void setIdentifier(Integer identifier) {
		this.identifier = identifier;
	}
	
	public Integer getIdentifier() {
		return identifier;
	}
}
