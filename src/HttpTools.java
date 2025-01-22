import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Optional;

public class HttpTools
{
	public static Optional<String> extractRequestedQueryValue(HttpExchange httpExchange,String key)
	{
		return extractQueryValue(httpExchange.getRequestURI().getQuery(),key);
	}
	
	public static Optional<String> extractQueryValue(String query, String key)
	{
		if(query == null) return Optional.empty();
		
		String[] pairs = query.split("&");
		
		for(String pair : pairs)
		{
			String[] splitPair = pair.split("=");
			if(splitPair[0].equals(key))
			{
				return Optional.of(splitPair[1]);
			}
		}
		
		return Optional.empty();
	}
	
	public static void returnStringToHttpExchange(HttpExchange httpExchange,String string,int code) throws IOException
	{
		// Google AI Overview result used to know how to use string compared to my old KML one
		
		httpExchange.sendResponseHeaders(code,string.getBytes().length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(string.getBytes());
		os.close();
		httpExchange.close();
	}
}
