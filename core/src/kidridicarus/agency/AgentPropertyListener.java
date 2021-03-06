package kidridicarus.agency;

/*
 * A listener that is called when a certain property is queried. Key is not given here, but was given when adding
 * the property listener to the Agent.
 * The class of value that is returned by this listener can be retrieved (using clsT) and checked before calling
 * getValue.
 */
public abstract class AgentPropertyListener<T> {
	public abstract T getValue();
	private final Class<T> clsT;
	public Class<T> getValueClass() { return clsT; }
	protected AgentPropertyListener(Class<T> clsT) { this.clsT = clsT; }
}
