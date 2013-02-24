/* This file is part of PilesOfBlocks.

	Copyright 2013 Mathias Broxvall
	
    PilesOfBlocks is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PilesOfBlocks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PilesOfBlocks.  If not, see <http://www.gnu.org/licenses/>.
*/ 	

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
