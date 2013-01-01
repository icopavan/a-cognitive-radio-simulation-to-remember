
public class Transmission {
	
	public double transmissionPower;
	
	public Transmission(double aTransmissionPower) {
		transmissionPower = aTransmissionPower;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
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
		Transmission other = (Transmission) obj;
		if (Double.doubleToLongBits(transmissionPower) != Double
				.doubleToLongBits(other.transmissionPower))
			return false;
		return true;
	}
	

}
