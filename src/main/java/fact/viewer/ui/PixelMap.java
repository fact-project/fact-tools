package fact.viewer.ui;

public interface PixelMap {

	public HexTile getCellById(int id);
	public int getSelectedSlice();
	public HexTile addCell(int id, int i, int j);
}
