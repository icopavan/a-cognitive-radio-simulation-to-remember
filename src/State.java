

public class State {

	public double frequency;
	public double transmissionPower;
	public double preferredFrequency;
	
	public State(double aFrequency, double aTransmissionPower) {
		frequency = aFrequency;
		transmissionPower = aTransmissionPower;
		preferredFrequency = 0.0;
	}

	@Override
	public String toString() {
		return "State [frequency=" + frequency + ", transmissionPower="
				+ transmissionPower + ", preferredFrequency="
				+ preferredFrequency + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(frequency);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(preferredFrequency);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(transmissionPower);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (Double.doubleToLongBits(frequency) != Double
				.doubleToLongBits(other.frequency))
			return false;
		if (Double.doubleToLongBits(preferredFrequency) != Double
				.doubleToLongBits(other.preferredFrequency))
			return false;
		if (Double.doubleToLongBits(transmissionPower) != Double
				.doubleToLongBits(other.transmissionPower))
			return false;
		return true;
	}

}
