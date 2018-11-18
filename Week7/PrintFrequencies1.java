import java.io.IOException;
import java.util.ArrayList;

public class PrintFrequencies1 implements PrintFrequencies {

	@Override
	public void printFrequencies(ArrayList<PairStrInt> term_frequencies) throws IOException {
		for(int i=0; i<25; i++)
			System.out.println(term_frequencies.get(i).key + "  -   "  + term_frequencies.get(i).value);
	}

}
