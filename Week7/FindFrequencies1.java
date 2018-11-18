import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class FindFrequencies1 implements FindFrequencies {

	@Override
	public ArrayList<PairStrInt> findAndSortFrequencies(String[] words) {
		HashMap<String, Integer> frequencyWords = new HashMap<>();
		for(String word : words){
			if(frequencyWords.containsKey(word))
				frequencyWords.put(word, frequencyWords.get(word)+1);
			else
				frequencyWords.put(word, 1);
		}
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		for(String word : frequencyWords.keySet()){
			term_frequencies.add(new PairStrInt(word, frequencyWords.get(word)));
		}
		Collections.sort(term_frequencies);
		return term_frequencies;
	}

}
