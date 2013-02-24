package mbrx.pob;

public class BlockItemSetting {
	public String name;
	public int blockID;
	public int metaData;
	public int stackSize;
	public int minPopTime;
	public int maxPopTime;
	
	public BlockItemSetting() {
		metaData=-1;
		blockID=-1;
		stackSize=1;
		name="unknown block";
		minPopTime=20;
		maxPopTime=20;
	}
	
	public final static BlockItemSetting nullSetting = new BlockItemSetting();
}
