package ua.rawfish2d.visuallib.utils;

public class BoolValue {
	private boolean value;

	public BoolValue(boolean val) {
		this.value = val;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean newVal) {
		value = newVal;
	}

	public boolean toggle() {
		value = !value;
		return value;
	}
}
