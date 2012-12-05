
public class PowerAction extends AbstractAction {

	Action action = Action.JUMP_POWER;
	
	double newPower;
	
	public PowerAction(double aPower) {
		newPower = aPower;
	}

	@Override
	public String toString() {
		return "PowerAction [action=" + action + ", newPower=" + newPower + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = (int) (prime * result + newPower);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (this.compareTo((PowerAction) obj) == 0)
			return true;
		PowerAction other = (PowerAction) obj;
		if (action != other.action)
			return false;
		if (newPower != other.newPower)
			return false;
		return true;
	}

	@Override
	public int compareTo(AbstractAction arg0) {
		try {
			PowerAction o = (PowerAction) arg0;
			return (int) (newPower - o.newPower);
		} catch (ClassCastException e) {
			try {
				@SuppressWarnings("unused")
				SpectrumAction o = (SpectrumAction) arg0;
				return -1;
			} catch (ClassCastException ex) {
				return 1;
			}
		}
	}
	
}	
