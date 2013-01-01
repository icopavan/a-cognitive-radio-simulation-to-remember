
public class PowerAction extends AbstractAction {

	double newPower;
	
	public PowerAction(double aPower) {
		newPower = aPower;
	}

	@Override
	public String toString() {
		return "PowerAction [newPower=" + newPower + "]";
	}

}	
