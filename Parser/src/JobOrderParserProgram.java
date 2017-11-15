

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.FeatureMap;
import gate.Gate;
import gate.Document;
import gate.util.GateException;
import gate.util.Out;
import gate.Factory;
import gate.creole.SerialAnalyserController;
import static gate.Utils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ToXMLContentHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonObject;



public class JobOrderParserProgram {
	final static Logger logger = Logger.getLogger(ResumeParserProgram.class);
    private  Annie annie;
    
    JobOrderParserProgram(){
        try {
            Out.prln("Initialising basic system...");
            Gate.init();
            Out.prln("...basic system initialised");

        }catch (Exception e){
            logger.info(e);
        }
        try {
            annie = new Annie();
            annie.initAnnie();
        }catch (Exception e){
            logger.info(e);
        }
	}
	
	File parseToHTMLUsingApacheTikka(String file)
			throws IOException, SAXException, TikaException {
		// determine extension
		String ext = FilenameUtils.getExtension(file);
		String outputFileFormat = "";
		// ContentHandler handler;
		if (ext.equalsIgnoreCase("html") | ext.equalsIgnoreCase("pdf")
				| ext.equalsIgnoreCase("doc") | ext.equalsIgnoreCase("docx")) {
			outputFileFormat = ".html";
			// handler = new ToXMLContentHandler();
		} else if (ext.equalsIgnoreCase("txt") | ext.equalsIgnoreCase("rtf")) {
			outputFileFormat = ".txt";
		} else {
			System.out.println("Input format of the file " + file
					+ " is not supported.");
			return null;
		}
		String OUTPUT_FILE_NAME = FilenameUtils.removeExtension(file)
				+ outputFileFormat;
		ContentHandler handler = new ToXMLContentHandler();
		// ContentHandler handler = new BodyContentHandler();
		// ContentHandler handler = new BodyContentHandler(
		// new ToXMLContentHandler());
		InputStream stream = new FileInputStream(file);
		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		try {
			parser.parse(stream, handler, metadata);
			FileWriter htmlFileWriter = new FileWriter(OUTPUT_FILE_NAME);
			BufferedWriter bw = new BufferedWriter(htmlFileWriter);
			//File f = new File(OUTPUT_FILE_NAME);  
			//OutputStreamWriter os2 = new OutputStreamWriter(new FileOutputStream(f),"Unicode");  

			bw.write(handler.toString());
			bw.flush();
			bw.close();
			return new File(OUTPUT_FILE_NAME);
		} finally {
			stream.close();
		}
	}
	
	public JSONObject loadGateAndAnnie(File file, String fileName) throws GateException,
			IOException {
		//Out.prln("Initialising basic system...");
		//Gate.init();
		//Out.prln("...basic system initialised");

		// initialise ANNIE (this may take several minutes)
		//Annie annie = new Annie();
		//annie.initAnnie();
		// create a GATE corpus and add a document for each command-line
		// argument
		Corpus corpus = Factory.newCorpus("Annie corpus");
		URL u = file.toURI().toURL();
		FeatureMap params = Factory.newFeatureMap();
		params.put("sourceUrl", u);
		params.put("preserveOriginalContent", new Boolean(true));
		params.put("collectRepositioningInfo", new Boolean(true));
		Out.prln("Creating doc for " + u);
		Document jd = (Document) Factory.createResource(
				"gate.corpora.DocumentImpl", params);
		corpus.add(jd);
		// tell the pipeline about the corpus and run it
		annie.setCorpus(corpus);
		annie.execute();
		

		Iterator iter = corpus.iterator();
		JSONObject parsedJSON = new JSONObject();
		Out.prln("Started parsing...");
		// while (iter.hasNext()) {
		if (iter.hasNext()) { // should technically be while but I am just
								// dealing with one document
			JSONObject profileJSON = new JSONObject();
			Document doc = (Document) iter.next();
			AnnotationSet defaultAnnotSet = doc.getAnnotations();

			AnnotationSet curAnnSet;
			Iterator it;
			Annotation currAnnot;
		
			String htmlFileName = fileName.substring(0, fileName.indexOf(".")) + ".html";
			String content = IO.readFileContent(htmlFileName);
			//jobTitle
			String jobTitle = extractJobTitle(content);
			if (jobTitle.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("jobTitle", jobTitle);
				parsedJSON.put("jobTitle", tempJson);
			}
			//degree requirement
			String degree = extractDegreeRequirement(content);
			if (degree.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("degree", degree);
				parsedJSON.put("degree", tempJson);
			}
			//travel requirement
			String travel = extractTravelRequirement(content);
			if (travel.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("travel", travel);
				parsedJSON.put("travel", tempJson);
			}
			
			//employmentType
			String employmentType = extractEmploymentType(content);
			if (employmentType.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("employmentType", employmentType);
				parsedJSON.put("employmentType", tempJson);
			}
			//JobLocation
			String jobLocation = extractJobLocation(content);
			if (jobLocation.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("jobLocation", jobLocation);
				parsedJSON.put("jobLocation", tempJson);
			}
			//salary
			ArrayList<String> salaryList = extractSalary(content);
			System.out.println("salaryList size:" + salaryList.size());
			if (salaryList.size() > 0){
				JSONArray salaries = new JSONArray();
				JSONObject tempJson = new JSONObject();
				tempJson.put("salary_min", salaryList.get(0));
				salaries.add(tempJson);
				if (salaryList.size() > 1){
					JSONObject tempJson2 = new JSONObject();
					tempJson2.put("salary_max", salaryList.get(1));
					salaries.add(tempJson2);
				}
				parsedJSON.put("salary", salaries);
			}
			//benefits
			String benefits = extractBenefits(content);
			System.out.println(jobTitle + "\t" + benefits);
			if (benefits.length() > 0){
				JSONObject tempJson = new JSONObject();
				tempJson.put("benefits", benefits);
				parsedJSON.put("benefits", tempJson);
			}
			// job category,location,schedule,shift
			String[] annSections = new String[] { "Category",
					"Location", "schedule", "shift" };
			String[] annKeys = new String[] { "job category", "location", "schedule",
					"shift" };
			for (short i = 0; i < annSections.length; i++) {
				JSONObject tempJson = new JSONObject();
				String annSection = annSections[i];
				curAnnSet = defaultAnnotSet.get(annSection);
				it = curAnnSet.iterator();
				JSONArray sectionArray = new JSONArray();
				while (it.hasNext()) { // extract all values for each
										// address,email,phone etc..
					currAnnot = (Annotation) it.next();
					String s = stringFor(doc, currAnnot);
					if (s != null && s.length() > 0) {
						sectionArray.add(s);
					}
				}
				if (sectionArray.size() > 0) {
					tempJson.put(annKeys[i], sectionArray);
				}
				if (!tempJson.isEmpty()) {
					parsedJSON.put(annSection, tempJson);
				}
					
			}
			
			// summary
			String[] list = new String[] { "summary"};
			for (String s : list) {
				curAnnSet = defaultAnnotSet.get(s);
				it = curAnnSet.iterator();
				JSONArray subSections = new JSONArray();
				while (it.hasNext()) {
					JSONObject subSection = new JSONObject();
					currAnnot = (Annotation) it.next();
					String key = (String) currAnnot.getFeatures().get(
							"sectionHeading");
					String value = stringFor(doc, currAnnot);
					if (!StringUtils.isBlank(key)
							&& !StringUtils.isBlank(value)) {
						subSection.put(key, value);
					}
					if (!subSection.isEmpty()) {
						subSections.add(subSection);
					}
				}
				if (!subSections.isEmpty()) {
					parsedJSON.put(s, subSections);
				}
			}
			// skills
			String[] skills = new String[] { "skills"};
			for (String skill : skills) {
				curAnnSet = defaultAnnotSet.get(skill);
				it = curAnnSet.iterator();
				JSONArray subSections = new JSONArray();
				while (it.hasNext()) {
					JSONObject subSection = new JSONObject();
					currAnnot = (Annotation) it.next();
					String key = (String) currAnnot.getFeatures().get(
							"sectionHeading");
					String value = stringFor(doc, currAnnot);
					if (!StringUtils.isBlank(key)
							&& !StringUtils.isBlank(value)) {
						subSection.put(key, value);
					}
					if (!subSection.isEmpty()) {
						subSections.add(subSection);
					}
				}
				if (!subSections.isEmpty()) {
					parsedJSON.put(skill, subSections);
				}
			}

			// work_experience
			curAnnSet = defaultAnnotSet.get("work_experience");
			it = curAnnSet.iterator();
			JSONArray workExperiences = new JSONArray();
			while (it.hasNext()) {
				JSONObject workExperience = new JSONObject();
				currAnnot = (Annotation) it.next();
				String key = (String) currAnnot.getFeatures().get(
						"sectionHeading");
				System.out.println(key);
				if (key.equals("work_experience_marker")) {
					// JSONObject details = new JSONObject();
					String[] annotations = new String[] { "date_start",
							"date_end", "jobtitle", "organization" };
					for (String annotation : annotations) {
						String v = (String) currAnnot.getFeatures().get(
								annotation);
						if (!StringUtils.isBlank(v)) {
							// details.put(annotation, v);
							workExperience.put(annotation, v);
						}
					}
					// if (!details.isEmpty()) {
					// workExperience.put("work_details", details);
					// }
					key = "text";

				}
				String value = stringFor(doc, currAnnot);
				if (!StringUtils.isBlank(key) && !StringUtils.isBlank(value)) {
					workExperience.put(key, value);
				}
				if (!workExperience.isEmpty()) {
					workExperiences.add(workExperience);
				}

			}
			if (!workExperiences.isEmpty()) {
				parsedJSON.put("work_experience", workExperiences);
			}

			IO.deleteFile(htmlFileName);
		}// if
		Out.prln("Completed parsing...");
		return parsedJSON;
	}
	
	public static String extractJobTitle(String content){
		String jobTitle = "";
		int startIndex = -1;
		int endIndex = -1;
		boolean find = false;
		String keywords [] = {"Intern", "Manager", "Developer", "Engineer", "Analyst", "project manager"};
		int i = 0;
		while (find == false && i < keywords.length){
			if(content.contains(keywords[i])){
				endIndex = content.indexOf(keywords[i]) + keywords[i].length();
				find = true;
				break;
			}
			else
				i += 1;
		}
		System.out.println(endIndex);
		if(find == true){
			startIndex = content.lastIndexOf("The ", endIndex);
			if(startIndex == -1)
				startIndex = content.lastIndexOf(" an ", endIndex);
			if(startIndex == -1)
				startIndex = content.lastIndexOf(" a ", endIndex);
			if(startIndex == -1 && endIndex <4000)
				startIndex = 0;
			System.out.println(startIndex);
		
			if(startIndex != -1)
				jobTitle = content.substring(startIndex, endIndex);
		}
		return jobTitle;
	}
	
	public static String extractDegreeRequirement(String content){
		String degree = "";
		int endIndex = -1;
		boolean find = false;
		content = content.toLowerCase().replace("m.s.", "master").replace("b.s.", "bachelor").replace(".", "");
		String keywords [] = {"phd", "master", "bachelor"};
		int i = 0;
		while (find == false && i < keywords.length){
			if(content.contains(keywords[i])){
				degree = keywords[i];
				find = true;
				break;
			}
			else
				i += 1;
		}
		System.out.println(endIndex);
		if(find == false){
			if(content.contains("degree"))
				degree = "bachelor";
		}
		return degree;
	}
	
	public static String extractTravelRequirement(String content){
		String travelRequirement = "";
		boolean find = false;
		content = content.toLowerCase();
		String keywords [] = {"phd", "master", "bachelor"};
		int i = 0;

		if(content.contains("travel")){
			find = true;
			travelRequirement = "travelling required";
			int startIndex = content.indexOf("travel");
			int endIndex = content.indexOf("international", startIndex);
			if(endIndex != -1)
				travelRequirement = "international travelling required";
		}
		return travelRequirement;
	}
	
	private static String extractEmploymentType(String content){
		String employmentType = "";
		boolean find = false;
		content = content.replace("-", "").toLowerCase();
		String keywords [] = {"fulltime", "parttime"};
		int i = 0;
		while (find == false && i < keywords.length){
			if(content.contains(keywords[i])){
				employmentType = keywords[i];
				find = true;
				break;
			}
			else
				i += 1;
		}
		return employmentType;
	}
	
	private static String extractJobLocation(String content){
		String location = "";
		boolean find = false;
		content = content.toLowerCase();
		String keywords [] = {"location"};
		int i = 0;
		while (find == false && i < keywords.length){
			if(content.contains(keywords[i])){
				int startIndex = content.indexOf(keywords[i]) + keywords[i].length() + 1;
				int endIndex = content.indexOf(" ", startIndex);
				if (endIndex == -1)
					location = content.substring(startIndex);
				else
					location = content.substring(startIndex, endIndex);
				find = true;
				break;
			}
			else
				i += 1;
		}
		return location.replace(":","").trim();
	}
	
	private static ArrayList<String> extractSalary(String content){
		ArrayList<String> salaryList = new ArrayList<String>();
		content = content.toLowerCase();
		if(content.contains("salary")){
			int dollarIndex = content.indexOf("$") ;
			while(dollarIndex != -1){
				int endIndex = content.indexOf(" ", dollarIndex);
				if (endIndex != -1){
					String salaryContent = content.substring(dollarIndex + 1, endIndex);
					salaryList.add(salaryContent);
					System.out.println(salaryContent);
				}
				dollarIndex = content.indexOf("$", endIndex);
			}
		}
		return salaryList;
	}
	
	private static String extractBenefits(String content){
		String benefits = "";
		boolean find = false;
		content = content.replace(".", "").toLowerCase();
		String keywords [] = {"benifits", "bonus"};
		int i = 0;
		while (find == false && i < keywords.length){
			if(content.contains(keywords[i])){
				int startIndex = content.indexOf("Benefits") + 9;
				benefits = content.substring(startIndex);
				find = true;
				break;
			}
			else
				i += 1;
		}
		return benefits;
	}
}
