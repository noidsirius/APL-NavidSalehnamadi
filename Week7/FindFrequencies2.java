import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class FindFrequencies2 implements FindFrequencies {

	@Override
	public ArrayList<PairStrInt> findAndSortFrequencies(String[] words) {
		ArrayList<String> arWords = new ArrayList<>(Arrays.asList(words));
		Collections.sort(arWords);
		ArrayList<PairStrInt> term_frequencies = new ArrayList<>();
		int counter = 1;
		String lastWord = arWords.get(0);
		for(int i=1; i<arWords.size(); i++){
			if(arWords.get(i).equals(lastWord))
				counter += 1;
			else{
				term_frequencies.add(new PairStrInt(lastWord, counter));
				counter = 1;
				lastWord = arWords.get(i);
			}
		}
		term_frequencies.add(new PairStrInt(lastWord, counter));
		Collections.sort(term_frequencies);
		return term_frequencies;
	}

}
