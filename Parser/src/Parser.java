import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import gate.util.Out;

public class Parser {


	public static void main(String[] args) throws IOException {
		ResumeParserProgram resumeParser = new ResumeParserProgram();
		JobOrderParserProgram jdParser = new JobOrderParserProgram();
		
		int inputType = Integer.parseInt(args[0]);//1: resume, 2: jd
		String listFileName = "";
		if (inputType == 1){
			listFileName = "D:/Wailian/myjobtank_export_20171011/resumes_list.txt";
		}
		if (inputType == 2){
			listFileName = "D:/Wailian/50JD_297Resume/Job_Description_50/list.txt";
		}
		ArrayList<String> fileList = IO.readFileList(listFileName);
		int length = fileList.size();
		System.out.println(length);
			
		int start = 0;
		for(int i = 19; i < 20; i++){
			String inputFileName = fileList.get(i);
			String inputFilePath = "";
			String outputFilePath = "";
			if (inputType == 1){
				inputFilePath = "D:/Wailian/myjobtank_export_20171011/resumes/";
				outputFilePath = "D:/Wailian/myjobtank_export_20171011/resumes_parsedResults/";
			}
			if (inputType == 2){
				inputFilePath = "D:/Wailian/50JD_297Resume/Job_Description_50/";
				outputFilePath = "D:/Wailian/50JD_297Resume/Job_Description_50_parsedResults/";
			}
			String outputFileName = inputFileName.substring(0, inputFileName.indexOf(".")) + ".json";
			try {
				if (inputType == 1){
					File tikkaConvertedFile = resumeParser.parseToHTMLUsingApacheTikka(inputFilePath + inputFileName);
				
					if (tikkaConvertedFile != null) {
						
						JSONObject parsedJSON = resumeParser.loadGateAndAnnie(tikkaConvertedFile, inputFilePath + inputFileName);
						
						Out.prln("Writing to output...");
						FileWriter jsonFileWriter = new FileWriter(outputFilePath + outputFileName);
						jsonFileWriter.write(parsedJSON.toJSONString());
						jsonFileWriter.flush();
						jsonFileWriter.close();
						Out.prln("Output written to file " + outputFileName);
					}
				}
				if (inputType == 2){
					File tikkaConvertedFile = jdParser.parseToHTMLUsingApacheTikka(inputFilePath + inputFileName);
				
					if (tikkaConvertedFile != null) {
						
						JSONObject parsedJSON = jdParser.loadGateAndAnnie(tikkaConvertedFile, inputFilePath + inputFileName);
						
						Out.prln("Writing to output...");
						FileWriter jsonFileWriter = new FileWriter(outputFilePath + outputFileName);
						jsonFileWriter.write(parsedJSON.toJSONString());
						jsonFileWriter.flush();
						jsonFileWriter.close();
						Out.prln("Output written to file " + outputFileName);
					}
				}
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Sad Face :( .Something went wrong.");
				e.printStackTrace();
				if (inputType == 1){
					FileWriter fw = new FileWriter("D:/Wailian/myjobtank_export_20171011/resumes_parsedResults/exception.txt", true);
					fw.write(inputFileName + "\n");
					fw.close();
				}
				if (inputType == 2){
					FileWriter fw = new FileWriter("D:/Wailian/50JD_297Resume/Job_Description_50/exception.txt", true);
					fw.write(inputFileName + "\n");
					fw.close();
				}
			}
		}
	}
}
