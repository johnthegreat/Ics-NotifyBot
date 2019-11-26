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

import java.lang.Thread.UncaughtExceptionHandler;

public class ThrowableHandler {
	private static ThrowableHandler instance = new ThrowableHandler();
	public static ThrowableHandler getInstance() {
		return instance;
	}
	
	
	private UncaughtExceptionHandler uncaughtExceptionHandler;
	
	private ThrowableHandler() {
		this.uncaughtExceptionHandler = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				ThrowableHandler.this.handleThrowable(e);
			}
		};
	}
	
	public void handleThrowable(Throwable t) {
		t.printStackTrace(System.err);
	}
	
	public UncaughtExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}
}
