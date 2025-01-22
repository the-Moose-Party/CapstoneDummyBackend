import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.greenwolf24.PolyTool.Files.SimpleReader;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

// dummy backend, simply grabs the requested file if available
public class Main
{
	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting server");
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(8027),0);
		
		httpServer.createContext("/getStudentReport",new StudentReportGetterHandler());
		
		httpServer.setExecutor(null);
		httpServer.start();
	}
	
	private static class StudentReportGetterHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			try
			{
				Optional<String> requestedStudentString = HttpTools.extractRequestedQueryValue(httpExchange, "requestedStudentId");
				
				if(requestedStudentString.isEmpty())
				{
					HttpTools.returnStringToHttpExchange(httpExchange, "{}", 400);
					return;
				}
				
				try
				{
					Integer.parseInt(requestedStudentString.get());
				} catch(NumberFormatException nfex)
				{
					HttpTools.returnStringToHttpExchange(httpExchange, "{}", 400);
					return;
				}
				
				String studentIdNumberString = requestedStudentString.get();
				
				String theoreticalFilePath = "data/"+studentIdNumberString+".json";
				
				File studentFile = new File(theoreticalFilePath);
				
				if(!studentFile.exists()) {HttpTools.returnStringToHttpExchange(httpExchange,"{}",404);}
				
				String retString = SimpleReader.getAsString(studentFile);
				
				HttpTools.returnStringToHttpExchange(httpExchange,retString,200);
			}
			catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"{}",500);}
		}
	}
}