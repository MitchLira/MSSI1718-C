package storage;

import java.io.Serializable;

public class Pair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String state;
	private String action;
	
	public Pair(String state, String action) {
		this.state = state;
		this.action = action;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Pair)) {
			return false;
		}
		Pair rhs = (Pair) obj;
		return (this.state.equals(rhs.state) && this.action.equals(rhs.action));
	}
}
