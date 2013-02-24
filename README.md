PilesOfBlocks (POB)
===================

A minecraft mod that turns blockitems in the world back into blocks,
making it harder to get rid of junk blocks.  

The main purpose of this mod is to make mining a little bit more
realistic by requiring the player to setup an infrastructure for
transporting both the valuable ores as the undesirable rock mass out
of the mine. The primary mechanism added in this mod is to to
automatically expire all dropped block items and turn them into
blocks. This means that all rocks that are mined have to be dumped
somewhere (usually on a large garbage pile outside the mine entrance). 

The second mechanism added is to lower the maximum stack size
of blocks in the players inventory - so that he cannot just carry an
almost infinite amount of rocks, but rather is encouraged to use sets
of minecarts to move the undesirable rocks out of the mine. 

Both mechanisms of this mod can be configured by using a set of rules
that specify which block items are affected by the mod, the timewindow
in which the blocks will be popped (put back in the world) and the
maximum allowed stacksize of the blocks. Rather than using hard-coded
BlockID's the rules are centrered around the use of regular
expressions for matching the block names - which allows the same
configuration files to be used for blocks introduced by any other
mods. 

Recommended other mods
======================

This mod is recommended to be played together with the amazing
BlockPhysics mod which allows the blocks to fall in realistic
ways. This mod will also make the act of digging reasonable mines a
little bit more challenging. 

If you also use the UndergroundBiomes mods you can use regular
expressions in the POB configuratioj file to simplify the matching of all the new types of
cobblestones. 

Configuring Piles of Blocks
===========================

To configure POB you need to edit the file config/PilesOfBlocks.cfg
which will reside under %APPDATA%/minecraft or under your .minecraft
directory (depending on if you are a Windows or Linux user). 

This file contains a set of generic setting at the bottom as well as a
set of setting for matching block names that should be affected by the
mod. 

All rules have a name of Bi_xyz where i is an integer counting up from 0.
Each rule has to have a name _Name (eg. B42_Name) that is a regular expression for matching blocks to the rule.
Additional information for the rules include any of the following:
  * _MetaData: overrides the metadata to give to the block when it pops
  * _StackSize: overrides the maximum allowed stacksize for blockitems
  matching the rule
  * _MinPopTime: overrides the minimum time before a block will be
  popped.
  * _MaxPopTime: overrides the maximum time before a block will be
  popped.

If there are multiple rules that match a specific block, then the last
specified rule will be used to update the setting of that block. 

MetaData should be -1 for most blocks, but can have another value for
eg. blocks that have a state and are to be modified by the
BlockPhysics mod. This only works for blocks with a blockid less than
256. 

Compatability
=============

POB requires the latest release of Forge (6.6.1 as of this writing) and the latest
release of Minecraft (1.4.7 as of this writing). I intend to keep it
updated as soon as possible whenever Forge and/or Minecraft updates,
depending on my online activities. 
