import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.greenwolf24.PolyTool.Files.CSV.ReadCsvFile;
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
	private static final String VERSION = "1.5.2";
	private static final String preExistTypeProgressReportRoot = "data/studentData/preExistTypeProgressReport/";
	private static final File preExistTypeProg_FileRoot = new File(preExistTypeProgressReportRoot);
	public static final String DATA_CATALOG_DATA_REQS_OPTIONS = "data/catalogData/ReqsOptions/";
	private static final File courseFulfillmentOptionsRoot = new File(DATA_CATALOG_DATA_REQS_OPTIONS);
	//public static final String DATA_CATALOG_DATA_CourseDescs = "data/catalogData/CourseDescs/";
	//private static final File courseDescriptionsRoot = new File(DATA_CATALOG_DATA_CourseDescs);
	private static final String DATA_COURSES_COURSE2SJSON = "data/Courses/Course2sJson/";
	private static final File course2sClassJsonsRoot = new File(DATA_COURSES_COURSE2SJSON);
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Capstone Dummy Backend Version " + VERSION);
		
		System.out.println("Checking file structure");
		
		verifyFolderStructure(preExistTypeProg_FileRoot);
		verifyFolderStructure(courseFulfillmentOptionsRoot);
		verifyFolderStructure(course2sClassJsonsRoot);
		
		System.out.println("Starting server");
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(8227),0);
		
		httpServer.createContext("/dummyBackendVersion",new DummyBackendVersionHandler());
		httpServer.createContext("/getStudentReport",new StudentReportGetterHandler());
		httpServer.createContext("/addStudentReport",new StudentReportPutterHandler());
		
		httpServer.createContext("/getCourseFulfillmentOptions",new CourseFulfillmentOptionsHandler());
		
		httpServer.createContext("/getCourseDescriptions",new CourseDescriptionsGetterHandler());
		
		httpServer.createContext("/directoryInfo.json",new DirectoryInformationHandler());
		
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
	
	private static class DirectoryInformationHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			Optional<String> filtersOpt = HttpTools.extractRequestedQueryValue(httpExchange,"filter");
			String filter = filtersOpt.orElse("NONE");
			
			JsonElement directory = switch(filter)
			{
				case "studentsNoExtensionParseToInt" -> (JsonArray) ((JsonObject) generateJsonDirectory(preExistTypeProg_FileRoot, true,true)).get(preExistTypeProg_FileRoot.getName());
				case "studentsNoExtension" -> (JsonArray) ((JsonObject) generateJsonDirectory(preExistTypeProg_FileRoot, true)).get(preExistTypeProg_FileRoot.getName());
				case "students" -> (JsonArray) ((JsonObject) generateJsonDirectory(preExistTypeProg_FileRoot)).get(preExistTypeProg_FileRoot.getName());
				default -> (JsonObject) generateJsonDirectory(new File("data"));
			};
			
			HttpTools.returnStringToHttpExchange(httpExchange,directory.toString(),200);
			return;
		}
		
		private static JsonElement generateJsonDirectory(File item)
		{
			return generateJsonDirectory(item,false,false);
		}

		private static JsonElement generateJsonDirectory(File item,boolean removeFileExtensions) {return generateJsonDirectory(item,removeFileExtensions,false);}
		
		private static JsonElement generateJsonDirectory(File item,boolean removeFileExtensions,boolean parseToInt)
		{
			if(!item.isDirectory())
			{
				String prim = item.getName();
				if(removeFileExtensions | parseToInt) prim = prim.replace(".json","");
				if(parseToInt) return new JsonPrimitive(Integer.parseInt(prim));

				return new JsonPrimitive(item.getName());
			}
			
			JsonArray subArray = new JsonArray();
			
			for(File subItem : item.listFiles())
			{
				subArray.add(generateJsonDirectory(subItem,removeFileExtensions,parseToInt));
			}
			
			JsonObject myObj = new JsonObject();
			myObj.add(item.getName(),subArray);
			
			return myObj;
		}
	}
	
	private static class DummyBackendVersionHandler implements HttpHandler
	{
		@Override
		public void handle(HttpExchange httpExchange) throws IOException
		{
			System.out.println("H 125 " + httpExchange);	
			HttpTools.returnStringToHttpExchange(httpExchange, "{\"Version\":\""+VERSION+"\"}",200);
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
				
				String theoreticalFilePath = preExistTypeProgressReportRoot+studentIdNumberString+".json";
				
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
				
				if(reqReq_Option.isEmpty())
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
			Optional<String> requested = HttpTools.extractRequestedQueryValue(httpExchange,"requested");
			Optional<String> numType = HttpTools.extractRequestedQueryValue(httpExchange,"numType");
			Optional<String> retType = HttpTools.extractRequestedQueryValue(httpExchange,"retType");
			
			if(requested.isEmpty() | numType.isEmpty() | retType.isEmpty())
			{
				HttpTools.returnStringToHttpExchange(httpExchange,"Missing Query",400);
				return;
			}
			
			String reqNumString = requested.get();
			String numTypeString = numType.get();
			String retTypeString = retType.get();
			
			String referenceNum = "";
			
			if(numTypeString.equals("MOOSE_class2s")) referenceNum = reqNumString;
			
			if(numTypeString.equals("MOOSE_crse_id")) referenceNum = crse_id_toClass2sNum(reqNumString);
			
			if(numTypeString.equals("MOOSE_courseName")) referenceNum = courseNameToClass2sNum(reqNumString);
			
			if(numTypeString.equals("MOOSE_catalogID")) referenceNum = catalogIdToClass2sNum(reqNumString);
			
			// TODO. implement more ways to get the reference number
			
			// TODO. implement returning according to retType
			
			File file = new File(DATA_COURSES_COURSE2SJSON +referenceNum+".json");
			
			if(!file.exists())
			{
				HttpTools.returnStringToHttpExchange(httpExchange,"File not found",404);
				return;
			}
			
			String retString = SimpleReader.getAsString(file);
			HttpTools.returnStringToHttpExchange(httpExchange,retString,200);
		}
		
		private static final String[][] courseNameToCourse2sClassNumCSV = new ReadCsvFile(new File("data/Courses/courseNameToCourse2sClassNum.reduced.csv")).getAsStringsArray();
		
		private static String crse_id_toClass2sNum(String crse_id)
		{
			// we currently have no way to do this
			// so we will return a random one
			
			return courseNameToCourse2sClassNumCSV[(int)(Math.random()*courseNameToCourse2sClassNumCSV.length)][1];
		}
		
		private static String catalogIdToClass2sNum(String catalogId)
		{
			// we currently have no way to do this
			// so we will return a random one
			
			return courseNameToCourse2sClassNumCSV[(int)(Math.random()*courseNameToCourse2sClassNumCSV.length)][1];
		}
		
		private static String courseNameToClass2sNum(String name)
		{
			String lookFor = name.replace(" ","").toUpperCase();
			
			for(String[] line : courseNameToCourse2sClassNumCSV)
			{
				if(line[0].equals(lookFor))
				{
					return line[1];
				}
			}
			
			// if we fail, return a random one
			return courseNameToCourse2sClassNumCSV[(int)(Math.random()*courseNameToCourse2sClassNumCSV.length)][1];
		}
	}
}
