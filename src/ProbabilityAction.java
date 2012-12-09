
public class ProbabilityAction extends AbstractAction {

	Action action = Action.JUMP_PROBABILITY;
	
	ProbabilityChange probabilityChange; 
	
	public ProbabilityAction(ProbabilityChange aChange) {
		probabilityChange = aChange;
	}
	
	@Override
	public int compareTo(AbstractAction arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
