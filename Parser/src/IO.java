import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class IO {
	public static ArrayList<String> readFileList(String listFileName){
		ArrayList<String> fileList = new ArrayList<String>();
		try {
			BufferedReader  reader = new BufferedReader(new FileReader(listFileName));
			String tempString = reader.readLine();
			while (tempString != null) {
				fileList.add(tempString.trim());
				tempString = reader.readLine();
            }
            reader.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
		return fileList;
	}
	
	public static String readFileContent(String fileName){
		System.out.println(fileName);
		String content = "";
		try {
			InputStreamReader  reader = new InputStreamReader(new FileInputStream(fileName));
			BufferedReader bf = new BufferedReader(reader);
			String tempString = bf.readLine();
			while (tempString != null) {
				content += tempString.trim()+ "\t";
				tempString = bf.readLine();
            }
            reader.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

		//System.out.println(content.substring(0, 4000));
		//System.out.println(content.length());
		while(content.contains("<")){
			int startIndex = content.indexOf("<");
			int endIndex = content.indexOf(">", startIndex);
			content = content.substring(0, startIndex) + content.substring(endIndex + 1);
			System.out.println(content.length());
		}
		//System.out.println(content.length());
		//System.out.println(content);
		return content.trim();
	}
	
	public static boolean deleteFile(String fileName) {
	    File file = new File(fileName);
	    if (file.exists() && file.isFile()) {
	        if (file.delete()) {
	            System.out.println("remove one file" + fileName + " success!");
	            return true;
	        } else {
	            System.out.println("remove one file" + fileName + " fail!");
	            return false;
	        }
	    } else {
	        System.out.println("remove one file fail:" + fileName + " not existed!");
	        return false;
	    }
	}
}
