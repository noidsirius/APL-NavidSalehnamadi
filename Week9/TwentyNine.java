import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

class PairStrInt implements Comparable<PairStrInt>{
	public String key;
	public int value;
	public PairStrInt(){
		key = "";
		value = 0;
	}
	public PairStrInt(String key, int value){
		this.key = key;
		this.value = value;
	}
	@Override
	public int compareTo(PairStrInt o) {
		return o.value - this.value;
	}
}


class DataSpace {
	public int numberOfThreads = 5;
	public ConcurrentLinkedQueue<String> words;
	public ArrayList<HashMap<String, Integer>> allFrequencyWords;
	public ConcurrentHashMap<String, Integer> frequencyWords;
	private static DataSpace instance;
	private DataSpace(){
		words = new ConcurrentLinkedQueue<>();
		allFrequencyWords = new ArrayList<>();
		frequencyWords = new ConcurrentHashMap<>();
	}
	public static DataSpace getInstance(){
		if(instance == null)
			instance = new DataSpace();
		return instance;
		
	}
}

class WordGenerator{
	private String filePath;
	public WordGenerator(String filePath){
		this.filePath = filePath;
	}
	public void generate(){
		String wholeText;
		String stopWordsText = "";
		try {
			wholeText = new String(Files.readAllBytes(Paths.get(filePath)));
			wholeText = wholeText.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
			stopWordsText = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
			TreeSet<String> stopWords = new TreeSet<>();
			stopWords.addAll(Arrays.asList(stopWordsText.split(",")));
			for(String word: wholeText.split(" ")){
				if(!stopWords.contains(word) && word.length() > 1)
					DataSpace.getInstance().words.add(word);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


class WordProcessor extends Thread {
	public WordProcessor(){
	}
	@Override
	public void run(){
//		System.out.println("Start Thread");
		HashMap<String, Integer> frequencyWords = new HashMap<>();
		while(true){
			String word = DataSpace.getInstance().words.poll();
			if(word == null)
				break;
			frequencyWords.put(word, 1+frequencyWords.getOrDefault(word, 0));
		}
		synchronized (DataSpace.getInstance().allFrequencyWords) {
//			System.out.println("Add Words");
			DataSpace.getInstance().allFrequencyWords.add(frequencyWords);
		}
		
		
	}
}
class FrequencyFinder extends Thread {
	private Pattern pattern;
	public FrequencyFinder(Pattern pattern){
		this.pattern = pattern;
	}
	@Override
	public void run(){
		for(int i=0; i< DataSpace.getInstance().numberOfThreads; i++){
			if(DataSpace.getInstance().allFrequencyWords.size() <= i){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				i--;
				continue;
			}
			HashMap<String, Integer> tmpFrequenctWords = null;
			tmpFrequenctWords = (HashMap<String, Integer>) DataSpace.getInstance().allFrequencyWords.get(i).clone();
			for(String word: tmpFrequenctWords.keySet())
				if(this.pattern.matcher(word).find()){
					synchronized (DataSpace.getInstance().frequencyWords) {
						DataSpace.getInstance().frequencyWords.put(word, tmpFrequenctWords.get(word)+DataSpace.getInstance().frequencyWords.getOrDefault(word, 0));
					}
					
				}
		}
	}
}


public class TwentyNine {

	public static void main(String[] args) throws Exception {
		WordGenerator wg = new WordGenerator(args[0]);
		wg.generate();
		ArrayList<Thread> workers = new ArrayList<>();
		for(int i=0; i< DataSpace.getInstance().numberOfThreads; i++){
			workers.add(new WordProcessor());
		}
		workers.add(new FrequencyFinder(Pattern.compile("^[a-e][^a-zA-Z0-9]*")));
		workers.add(new FrequencyFinder(Pattern.compile("^[f-j][^a-zA-Z0-9]*")));
		workers.add(new FrequencyFinder(Pattern.compile("^[k-o][^a-zA-Z0-9]*")));
		workers.add(new FrequencyFinder(Pattern.compile("^[p-t][^a-zA-Z0-9]*")));
		workers.add(new FrequencyFinder(Pattern.compile("^[u-z][^a-zA-Z0-9]*")));
		for(int i=0; i<workers.size(); i++){
			workers.get(i).start();
		}
		for(int i=0; i<workers.size(); i++){
			workers.get(i).join();
		}
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		for(String word : DataSpace.getInstance().frequencyWords.keySet()){
			term_frequencies.add(new PairStrInt(word, DataSpace.getInstance().frequencyWords.get(word)));
		}
		Collections.sort(term_frequencies);
		for(int i=0; i<25; i++)
			System.out.println(term_frequencies.get(i).key + "  -   "  + term_frequencies.get(i).value);
	}

}