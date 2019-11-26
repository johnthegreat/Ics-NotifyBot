package notifybot;

import notifybot.objects.Event;
import notifybot.objects.UserNotificationPreference;
import notifybot.objects.UserNotificationRequest;
import notifybot.objects.UserNotificationRequestManager;
import org.apache.commons.lang3.StringUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class NotifyBotUtils {
	public static String buildRequestsTable(UserNotificationPreference userNotificationPreference) {
		final String newline = "\n";
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("%-4s %-17s %-6s %-6s %-45s %-5s %-26s%s", "Id","Username","Event","Method","Message","Count","Last Triggered",newline));
		
		stringBuilder.append(StringUtils.repeat('-', stringBuilder.length()-1));
		stringBuilder.append(newline);
		List<UserNotificationRequest> userNotificationRequests = new ArrayList<UserNotificationRequest>(userNotificationPreference.getNotificationMapByUsername().values());
		userNotificationRequests.sort(UserNotificationRequestManager.COMPARATOR_IDENTIFIER);
		for(UserNotificationRequest request : userNotificationRequests) {
			for (Event event : request.getEvents()) {
				stringBuilder.append(String.format("%-4d %-17s %-6s %-6s %-45s %-5s %-26s%s",request.getIdentifier(), request.getUsername(), event.getFriendlyName(), request.getNotificationMethodForEvent(event).getFriendlyName(), request.getMessageForEvent(event), request.getNumTimesNotified(), formatTimestamp(request.getLastTimeNotified()), newline));
			}
		}
		return stringBuilder.toString();
	}
	
	private static String formatTimestamp(Long timestamp) {
		if (timestamp == null) {
			return "Never";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a z");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(new Date(timestamp));
	}
}
