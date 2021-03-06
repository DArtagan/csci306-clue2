package clue;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JPanel;

import clue.RoomCell.DoorDirection;

public class Board extends JPanel {
	// Configuration constants.
	public static final char CLOSET = 'X';
	public static final char WALKWAY = WalkwayCell.WALKWAY;
	// Member variables.
	private ArrayList<BoardCell> cells;
	protected TreeMap<Character, String> rooms;
	protected int numRows;
	protected int numColumns;
	private HashSet<BoardCell> targets;
	private Map<Integer, LinkedList<Integer>> adjMap;
	private Map<Character, Point> namesMap;
	private boolean[] visited;

	public Board() {
		cells = new ArrayList<BoardCell>();
		rooms = new TreeMap<Character, String>();
		namesMap = new HashMap<Character, Point>();
	}

	public Board(String layoutName, String legendName) throws IOException, BadConfigFormatException {
		cells = new ArrayList<BoardCell>();
		rooms = new TreeMap<Character, String>();
		namesMap = new HashMap<Character, Point>();
		loadConfigFiles(layoutName, legendName);
	}

	public void loadConfigFiles(String layoutName, String legendName) throws IOException, BadConfigFormatException {
		loadLegend(legendName);
		loadLayout(layoutName);
	}

	private void loadLegend(String legendName) throws IOException, BadConfigFormatException {
		FileReader legendReader = new FileReader(legendName);
		Scanner legendIn = new Scanner(legendReader);
		String line, parts[];
		while (legendIn.hasNextLine()) {
			line = legendIn.nextLine();
			parts = line.split(", ");
			if(parts.length != 2 || parts[0] == "" || parts[1] == "") {
				legendIn.close();
				legendReader.close();
				throw new BadConfigFormatException("Legend is malformed.");
			}
			rooms.put(parts[0].charAt(0), parts[1]);
		}
		legendIn.close();
		legendReader.close();
	}

	private void loadLayout(String layoutName) throws BadConfigFormatException, IOException {
		int colCount1 = 0;
		int colCount2 = 0;
		char c;
		FileReader reader = new FileReader(layoutName);
		Scanner scan = new Scanner(reader);

		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			colCount1 = 0;
			for (int i = 0; i < line.length(); ++i) {
				c = line.charAt(i);
				if (!(c != ',' && (i == 0 || line.charAt(i-1) == ','))) {
					continue;
				}
				if (!rooms.containsKey(c)) {
					scan.close();
					reader.close();
					throw new BadConfigFormatException("Bad room initial in layout.");
				} else {
					if (c != WALKWAY) {
						if (i != line.length()-1) {
							if (line.charAt(i+1) == 'N') {
								namesMap.put(c, new Point(colCount1, numRows));
							}
							cells.add(new RoomCell(numRows, colCount1, c, line.charAt(i+1)));
						} else {
							cells.add(new RoomCell(numRows, colCount1, c));
						}
					} else {
						cells.add(new WalkwayCell(numRows, colCount1));
					}
					++colCount1;
				}
			}
			++numRows;
			if (numRows == 1) {
				// If this is the first row, store the number of columns to a temp value.
				colCount2 = colCount1;
			} else if (colCount1 != colCount2) {
				// Compare columns to temp value to ensure all rows have same number of columns.
				scan.close();
				reader.close();
				throw new BadConfigFormatException("Unexpected number of columns.");
			}
		}
		numColumns = colCount2;
		scan.close();
		reader.close();
	}

	public int calcIndex(int row, int col) {
		return row*numColumns+col;
	}

	public BoardCell getCellAt(int row, int col) {
		return getCellAt(calcIndex(row, col));
	}

	public BoardCell getCellAt(int index) {
		return cells.get(index);
	}

	public Map<Character, String> getRooms() {
		return rooms;
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void calcAdjacencies() {
		adjMap = new HashMap<Integer, LinkedList<Integer>>();

		LinkedList <Integer> adjs;
		for (int i = 0; i < numRows*numColumns; i++) {
			adjs = new LinkedList<Integer>();
			if (getCellAt(i).isDoorway()) {
				if (((RoomCell) cells.get(i)).getDoorDirection() == DoorDirection.DOWN) {
					adjs.add(i+numColumns);
				} else if (((RoomCell) cells.get(i)).getDoorDirection() == DoorDirection.LEFT) {
					adjs.add(i-1);
				} else if (((RoomCell) cells.get(i)).getDoorDirection() == DoorDirection.RIGHT) {
					adjs.add(i+1);
				} else if (((RoomCell) cells.get(i)).getDoorDirection() == DoorDirection.UP) {
					adjs.add(i-numColumns);
				}
			} else if (!getCellAt(i).isRoom()) {
				if (i%numColumns != 0) {
					// If the square isn't on the left.
					if(!cells.get(i-1).isRoom() || (cells.get(i-1).isDoorway() && ((RoomCell) cells.get(i-1)).getDoorDirection() == DoorDirection.RIGHT)) {
						// If it's a walkway or a proper facing door, add the
						// square to the left to the adjacency list.
						adjs.add(i-1);
					}
				}
				if(i/numColumns != 0) {
					// If the square isn't on the top.
					if(!cells.get(i-numColumns).isRoom() || (cells.get(i-numColumns).isDoorway() && ((RoomCell) cells.get(i-numColumns)).getDoorDirection() == DoorDirection.DOWN)) {
						// If it's a walkway or a proper facing door, add
						// the square to the top to the adjacency list.
						adjs.add(i-numColumns);
					}
				}
				if(i%numColumns != numColumns-1) {
					// If the square isn't on the right.
					if(!cells.get(i+1).isRoom() || (cells.get(i+1).isDoorway() && ((RoomCell) cells.get(i+1)).getDoorDirection() == DoorDirection.LEFT)) {
						// If it's a walkway or a proper facing door,
						// add the square to the right to the adjacency list.
						adjs.add(i+1);
					}
				}
				if(i/numColumns != numRows-1) {
					//if the square isn't on the bottom.
					if(!cells.get(i+numColumns).isRoom() || (cells.get(i+numColumns).isDoorway() && ((RoomCell) cells.get(i+numColumns)).getDoorDirection() == DoorDirection.UP)) {
						// If it's a walkway or a proper facing door,
						// add the square to the top to the adjacency list
						adjs.add(i+numColumns);
					}
				}
			}
			adjMap.put(i, adjs);
		}
	}

	public LinkedList<Integer> getAdjList(int index) {
		return adjMap.get(index);
	}

	public LinkedList<Integer> getAdjCells(int index) {
		LinkedList<Integer> adj = new LinkedList<Integer>();
		if((index - numColumns) >= 0) {
			adj.add(index - numColumns);
		}
		if((index + numColumns) < (numRows*numColumns)) {
			adj.add(index + numColumns);
		}
		if((index % numColumns) > 0) {
			adj.add(index - 1);
		}
		if(((index + 1) % numColumns) > 0) {
			adj.add(index + 1);
		}
		return adj;
	}

	public void calcTargets(int row, int col, int steps) {
		targets = new HashSet<BoardCell>();
		visited = new boolean[numRows*numColumns];
		Arrays.fill(visited, false);
		int index = calcIndex(row,col);
		calcTargetsHelper(index, steps);
	}

	private void calcTargetsHelper(int index, int steps) {
		visited[index] = true;
		LinkedList<Integer> adjacencies = getAdjList(index);

		for (int i : adjacencies) {
			if(visited[i] == false) {
				if(getCellAt(i).isDoorway()) {
					targets.add(getCellAt(i));
				}
				if (steps == 1) {
					if (!getCellAt(i).isDoorway()) {
						targets.add(getCellAt(i));
					}
				} else {
					calcTargetsHelper(i, steps-1);
				}
			}
		}
		visited[index] = false;
	}

	public HashSet<BoardCell> getTargets() {
		return targets;
	}

	public HashSet<BoardCell> getTargets(int index, int steps) {
		targets = new HashSet<BoardCell>();
		visited = new boolean[numRows*numColumns];
		Arrays.fill(visited, false);
		calcTargetsHelper(index, steps);
		return targets;
	}

	public void paintNames(Graphics g) {
		g.setColor(Color.WHITE);
		for (Character key : namesMap.keySet()) {
			g.drawString(rooms.get(key), namesMap.get(key).x*GUIBoard.CELL_SIZE, namesMap.get(key).y*GUIBoard.CELL_SIZE);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLUE);
		for(BoardCell cell : cells) {
			cell.draw(g, this);
		}
		if (targets != null) {
			for (BoardCell target : targets) {
				target.draw(g, this, Color.CYAN);
			}
		}
		paintNames(g);
	}

	public void setTargets(HashSet<BoardCell> set) {
		targets = set;
	}
}
