
public class PairStrInt implements Comparable<PairStrInt>{
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