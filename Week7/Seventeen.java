import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class Seventeen {

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
	static class Data {
		private static Data instance = null;
		public String whole_text;
		public String filePath;
		public ArrayList<PairStrInt> term_frequencies;
		private Data(){
			this.term_frequencies = new ArrayList<>();
			this.whole_text = "";
		}
		public static Data getInstance(){
			if(instance == null)
				instance = new Data();
			return instance;
		}
	}
	
	public void getWholeText(Data data) throws IOException{
		File file = new File(data.filePath);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
		    data.whole_text += line + ' ';
		    }
		br.close();
	}
	public void refineText(Data data){
		data.whole_text = data.whole_text.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
	}
	
	public void removeStopWords(Data data) throws IOException{
		File file = new File("../stop_words.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		TreeSet<String> stopWords = new TreeSet<>();
		while ((line = br.readLine()) != null) {
			String words[] = line.split(",");
			for(String word : words)
				stopWords.add(word);
		}
		br.close();
		String newText = "";
		for(String word : data.whole_text.split(" ")){
			if(!stopWords.contains(word) && word.length() > 1)
				newText += word + ' ';
		}
		data.whole_text = newText;
	}
	public void findAndSortFrequencies(Data data){
		HashMap<String, Integer> frequencyWords = new HashMap<>();
		for(String word : data.whole_text.split(" ")){
			if(frequencyWords.containsKey(word))
				frequencyWords.put(word, frequencyWords.get(word)+1);
			else
				frequencyWords.put(word, 1);
		}
		for(String word : frequencyWords.keySet()){
			data.term_frequencies.add(new PairStrInt(word, frequencyWords.get(word)));
		}
		Collections.sort(data.term_frequencies);
	}
	public void print_frequencies(Data data){
		for(int i=0; i<25; i++)
			System.out.println(data.term_frequencies.get(i).key + "  -   "  + data.term_frequencies.get(i).value);
	}

	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Data.getInstance().filePath = args[0];
		Class<?> cls = Class.forName("Seventeen");
		Object object = cls.newInstance();
		Class[] cArgs = new Class[1];
		cArgs[0] = Data.class;
		Method m = cls.getMethod("getWholeText", cArgs);
		m.invoke(object, Data.getInstance());
		m = cls.getMethod("refineText", cArgs);
		m.invoke(object, Data.getInstance());
		m = cls.getMethod("removeStopWords", cArgs);
		m.invoke(object, Data.getInstance());
		m = cls.getMethod("findAndSortFrequencies", cArgs);
		m.invoke(object, Data.getInstance());
		m = cls.getMethod("print_frequencies", cArgs);
		m.invoke(object, Data.getInstance());
		
	}

}
