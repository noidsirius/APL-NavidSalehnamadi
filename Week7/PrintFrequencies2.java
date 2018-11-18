import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class PrintFrequencies2 implements PrintFrequencies {

	@Override
	public void printFrequencies(ArrayList<PairStrInt> term_frequencies) throws IOException {
		OutputStream os = System.out;
		for(PairStrInt psi : term_frequencies.subList(0, 25)){
			String line = psi.key.toString() + "  -   "  + psi.value + "\n";
			os.write(line.getBytes(), 0, line.getBytes().length);
		}			
	}

}
