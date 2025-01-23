import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.greenwolf24.PolyTool.Files.SimpleReader;
import io.github.greenwolf24.PolyTool.Files.SimpleWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Optional;

// dummy backend, simply grabs the requested file if available
public class Main
{
	private static final String version = "1.2.2";
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Starting server");
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(8227),0);
		
		httpServer.createContext("/dummyBackendVersion",new DummyBackendVersionHandler());
		httpServer.createContext("/getStudentReport",new StudentReportGetterHandler());
		httpServer.createContext("/addStudentReport",new StudentReportPutterHandler());
		
		httpServer.setExecutor(null);
		httpServer.start();
		
		System.out.println("Server up");
	}
	
	private static class DummyBackendVersionHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			HttpTools.returnStringToHttpExchange(httpExchange,version,200);
		}
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
					HttpTools.returnStringToHttpExchange(httpExchange, "Request Query Missing", 400);
					return;
				}
				
				try
				{
					Integer.parseInt(requestedStudentString.get());
				} catch(NumberFormatException nfex)
				{
					HttpTools.returnStringToHttpExchange(httpExchange, "Not A Number", 400);
					return;
				}
				
				String studentIdNumberString = requestedStudentString.get();
				
				String theoreticalFilePath = "data/"+studentIdNumberString+".json";
				
				File studentFile = new File(theoreticalFilePath);
				
				if(!studentFile.exists()) {HttpTools.returnStringToHttpExchange(httpExchange,"Student File Not Found",404);}
				
				String retString = SimpleReader.getAsString(studentFile);
				
				HttpTools.returnStringToHttpExchange(httpExchange,retString,200);
			}
			catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Internal Error",500);}
		}
	}
	
	private static class StudentReportPutterHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			// Handling a PUT request with a body adapted from Google AI Summary result
			try
			{
				if(!httpExchange.getRequestMethod().equalsIgnoreCase("PUT")){HttpTools.returnStringToHttpExchange(httpExchange,"Not a PUT",405);}
				
				// TODO modify tag
				// TODO modification key
				// TODO authentication check
				// TODO extract authentication method
				
				Optional<String> puttingStudentString = HttpTools.extractRequestedQueryValue(httpExchange, "puttingStudentId");
				
				if(puttingStudentString.isEmpty())
				{
					HttpTools.returnStringToHttpExchange(httpExchange, "Put Number Query Missing", 400);
					return;
				}
				
				try
				{
					Integer.parseInt(puttingStudentString.get());
				} catch(NumberFormatException nfex)
				{
					HttpTools.returnStringToHttpExchange(httpExchange, "Not A Number", 400);
					return;
				}
				
				String studentIdNumberString = puttingStudentString.get();
				
				String theoreticalFilePath = "data/"+studentIdNumberString+".json";
				
				File studentFile = new File(theoreticalFilePath);
				
				// TODO modify tag
				if(studentFile.exists()) {HttpTools.returnStringToHttpExchange(httpExchange,"Student File Exists",409	);}
				
				InputStream bodyStream = httpExchange.getRequestBody();
				byte[] bodyData = bodyStream.readAllBytes();
				bodyStream.close();
				String bodyString = new String(bodyData);
				
				try
				{
					JsonObject jsonStudent = new Gson().fromJson(bodyString, JsonObject.class);
				}catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Json Fail. Not JSON or Malformed JSON",415);return;}
				
				SimpleWriter.writeString(bodyString,studentFile);
				
				HttpTools.returnStringToHttpExchange(httpExchange,"Student Saved",200);
			}catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Internal Error",500);}
		}
	}
}