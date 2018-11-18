import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class ExtractWord2 implements ExtractWord {
	public String[] extractWords(String filePath) throws IOException{
		String wholeText =  new String(Files.readAllBytes(Paths.get(filePath)));;
		String stopWordsText = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));;
		wholeText = wholeText.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
		TreeSet<String> stopWords = new TreeSet<>();
		stopWords.addAll(Arrays.asList(stopWordsText.split(",")));
		ArrayList<String> nonStopWords = new ArrayList<>();
		for(String word : wholeText.split(" ")){
			if(!stopWords.contains(word) && word.length() > 1)
				nonStopWords.add(word);
		}
		return nonStopWords.toArray(new String[0]);
	}
	
}
