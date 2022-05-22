package dasniko.keycloak.authenticator.gateway;

import org.jboss.logging.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class AwsSmsService implements SmsService {

	private static final Logger LOG = Logger.getLogger(AwsSmsService.class);
	private final String api_token;
	private final String sid;

	private static final HttpClient httpClient = HttpClient.newBuilder()
		.version(HttpClient.Version.HTTP_1_1)
		.connectTimeout(Duration.ofSeconds(10))
		.build();

	AwsSmsService(Map<String, String> config) {
		api_token = config.get("api_token");
		sid = config.get("sid");
	}

	@Override
	public void send(String phoneNumber, String message) {
		Map<String, Object> obj = new HashMap<>();

		Calendar mCalendar = Calendar.getInstance();
		Date date = mCalendar.getTime();

		obj.put("api_token", api_token);
		obj.put("sid", sid);
		obj.put("msisdn", phoneNumber);
		obj.put("sms", message);
		obj.put("csms_id", Long.toHexString(date.getTime()/1000));


		String json = "{"+obj.entrySet().stream()
			.map(e -> "\""+ e.getKey() + "\":\"" + String.valueOf(e.getValue()) + "\"")
			.collect(Collectors.joining(", "))+"}";


		try {
			HttpRequest request = HttpRequest.newBuilder()
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json))
				.uri(URI.create("https://smsplus.sslwireless.com/api/v3/send-sms"))
				.build();

			HttpResponse response =  httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			LOG.warn(response.body().toString());

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
