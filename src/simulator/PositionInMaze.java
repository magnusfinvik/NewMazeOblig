package simulator;

import java.awt.*;
import java.io.Serializable;

public class PositionInMaze implements Serializable{
	private int xpos;
	private int ypos;
	Color color;
	public PositionInMaze(int xp, int yp, Color color) {
		xpos = xp;
		ypos = yp;
		this.color = color;
	}

	public int getXpos() {
		return xpos;
	}

	public int getYpos() {
		return ypos;
	}
	
	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "xpos: " + xpos + "\typos: " + ypos;
	}
}
