package clue;

import java.awt.Color;
import java.lang.reflect.Field;
import java.util.HashSet;

public abstract class Player {
	private String name;
	private Color color;
	private int index;
	private HashSet<Card> myCards;

	public Player(String name, String color, int index) {
		this.name = name;
		this.index = index;

		try {
		    Field field = Class.forName("java.awt.Color").getField(color);
		    this.color = (Color)field.get(null);
		} catch (Exception e) {
		    this.color = null;  // Not defined
		}
	}

	public Card disproveSuggestion(String person, String room, String weapon) {
		return null;
	}

	// Automatically generated by Eclipse.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Player other = (Player) obj;
		if (color == null) {
			if (other.color != null) {
				return false;
			}
		} else if (!color.equals(other.color)) {
			return false;
		}
		if (index != other.index) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Player [name=" + name + ", color=" + color + ", index=" + index
				+ "]";
	}
}
