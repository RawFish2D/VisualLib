package ua.rawfish2d.visuallib.utils;

public class DoubleValue {
	private double value;
	private final double min;
	private final double max;

	public DoubleValue(double val, double min, double max) {
		this.value = val;
		this.min = min;
		this.max = max;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double newVal) {
		value = MiscUtils.clamp(newVal, min, max);
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}
