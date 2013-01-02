
public class StateAction {

	public State state;
	public TransmissionAction transmissionAction;

	public StateAction(State aState, TransmissionAction anAction) {
		this.state = aState;
		this.transmissionAction = anAction;
	}
	
	@Override
	public String toString() {
		return "StateAction [state=" + state + ", transmissionAction="
				+ transmissionAction + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime
				* result
				+ ((transmissionAction == null) ? 0 : transmissionAction
						.hashCode());
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
		StateAction other = (StateAction) obj;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (transmissionAction == null) {
			if (other.transmissionAction != null)
				return false;
		} else if (!transmissionAction.equals(other.transmissionAction))
			return false;
		return true;
	}
	
}
