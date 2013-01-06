import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ACRSTRUtil {

	public static final String OLD_FILES_DIRECTORY = "what-once-was";
	
	public static final String OUTPUT_FILE_NAME = "output";
	
	public static final String OUTPUT_FILE_EXTENSION = ".txt";
	
	public static BufferedWriter bufferedWriter;
	
	public static final String SETTINGS_FILE_NAME = "acrstr.conf";
	
	public static Map<String, String> settings = new TreeMap<String, String>();
	
	public static void readSettingsFile() throws FileNotFoundException {
		File settingsFile = new File(SETTINGS_FILE_NAME);
		Scanner fileScanner = new Scanner(settingsFile);
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			if (!line.startsWith("#")) {
				String[] settingAndValue = line.split("\\=");
				settings.put(settingAndValue[0], settingAndValue[1]);
			}
		}
	}
	
	public static String getSetting(String setting) {
		return settings.get(setting);
	}

	public static void initialize() {
		File logFile = new File(OUTPUT_FILE_NAME + OUTPUT_FILE_EXTENSION);
		if (logFile.exists()) {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd-HH-MM-ss");
			String dateSuffix = dateFormat.format(date);
			logFile.renameTo(new File(OLD_FILES_DIRECTORY + "/" + OUTPUT_FILE_NAME + "-" + dateSuffix + OUTPUT_FILE_EXTENSION));
		}
	}
	
	public static void log(Object logLine) {
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME + OUTPUT_FILE_EXTENSION, true));
			bufferedWriter.write(logLine + "\n");
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void createOutputInAppendMode(String fileName, String line)
			throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true));
		bw.write(line + "\n");
		bw.close();
	}
	


}
