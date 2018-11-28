import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PairStringInt implements Comparable<PairStringInt>{
	public String key;
	public int value;
	public PairStringInt(){
		key = "";
		value = 0;
	}
	public PairStringInt(String key, int value){
		this.key = key;
		this.value = value;
	}
	@Override
	public int compareTo(PairStringInt o) {
		return o.value - this.value;
	}
}


public class ThirtyOne {

	public static String readFile(String filePath){
		try {
			return new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public static ArrayList<String> partition(String wholeText, int nlines){
		String[] lines = wholeText.split("\n");
		
		ArrayList<String> allLines = new ArrayList<>();
		for(int i=0; i<lines.length; i+=nlines)
			allLines.add(String.join("\n",Arrays.copyOfRange(lines, i, i+nlines)));
		return allLines;
		
	}
	
	public static ArrayList<PairStringInt> splitWords(String lines){
		String wholeText = lines.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
		String stopWordsText = "";
		try {
			stopWordsText = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		TreeSet<String> stopWords = new TreeSet<>();
		stopWords.addAll(Arrays.asList(stopWordsText.split(",")));
		ArrayList<PairStringInt> output = new ArrayList<>();
		for(String word: wholeText.split(" ")){
			if(!stopWords.contains(word) && word.length() > 1)
				output.add(new PairStringInt(word,1));
		}
		return output;
	}
	
	public static HashMap<Pattern, ArrayList<PairStringInt>> regroup(Stream stream, Pattern[] patterns){
		HashMap<Pattern, ArrayList<PairStringInt>> output = new HashMap<>();
		for(Pattern p: patterns)
			output.put(p, new ArrayList<>());
		for(Object obj : stream.toArray()){
			ArrayList<PairStringInt> arr = (ArrayList<PairStringInt>) obj;
			for(Pattern p: patterns){
				ArrayList<PairStringInt> tmp = (ArrayList<PairStringInt>) arr.parallelStream().filter(psi -> p.matcher(psi.key).find()).collect(Collectors.toCollection(ArrayList::new));
				output.get(p).addAll(tmp);
			}
		}
		return output;
		
	}
	public static ArrayList<PairStringInt> countWordsInGroups(ArrayList<PairStringInt> input){
		ArrayList<PairStringInt> output = new ArrayList<>();
		HashMap<String, Integer> frequencyWords = new HashMap<>();
		for(PairStringInt psi: input)
			frequencyWords.put(psi.key, psi.value + frequencyWords.getOrDefault(psi.key, 0));
		for(String key: frequencyWords.keySet())
			output.add(new PairStringInt(key, frequencyWords.get(key)));
		return output;
	}
	
	
	public static void main(String[] args) throws Exception {
		Stream s = partition(readFile(args[0]), 200).parallelStream().map(lines -> splitWords(lines));
		Pattern[] patterns = new Pattern[]{	Pattern.compile("^[a-e][^a-zA-Z0-9]*"),
				Pattern.compile("^[f-j][^a-zA-Z0-9]*"),
				Pattern.compile("^[k-o][^a-zA-Z0-9]*"),
				Pattern.compile("^[p-t][^a-zA-Z0-9]*"),
				Pattern.compile("^[u-z][^a-zA-Z0-9]*")};

		HashMap<Pattern, ArrayList<PairStringInt>> newGroups = regroup(s, patterns);
		Stream s2 = newGroups.keySet().parallelStream().map(p -> countWordsInGroups(newGroups.get(p)));
		List<PairStringInt> term_frequencies = new ArrayList<>(); 
		for(Object o : s2.toArray())
			term_frequencies.addAll((List<PairStringInt>) o);
		Collections.sort(term_frequencies);
		for(int i=0; i<25; i++)
			System.out.println(term_frequencies.get(i).key + "  -   "  + term_frequencies.get(i).value);
	}

}