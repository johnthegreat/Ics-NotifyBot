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

import java.util.Comparator;
import java.util.Stack;

public class UserNotificationRequestManager {
	private static UserNotificationRequestManager instance;
	public static Comparator<UserNotificationRequest> COMPARATOR_USERNAME;
	public static Comparator<UserNotificationRequest> COMPARATOR_IDENTIFIER;
	
	static {
		instance = new UserNotificationRequestManager();
		COMPARATOR_USERNAME = new Comparator<UserNotificationRequest>() {
			@Override
			public int compare(UserNotificationRequest o1,
					UserNotificationRequest o2) {
				return o1.getUsername().compareTo(o2.getUsername());
			}
		};
		
		COMPARATOR_IDENTIFIER = new Comparator<UserNotificationRequest>() {
			@Override
			public int compare(UserNotificationRequest o1,
					UserNotificationRequest o2) {
				return o1.getIdentifier().compareTo(o2.getIdentifier());
			}
		};
	}
	
	public static UserNotificationRequestManager getInstance() {
		return instance;
	}
	
	private int lastNumber = 0;
	private Stack<Integer> idStack;
	
	private UserNotificationRequestManager() {
		this.idStack = new Stack<Integer>();
	}
	
	public int getNextAvailableNumber() {
		if (this.idStack.isEmpty()) {
			this.lastNumber++;
			this.idStack.add(this.lastNumber);
		}
		
		return this.idStack.pop();
	}
	
	public void releaseNumber(Integer number) {
		if (!this.idStack.contains(number)) {
			this.idStack.add(number);
		}
	}
}
