package piotrrr.thesis.bots.tuning;

public class Param<E extends Comparable<E>> {
	
	private E value;
	
	private E minRationalVal;
	
	private E maxRationalVal;
	
	public Param(E val, E min, E max) {
		this.maxRationalVal = max;
		this.minRationalVal = min;
		setValue(val);
	}

	public E value() {
		return value;
	}

	public void setValue(E value) {
		if (value.compareTo(minRationalVal) < 0) this.value = minRationalVal;
		else if (value.compareTo(maxRationalVal) > 0) this.value = maxRationalVal;
		else this.value = value;
	}
	
	

}
