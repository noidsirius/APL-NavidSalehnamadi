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

class ActiveWFObject extends Thread{
	protected boolean done = false;
	public ArrayList<Object[]> queue;
	public ActiveWFObject(){
		queue = new ArrayList<>();
	}
	@Override
	public void run() 
    { 
        try
        { 
        	synchronized (this) {
	        	wait();
	        	while(done == false){
		    		if(queue.size() > 0){		    			
		    			Object[] message = queue.get(0);
		    			queue.remove(0);
		    			this.dispatch(message);
		    		}
		    		else{
		    			wait();
		    		}
	    		}
        	}
        	
        } 
        catch (Exception e) 
        { 
            // Throwing an exception 
            System.out.println (this.getName() + " Exception is caught");
            System.out.println(e);
            System.out.println(e.getStackTrace().toString());
        } 
    } 
	protected void dispatch(Object[] message) throws Exception{
		String command = (String) message[0];
//		System.out.println(this.getName() + " Dispatch " + command);
		if(command.equals("die"))
			done = true;
//		throw new Exception("Not implemented");
	}
	public void send(Object[] message){
		synchronized (this) {
//			System.out.println("send: " + this.getClass() + " " + (String) message[0] + " " + message.length);
			queue.add(message);
			this.notify();			
		}
//		System.out.println(this.isAlive() + " " + queue.size() + " send: " + this.getClass() + " " + (String) message[0] + " " + message.length);
	}
}

class DataStorage extends ActiveWFObject {
	private String wholeText;
	private StopWordManager stopWord;
	private void init(Object[] message) throws IOException{
		String filePath = (String) message[1];
		stopWord = (StopWordManager) message[2];
		wholeText = new String(Files.readAllBytes(Paths.get(filePath)));
//		wholeText = wholeText.substring(0, 100);
		wholeText = wholeText.replaceAll("[^a-zA-Z0-9]", " ").toLowerCase();
	}
	private void wordGenerator() throws Exception{
		for(String word: wholeText.split(" ")){
			this.stopWord.send(new Object[]{"processNonStopWord",word});
		}
		this.stopWord.send(new Object[]{"print"});
//		this.stopWord.send(new Object[]{"die"});
//		System.out.println((this.stopWord.queue.get(this.stopWord.queue.size()-1)));
		this.send(new Object[]{"die"});
	}
	public void dispatch(Object[] message) throws Exception{
		super.dispatch(message);
		String command = (String) message[0];
		switch(command){
		case "init":
			init(message);
			break;
		case "wordGenerator":
			wordGenerator();
			break;
		}			
	}
}

class StopWordManager extends ActiveWFObject {
	private TreeSet<String> stopWords;
	private WordFrequencyManager wordFrequency;
	private void init(Object[] message) throws IOException{
		wordFrequency = (WordFrequencyManager) message[1];
		String stopWordsText = new String(Files.readAllBytes(Paths.get("../stop_words.txt")));;
		stopWords = new TreeSet<>();
		stopWords.addAll(Arrays.asList(stopWordsText.split(",")));
	}
	private void processNonStopWord(Object[] message) throws Exception{
//		System.out.println("S");
		String word = (String) message[1];
		if(!stopWords.contains(word) && word.length() > 1)
			wordFrequency.send(new Object[]{"increment",word});
	}
	public void dispatch(Object[] message) throws Exception{
		super.dispatch(message);
		String command = (String) message[0];
//		System.out.println("Begin " + command + " " + queue.size());
		switch(command){
		case "init":
			init(message);
			break;
		case "processNonStopWord":
			processNonStopWord(message);
			break;
		case "print":
			wordFrequency.send(message);
			this.send(new Object[]{"die"});
			break;
//		default:
//			wordFrequency.send(message);
		}			
//		System.out.println("END " + command + " " + queue.size());
	}
}

class WordFrequencyManager extends ActiveWFObject {
	private HashMap<String, Integer> frequencyWords;

	private void init(){
		frequencyWords = new HashMap<>();
	}
	private void increment(Object[] message){
		String word = (String) message[1];
		if(frequencyWords.containsKey(word))
			frequencyWords.put(word, frequencyWords.get(word)+1);
		else
			frequencyWords.put(word, 1);
	}
	private void printTop25(){
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		for(String word : frequencyWords.keySet()){
			term_frequencies.add(new PairStrInt(word, frequencyWords.get(word)));
		}
		Collections.sort(term_frequencies);
		for(int i=0; i<25; i++)
			System.out.println(term_frequencies.get(i).key + "  -   "  + term_frequencies.get(i).value);
		this.send(new Object[]{"die"});
	}
	public void dispatch(Object[] message) throws Exception {
		super.dispatch(message);
		String command = (String) message[0];
		switch(command){
		case "init":
			init();
			break;
		case "increment":
			increment(message);
			break;
		case "print":
			printTop25();
			break;
//		default:
//			throw new Exception("Undefined command " + command);
		}
		
	}
}

public class TwentyEight {

	public static void main(String[] args) throws Exception {
		WordFrequencyManager wfManager = new WordFrequencyManager();
		StopWordManager swManager = new StopWordManager();
		DataStorage dStorage = new DataStorage();
		wfManager.setName("WF");
		swManager.setName("SW");
		dStorage.setName("DS");
		dStorage.start();
		swManager.start();
		wfManager.start();
		wfManager.send(new Object[]{"init"});
		swManager.send(new Object[]{"init",wfManager});		
		dStorage.send(new Object[]{"init",args[0], swManager});
		dStorage.send(new Object[]{"wordGenerator"});
		System.out.println(Thread.activeCount());
		dStorage.join();
		swManager.join();
		wfManager.join();
		
	}

}