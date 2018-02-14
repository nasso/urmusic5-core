package io.github.nasso.urmusic.model.project.control;

public class OptionParam extends EffectParam<Integer> {
	private String[] options;
	
	private Integer value = 0;
	
	public OptionParam(String name, String... values) {
		super(name);
		
		this.options = new String[values.length];
		for(int i = 0; i < values.length; i++)
			this.options[i] = values[i];
	}
	
	public int getOptionCount() {
		return this.options.length;
	}
	
	public String getOptionName(int i) {
		return this.options[i];
	}
	
	protected void setStaticValue(Integer val) {
		this.value = val;
	}
	
	protected Integer getStaticValue() {
		return this.value;
	}
	
	protected Integer cloneValue(Integer val) {
		return val;
	}
	
	public Integer ramp(Integer s, Integer e, float t) {
		return t < 1.0f ? s : e;
	}
}
