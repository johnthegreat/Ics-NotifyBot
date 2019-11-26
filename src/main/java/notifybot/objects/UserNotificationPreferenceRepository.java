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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UserNotificationPreferenceRepository {
	private Map<String, UserNotificationPreference> mapByUsername;
	
	public UserNotificationPreferenceRepository() {
		this.mapByUsername = new HashMap<String, UserNotificationPreference>();
	}
	
	public Map<String, UserNotificationPreference> getMapByUsername() {
		return mapByUsername;
	}
	
	public UserNotificationRequest[] getRequestsForUsername(String username) {
		List<UserNotificationRequest> requests = new ArrayList<UserNotificationRequest>();
		
		for(Entry<String, UserNotificationPreference> entry : this.mapByUsername.entrySet()) {
			for(UserNotificationRequest request : entry.getValue().getNotificationMapByUsername().values()) {
				if (request.getUsername() != null && request.getUsername().equals(username)) {
					requests.add(request);
				}
			}
		}
		
		return requests.toArray(new UserNotificationRequest[0]);
	}
}
