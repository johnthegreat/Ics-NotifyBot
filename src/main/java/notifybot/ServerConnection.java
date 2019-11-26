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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DecimalFormat;

public class ServerConnection {
	private static String size(long size) {
		if(size <= 0) return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return String.format("%s %s",new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)),units[digitGroups]);
	}
	
	// Removes all tags from a username, e.g. (SR)(TM).
	public static String stripTags(final String username) {
		int idx = username.indexOf("(");
		if (idx > -1) {
			return username.substring(0,idx);
		}
		return username;
	}
	
	protected InputStream dataIn;
	protected OutputStream dataOut;
	private Socket socket;
	
	protected long totalBytesIn;
	protected long totalBytesOut;
	
	protected PrintStream outPrintStream;
	protected PrintStream errPrintStream;
	
	public boolean connect(String address,int port,String username,String password) {
		try {
			socket = new Socket(address,port);
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = socket.getOutputStream();
			
			
			String str;
			str = read("login: ").replace("\n\r","\n");
			if (outPrintStream != null) {
				outPrintStream.println(str);
			}
			
			write(username);
			str = read(":").replace("\n\r","\n");
			if (outPrintStream != null) {
				outPrintStream.println(str);
			}
			write(password);
			String var = read("fics%").replace("\n\r","\n");
			if (outPrintStream != null) {
				outPrintStream.println(var);
			}
			if (var.contains("*** Sorry " + username + " is already logged in ***")) {
				// *** Sorry newbugbot is already logged in ***
				String msg = String.format("Connect failed: %s is already logged in.",username);
				if (errPrintStream != null) {
					errPrintStream.println(msg);
				}
				return false;
			} else if (var.contains(String.format("**** Starting FICS session as %s",username))) {
				// **** Starting FICS session as newbugbot(U) ****
				return true;
			}
			
			return true;
		} catch (Throwable t) { 
			ThrowableHandler.getInstance().handleThrowable(t);
			return false;
		}
	}
	
	public boolean connect(String username,String password) {
		return connect("freechess.org",5000,username,password);
	}
	
	public InputStream getDataInputStream() {
		return this.dataIn;
	}
	
	public OutputStream getOutputStream() {
		return this.dataOut;
	}
	
	public boolean isClosed() {
		return this.socket.isClosed();
	}
	
	public String getSingleLine() {
		return read("\n\r").replace("\n\r","");
	}
	
	public void printNetworkStats() {
		if (outPrintStream != null) {
			outPrintStream.println("Total Bytes In: " + size(totalBytesIn));
			outPrintStream.println("Total Bytes Out: " + size(totalBytesOut));
			outPrintStream.println("Total Bytes: " + size(totalBytesIn+totalBytesOut));
		}
	}
	
	public String read(String prompt) {
		char promptLastChar = prompt.charAt(prompt.length()-1);
		
		StringBuilder result = new StringBuilder(1024);
		try {
			char c;
			do {
				int last = dataIn.read();
				c = (char) last;
				result.append(c);
				
				if (promptLastChar == c && result.lastIndexOf(prompt) >= 0) {
					break;
				}
			} while (true);
		} catch (IOException e) {
			ThrowableHandler.getInstance().handleThrowable(e);
		}
		
		totalBytesIn += result.length();
		
		result = handleTimeseal(result);
		
		int idx = result.indexOf(prompt);
		if (idx < 0) {
			return result.toString();
		} else {
			return result.substring(0,idx);
		}
	}
	
	private StringBuilder handleTimeseal(StringBuilder stringBuilder) {
		int pos = -1;
		final String TIMESEAL_STRING = "[G]\0";
		int len = TIMESEAL_STRING.length();
		// YOU MUST ACK EACH [G]\0!!
		while((pos = stringBuilder.indexOf(TIMESEAL_STRING)) >= 0) {
			this.write("\0029\n");
			stringBuilder.replace(pos, pos+len, "");
		}
		return stringBuilder;
	}
	
	public void writeBytes(byte[] bytes) {
		if (bytes.length == 0) throw new IllegalArgumentException();
		
		totalBytesOut += bytes.length;
		try {
			dataOut.write(bytes, 0, bytes.length);
			dataOut.flush();
		} catch (IOException e) {
			ThrowableHandler.getInstance().handleThrowable(e);
		}
	}
	
	public void write(String message) {
		if (message == null) {
			throw new IllegalArgumentException();
		}
		
		message += "\r\n";
		this.writeBytes(message.getBytes());
	}
	
	public void writeBatch(String[] array) {
		for(String s : array) {
			write(s);
		}
	}
	
	public void setOutPrintStream(PrintStream outPrintStream) {
		this.outPrintStream = outPrintStream;
	}
	
	public void setErrPrintStream(PrintStream errPrintStream) {
		this.errPrintStream = errPrintStream;
	}
}
