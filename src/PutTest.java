import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PutTest
{
	public static void main(String[] args) throws IOException, InterruptedException
	{
		String malformedPayload1 = "{]p12]";
		String emptyPayload = "";
		String emptyJson = "{}";
		String studentWithName = "{\"name\":\"Jane Doe\"}";
		
		String requestBody = studentWithName;
		
			// More or less completely copied from https://restful-api.dev/send-a-put-request-java/
		
		HttpRequest request = HttpRequest.newBuilder()
				.PUT(HttpRequest.BodyPublishers.ofString(requestBody))
				.uri(URI.create("http://127.0.0.1:8027/addStudentReport?puttingStudentId=116"))
				.header("Content-Type", "application/json")
				.build();
		
		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());
		
		System.out.println(response.statusCode());
		System.out.println(response.body());
	}
}
