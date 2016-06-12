package sokonet.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

class Level {
	/**
	 *
	 */
	enum Content {
		Void, Wall, Player, Crate
	}

	/**
	 *
	 */
	class Cell {
		private Content content = Content.Void;
		private boolean target = false;
		private boolean outside = false;

		Content getContent() {
			return content;
		}

		boolean isTarget() {
			return target;
		}

		boolean isOutside() {
			return outside;
		}
	}

	private int index;
	private Cell[][] cells;
	private int px, py;
	private Stack<Command> rewind = new Stack<>();

	/**
	 *
	 * @param index
	 * @param width
	 * @param height
	 */
	Level(int index, int width, int height) {
		this.index = index;
		cells = new Cell[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				cells[x][y] = new Cell();
			}
		}
	}

	/**
	 * TODO
	 */
	void scanOutsides() {
		for (Cell[] col : cells) {
			for (int i = 0; i < col.length; i++) {
				if (col[i].content == Content.Wall) break;
				col[i].outside = true;
			}

			for (int i = col.length - 1; i > 0; i--) {
				if (col[i].content == Content.Wall) break;
				col[i].outside = true;
			}

		}
	}

	/**
	 *
	 * @return
	 */
	public int index() {
		return index;
	}

	/**
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	Cell cell(int x, int y) {
		return cells[x][y];
	}

	/**
	 *
	 * @return
	 */
	int width() {
		return cells.length;
	}

	/**
	 *
	 * @return
	 */
	int height() {
		return cells[0].length;
	}

	/**
	 *
	 * @param pt
	 */
	void setWall(Point pt) {
		setContent(pt, Content.Wall);
	}

	/**
	 *
	 * @param pt
	 */
	void setCrate(Point pt) {
		setContent(pt, Content.Crate);
	}

	/**
	 *
	 * @param pt
	 */
	void setPlayer(Point pt) {
		px = pt.getX();
		py = pt.getY();
		setContent(pt, Content.Player);
	}

	/**
	 *
	 * @param pt
	 */
	void setTarget(Point pt) {
		cells[pt.getX()][pt.getY()].target = true;
	}

	/**
	 *
	 * @param pt
	 * @param content
	 */
	private void setContent(Point pt, Content content) {
		cells[pt.getX()][pt.getY()].content = content;
	}

	/**
	 *
	 */
	void rewind() {
		if (!rewind.empty()) {
			rewind.pop().execute();
		}
	}

	/**
	 *
	 * @return
	 */
	boolean done() {
		for (Cell[] col : cells) {
			for (Cell cell : col) {
				if (cell.target && cell.content != Content.Crate) return false;
			}
		}
		return true;
	}

	/**
	 *
	 * @return
	 */
	List<Point> up() {
		return move(0, -1);
	}

	/**
	 *
	 * @return
	 */
	List<Point> right() {
		return move(1, 0);
	}

	/**
	 *
	 * @return
	 */
	List<Point> down() {
		return move(0, 1);
	}

	/**
	 *
	 * @return
	 */
	List<Point> left() {
		return move(-1, 0);
	}

	/**
	 *
	 * @param dx
	 * @param dy
	 * @return
	 */
	private List<Point> move(int dx, int dy) {
		List<Point> altered = new ArrayList<>(3);

		int x = px + dx;
		int y = py + dy;

		Command undo = () -> {
			cells[px][py].content = Content.Void;
			px -= dx;
			py -= dy;
			cells[px][py].content = Content.Player;
		};

		switch (cells[x][y].content) {
			case Wall:
				throw new IllegalStateException("Invalid move!");

			case Crate:
				int xx = x + dx;
				int yy = y + dy;
				if (cells[xx][yy].content != Content.Void) {
					throw new IllegalStateException("Something is blocking this crate!");
				} else {
					cells[xx][yy].content = Content.Crate;
					altered.add(new Point(xx, yy));
					undo = undo.andThen(() -> {
						cells[xx][yy].content = Content.Void;
						cells[xx - dx][yy - dy].content = Content.Crate;
					});
				}
				break;
		}

		cells[x][y].content = Content.Player;
		cells[px][py].content = Content.Void;

		altered.add(new Point(x, y));
		altered.add(new Point(px, py));

		px = x;
		py = y;

		rewind.push(undo);
		return altered;
	}
}
