
public class TransmissionAction {

	@Override
	public String toString() {
		return "TransmissionAction [frequency=" + frequency
				+ ", transmissionPower=" + transmissionPower + "]";
	}

	public double frequency;
	public double transmissionPower;
	
	public TransmissionAction(double aFrequency, double aTransmissionPower) {
		frequency = aFrequency;
		transmissionPower = aTransmissionPower;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(frequency);
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
		TransmissionAction other = (TransmissionAction) obj;
		if (Double.doubleToLongBits(frequency) != Double
				.doubleToLongBits(other.frequency))
			return false;
		if (Double.doubleToLongBits(transmissionPower) != Double
				.doubleToLongBits(other.transmissionPower))
			return false;
		return true;
	}
	
}
