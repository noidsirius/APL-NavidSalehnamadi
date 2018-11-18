import java.io.IOException;
import java.util.Properties;

public class Nineteen {
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		
		Properties prop = new Properties();
		prop.load(new Nineteen().getClass().getClassLoader().getResourceAsStream("config.properties"));
		String EWClass = (String)prop.get("EWClass");
		String FFClass = (String)prop.get("FFClass");
		String PFClass = (String)prop.get("PFClass");
		Class<?> ewClass = Class.forName(EWClass);
		ExtractWord ew = (ExtractWord) ewClass.newInstance();
		Class<?> ffClass = Class.forName(FFClass);
		FindFrequencies ff = (FindFrequencies) ffClass.newInstance();
		Class<?> pfClass = Class.forName(PFClass);
		PrintFrequencies pf = (PrintFrequencies) pfClass.newInstance();
		pf.printFrequencies(ff.findAndSortFrequencies(ew.extractWords(args[0])));

	}

}
