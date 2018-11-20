import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

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

class Generator extends Thread{
	
	private Object mutex;
	private Object value;
	private boolean done = false; 
	public Generator(){
		this.mutex = new Object();
		value = null;
	}
	public Object getMutex(){
		return mutex;
	}
	public Object getNextValue(){
		synchronized (mutex) {
			try{
				mutex.notify();
				mutex.wait();
				if(isDone())
					return null;
				return value;
			}
			catch(Exception e){
				
			}
		}
		return null;
//		return value;
	}
	public boolean isDone(){
		return this.done;
	}
	public void genYield(Object o) throws InterruptedException{
		synchronized (this.mutex) {
			value = o;
			this.mutex.notify();
			this.mutex.wait();	
		}
	}
	public void genTerminate() throws InterruptedException{
		synchronized (this.mutex) {
				done = true;
				this.mutex.notify();
		}
	}
	@Override
	public void run(){
		synchronized (this.mutex) {
			try{
				mutex.wait();
				for(int i=0; i<5; i++){
					genYield(i);
				}
				genTerminate();
			}
			catch(Exception e){
				System.out.println("Error " + e);
			}
		}		
	}
}

class ActiveWFGenerator extends Generator {
	protected void init(Object[] message){
		
	}
	public Object dispatch(Object[] message) throws Exception{
		String command = (String) message[0];
		switch(command){
		case "init":
			init(message);
			break;
		case "next":
			return getNextValue();
		}			
		return null;
	}
}


class WordGenerator extends ActiveWFGenerator {
	private String wholeText;
	protected void init(Object[] message){
		String filePath = (String) message[1];
		try {
			wholeText = new String(Files.readAllBytes(Paths.get(filePath)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		wholeText = wholeText.substring(0, 100);
		wholeText = wholeText.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
	}
	@Override
	public void run(){
		try
		{
			synchronized (this.getMutex()) {
				this.getMutex().wait();
				for(String word: wholeText.split(" ")){
					genYield(word);
				}
				genTerminate();
	    	}
		}
		catch (Exception e){
          System.out.println (this.getName() + " Exception is caught");
          System.out.println(e);
          System.out.println(e.getStackTrace().toString());
		}
	}
}

class NonStopWordGenerator extends ActiveWFGenerator {
	private TreeSet<String> stopWords;
	private WordGenerator wordGenerator;
	protected void init(Object[] message) {
		String filePath = (String) message[1];
		wordGenerator = new WordGenerator();
		
		try {
			wordGenerator.start();
			Thread.sleep(100);
			wordGenerator.dispatch(new Object[]{"init",filePath});
		} catch (Exception e) {
			e.printStackTrace();
		}
		String stopWordsText="";
		try {
			stopWordsText = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		stopWords = new TreeSet<>();
		stopWords.addAll(Arrays.asList(stopWordsText.split(",")));
	}
	@Override
	public void run(){
		try
		{
			synchronized (this.getMutex()) {
				this.getMutex().wait();
				while(true){
					String word = (String) wordGenerator.dispatch(new Object[]{"next"});
					if(word == null)
						break;
					if(!stopWords.contains(word) && word.length() > 1)
						genYield(word);
				}
				genTerminate();
	    	}
		}
		catch (Exception e){
          System.out.println (this.getName() + " Exception is caught");
          System.out.println(e);
          System.out.println(e.getStackTrace().toString());
		}
	}
	

}

class WordFrequencyGenerator extends ActiveWFGenerator {
	private HashMap<String, Integer> frequencyWords;
	private NonStopWordGenerator nswGenerator;
	protected void init(Object[] message) {
		String filePath = (String) message[1];
		nswGenerator = new NonStopWordGenerator();
		
		try {
			nswGenerator.start();
			Thread.sleep(100);
			nswGenerator.dispatch(new Object[]{"init",filePath});
		} catch (Exception e) {
			e.printStackTrace();
		}
		frequencyWords = new HashMap<>();
	}
	private Object sortedTermFrequencies(){
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		for(String word : frequencyWords.keySet()){
			term_frequencies.add(new PairStrInt(word, frequencyWords.get(word)));
		}
		Collections.sort(term_frequencies);
		return term_frequencies;
	}
	
	@Override
	public void run(){
		try
		{
			synchronized (this.getMutex()) {
				this.getMutex().wait();
				int c = 0;
				while(true){
					String word = (String) nswGenerator.dispatch(new Object[]{"next"});
					if(word == null)
						break;
					if(frequencyWords.containsKey(word))
						frequencyWords.put(word, frequencyWords.get(word)+1);
					else
						frequencyWords.put(word, 1);
					c += 1;
					if(c % 10000 == 0){
//						System.out.println(Thread.activeCount());
						genYield(sortedTermFrequencies());
					}
				}
				genYield(sortedTermFrequencies());
				genTerminate();
	    	}
		}
		catch (Exception e){
          System.out.println (this.getName() + " Exception is caught");
          System.out.println(e);
          System.out.println(e.getStackTrace().toString());
		}
	}
	
}



public class TwentyEightThree {

	
	public static void main(String[] args) throws Exception {
		
		
		WordFrequencyGenerator wfGenerator = new WordFrequencyGenerator();
		wfGenerator.start();
		Thread.sleep(100);
		wfGenerator.dispatch(new Object[]{"init",args[0]});
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		while(true){
			Object ret = wfGenerator.dispatch(new Object[]{"next"});
			if(ret == null)
				break;
			term_frequencies = (ArrayList<PairStrInt>) ret;
		}
		for(int i=0; i<25; i++)
			System.out.println(term_frequencies.get(i).key + "  -   "  + term_frequencies.get(i).value);
	}

}
