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
	private static final String VERSION = "1.4.0";
	private static final String preExistTypeProgressReportRoot = "data/studentData/preExistTypeProgressReport/";
	private static final File preExistTypeProg_FileRoot = new File(preExistTypeProgressReportRoot);
	public static final String DATA_CATALOG_DATA_REQS_OPTIONS = "data/catalogData/ReqsOptions/";
	private static final File courseFulfillmentOptionsRoot = new File(DATA_CATALOG_DATA_REQS_OPTIONS);
	public static final String DATA_CATALOG_DATA_CourseDescs = "data/catalogData/CourseDescs/";
	private static final File courseDescriptionsRoot = new File(DATA_CATALOG_DATA_CourseDescs);
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Capstone Dummy Backend Version " + VERSION);
		
		System.out.println("Checking file structure");
		
		verifyFolderStructure(preExistTypeProg_FileRoot);
		verifyFolderStructure(courseFulfillmentOptionsRoot);
		verifyFolderStructure(courseDescriptionsRoot);
		
		System.out.println("Starting server");
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(8227),0);
		
		httpServer.createContext("/dummyBackendVersion",new DummyBackendVersionHandler());
		httpServer.createContext("/getStudentReport",new StudentReportGetterHandler());
		httpServer.createContext("/addStudentReport",new StudentReportPutterHandler());
		
		httpServer.createContext("/getCourseFulfillmentOptions",new CourseFulfillmentOptionsHandler());
		
		httpServer.createContext("/getCourseDescriptions",new CourseDescriptionsGetterHandler());
		
		httpServer.setExecutor(null);
		httpServer.start();
		
		System.out.println("Server up");
	}
	
	private static void verifyFolderStructure(File folder)
	{
		if(folder.exists() && folder.isDirectory())
		{
			System.out.println("--- Folder is found");
			System.out.println("--- " + folder.listFiles().length + " files found for " + folder.getName());
		} else
		{
			System.out.println("--- Folder not found. Creating");
			folder.mkdirs();
			System.out.println("--- Success");
		}
	}
	
	// TODO
	//  FileStructureHandler which returns a json object that is the directory tree
	
	private static class DummyBackendVersionHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			HttpTools.returnStringToHttpExchange(httpExchange, VERSION,200);
		}
	}
	
	private static class StudentReportGetterHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			try
			{
				// TODO authentication
				// TODO extract authentication method
				
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
				boolean overwrite = httpExchange.getRequestMethod().equalsIgnoreCase("PUT");
				if(httpExchange.getRequestMethod().equalsIgnoreCase("POST") | httpExchange.getRequestMethod().equalsIgnoreCase("PUT")){HttpTools.returnStringToHttpExchange(httpExchange,"Method needs to be POST or PUT",405);return;}
				
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
				
				if(studentFile.exists() && !overwrite) {HttpTools.returnStringToHttpExchange(httpExchange,"Student File Exists with POST used",409	);return;}
				
				InputStream bodyStream = httpExchange.getRequestBody();
				byte[] bodyData = bodyStream.readAllBytes();
				bodyStream.close();
				String bodyString = new String(bodyData);
				
				try
				{
					new Gson().fromJson(bodyString, JsonObject.class);
				}catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Json Fail. Not JSON or Malformed JSON",415);return;}
				
				SimpleWriter.writeString(bodyString,studentFile);
				
				HttpTools.returnStringToHttpExchange(httpExchange,"Student Saved",200);
			}catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Internal Error",500);}
		}
	}
	
	private static class CourseFulfillmentOptionsHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			try
			{
				Optional<String> reqGroup_Option = HttpTools.extractRequestedQueryValue(httpExchange,"reqGroup");
				
				if(reqGroup_Option.isEmpty())
				{
					HttpTools.returnStringToHttpExchange(httpExchange,"Requested Group Query Missing",400);
					return;
				}
				
				Optional<String> reqReq_Option = HttpTools.extractRequestedQueryValue(httpExchange,"reqReq");
				
				if(reqGroup_Option.isEmpty())
				{
					HttpTools.returnStringToHttpExchange(httpExchange,"Requested Requirement Query Missing",400);
					return;
				}
				
				// TODO. generate new requirements on the fly, this only returns one single thing
				//        or at least get a few more and then pick random if non-existent
				
				
				File file = new File(DATA_CATALOG_DATA_REQS_OPTIONS+"Q_rg12345_rq54321.json");
				
				if(!file.exists())
				{
					// Should not happen when returning a dummy response as we are now
					HttpTools.returnStringToHttpExchange(httpExchange,"File not found",404);
					return;
				}
				
				String retString = SimpleReader.getAsString(file);
				HttpTools.returnStringToHttpExchange(httpExchange,retString,200);
				
			}catch(Exception ex){HttpTools.returnStringToHttpExchange(httpExchange,"Unexpected server error",500);}
		}
	}
	
	private static class CourseDescriptionsGetterHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			Optional<String> requestedCourseOption = HttpTools.extractRequestedQueryValue(httpExchange,"course");
			
			if(requestedCourseOption.isEmpty())
			{
				HttpTools.returnStringToHttpExchange(httpExchange,"Requested Course Query Empty",400);
				return;
			}
			
			// TODO. add as many course descriptions as possible, this only returns one
			// TODO. add at least a couple more descriptions, and make it select a random one if a requested course doesn't exist
			
			File file = new File(DATA_CATALOG_DATA_CourseDescs+"Q_11111.json");
			
			if(!file.exists())
			{
				// Should not happen when returning a dummy response as we are now
				HttpTools.returnStringToHttpExchange(httpExchange,"File not found",404);
				return;
			}
			
			String retString = SimpleReader.getAsString(file);
			HttpTools.returnStringToHttpExchange(httpExchange,retString,200);
			
			
		}
	}
}