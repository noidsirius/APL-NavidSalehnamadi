import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

public class ExtractWord1 implements ExtractWord {
	public String[] extractWords(String filePath) throws IOException{
		File file = new File(filePath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String wholeText = "";
		while ((line = br.readLine()) != null)
		    wholeText += line + ' ';
		br.close();
		wholeText = wholeText.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
		file = new File("../stop_words.txt");
		br = new BufferedReader(new FileReader(file));
		TreeSet<String> stopWords = new TreeSet<>();
		while ((line = br.readLine()) != null) {
			String words[] = line.split(",");
			for(String word : words)
				stopWords.add(word);
		}
		br.close();
		String newText = "";
		for(String word : wholeText.split(" ")){
			if(!stopWords.contains(word) && word.length() > 1)
				newText += word + ' ';
		}
		return newText.split(" ");
	}
	
}
