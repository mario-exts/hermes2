package vn.speedsms.client.utils;

import java.util.Base64;

public class AuthorizationUtils {

	public static void main(String[] args) {
		if (args.length > 0) {
			String accessToken = args[0];
			System.out.println("Authorization: " + generateBasicAuth(accessToken));
		} else {
			System.out
					.println("[Usage]: java -cp .:./lib/*: vn.speedsms.client.utils.AuthorizationUtils <accessToken>");
		}
	}

	public static final String generateBasicAuth(String accessToken) {
		String userCredentials = accessToken + ":x";
		return "Basic " + Base64.getEncoder().encodeToString(userCredentials.getBytes());
	}
}
