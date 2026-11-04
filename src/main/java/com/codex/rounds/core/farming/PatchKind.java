/*
 * Copyright (c) 2019 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.codex.rounds.core.farming;

/**
 * How each kind of patch reports its state through its varbit. Adapted from RuneLite's Time
 * Tracking plugin (net.runelite.client.plugins.timetracking.farming.PatchImplementation), keeping
 * only the patches a round visits and noting, from each object's menu, what the patch wants doing.
 */
public enum PatchKind
{
	MUSHROOM("", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Mushroom patch[Rake,Inspect,Guide] 8314,8313,8312,8311
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 9)
				{
					// Bittercap Mushrooms[Inspect,Guide] 8315,8316,8317,8318,8319,8320
					return new PatchState(Produce.MUSHROOM, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value >= 10 && value <= 15)
				{
					// Bittercap Mushrooms[Pick,Inspect,Guide] 8321,8322,8323,8324,8325,8326
					return new PatchState(Produce.MUSHROOM, CropState.HARVESTABLE, value - 10, Action.HARVEST);
				}
				if (value >= 16 && value <= 20)
				{
					// Diseased Bittercap Mushrooms[Cure,Inspect,Guide] 8327,8328,8329,8330,8331
					return new PatchState(Produce.MUSHROOM, CropState.DISEASED, value - 15, Action.CURE);
				}
				if (value >= 21 && value <= 25)
				{
					// Dead Bittercap Mushrooms[Clear,Inspect,Guide] 8332,8333,8334,8335,8336
					return new PatchState(Produce.MUSHROOM, CropState.DEAD, value - 20, Action.CLEAR);
				}
				if (value >= 26 && value <= 255)
				{
					// Mushroom patch[Rake,Inspect,Guide] 8314,8314,8314,8314,8314,8314,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	HESPORI("", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Hespori patch[Rake,Inspect,Guide] 33722,33723,33724,33725
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 6)
				{
					// Hespori[Inspect,Guide] 33726,33727,33728
					return new PatchState(Produce.HESPORI, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value >= 7 && value <= 8)
				{
					// Hespori[Harvest,Inspect,Guide,Clear] 33729,33730
					return new PatchState(Produce.HESPORI, CropState.HARVESTABLE, value - 7, Action.HARVEST);
				}
				if (value == 9)
				{
					// Hespori patch[Rake,Inspect,Guide] 33722
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	BUSH("", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7575,7574,7573
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value == 4)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 5 && value <= 9)
				{
					// Redberry bush[Inspect,Guide] 7692,7693,7694,7695,7696
					return new PatchState(Produce.REDBERRIES, CropState.GROWING, value - 5, Action.WAIT);
				}
				if (value >= 10 && value <= 14)
				{
					// Redberry bush[Clear,Inspect,Guide,Pick-from] 7697,7701,7701,7701,7701
					return new PatchState(Produce.REDBERRIES, CropState.HARVESTABLE, value - 10, Action.HARVEST);
				}
				if (value >= 15 && value <= 20)
				{
					// Cadavaberry bush[Inspect,Guide] 7581,7582,7583,7584,7585,7586
					return new PatchState(Produce.CADAVABERRIES, CropState.GROWING, value - 15, Action.WAIT);
				}
				if (value >= 21 && value <= 25)
				{
					// Cadavaberry bush[Clear,Inspect,Guide,Pick-from] 7587,7591,7591,7591,7591
					return new PatchState(Produce.CADAVABERRIES, CropState.HARVESTABLE, value - 21, Action.HARVEST);
				}
				if (value >= 26 && value <= 32)
				{
					// Dwellberry bush[Inspect,Guide] 7605,7606,7607,7608,7609,7610,7611
					return new PatchState(Produce.DWELLBERRIES, CropState.GROWING, value - 26, Action.WAIT);
				}
				if (value >= 33 && value <= 37)
				{
					// Dwellberry bush[Clear,Inspect,Guide,Pick-from] 7612,7616,7616,7616,7616
					return new PatchState(Produce.DWELLBERRIES, CropState.HARVESTABLE, value - 33, Action.HARVEST);
				}
				if (value >= 38 && value <= 45)
				{
					// Jangerberry bush[Inspect,Guide] 7632,7633,7634,7635,7636,7637,...
					return new PatchState(Produce.JANGERBERRIES, CropState.GROWING, value - 38, Action.WAIT);
				}
				if (value >= 46 && value <= 50)
				{
					// Jangerberry bush[Clear,Inspect,Guide,Pick-from] 7640,7644,7644,7644,7644
					return new PatchState(Produce.JANGERBERRIES, CropState.HARVESTABLE, value - 46, Action.HARVEST);
				}
				if (value >= 51 && value <= 58)
				{
					// Whiteberry bush[Inspect,Guide] 7713,7714,7715,7716,7717,7718,...
					return new PatchState(Produce.WHITEBERRIES, CropState.GROWING, value - 51, Action.WAIT);
				}
				if (value >= 59 && value <= 63)
				{
					// Whiteberry bush[Clear,Inspect,Guide,Pick-from] 7721,7725,7725,7725,7725
					return new PatchState(Produce.WHITEBERRIES, CropState.HARVESTABLE, value - 59, Action.HARVEST);
				}
				if (value >= 64 && value <= 69)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 70 && value <= 74)
				{
					// Diseased redberry bush[Prune,Inspect,Guide] 7703,7704,7705,7706,7707
					return new PatchState(Produce.REDBERRIES, CropState.DISEASED, value - 69, Action.PRUNE);
				}
				if (value >= 75 && value <= 79)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 80 && value <= 85)
				{
					// Diseased cadavaberry bush[Prune,Inspect,Guide] 7593,7594,7595,7596,7597,7598
					return new PatchState(Produce.CADAVABERRIES, CropState.DISEASED, value - 79, Action.PRUNE);
				}
				if (value >= 86 && value <= 90)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 91 && value <= 97)
				{
					// Diseased dwellberry bush[Prune,Inspect,Guide] 7618,7619,7620,7621,7622,7623,7624
					return new PatchState(Produce.DWELLBERRIES, CropState.DISEASED, value - 90, Action.PRUNE);
				}
				if (value >= 98 && value <= 102)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 103 && value <= 110)
				{
					// Diseased jangerberry bush[Prune,Inspect,Guide] 7646,7647,7648,7649,7650,7651,...
					return new PatchState(Produce.JANGERBERRIES, CropState.DISEASED, value - 102, Action.PRUNE);
				}
				if (value >= 111 && value <= 115)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 116 && value <= 123)
				{
					// Diseased whiteberry bush[Prune,Inspect,Guide] 7727,7728,7729,7730,7731,7732,...
					return new PatchState(Produce.WHITEBERRIES, CropState.DISEASED, value - 115, Action.PRUNE);
				}
				if (value >= 124 && value <= 133)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576,7576,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 134 && value <= 138)
				{
					// Dead redberry bush[Clear,Inspect,Guide] 7708,7709,7710,7711,7712
					return new PatchState(Produce.REDBERRIES, CropState.DEAD, value - 133, Action.CLEAR);
				}
				if (value >= 139 && value <= 143)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 144 && value <= 149)
				{
					// Dead cadavaberry bush[Clear,Inspect,Guide] 7599,7600,7601,7602,7603,7604
					return new PatchState(Produce.CADAVABERRIES, CropState.DEAD, value - 143, Action.CLEAR);
				}
				if (value >= 150 && value <= 154)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 155 && value <= 161)
				{
					// Dead dwellberry bush[Clear,Inspect,Guide] 7625,7626,7627,7628,7629,7630,7631
					return new PatchState(Produce.DWELLBERRIES, CropState.DEAD, value - 154, Action.CLEAR);
				}
				if (value >= 162 && value <= 166)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 167 && value <= 174)
				{
					// Dead jangerberry bush[Clear,Inspect,Guide] 7654,7655,7656,7657,7658,7659,...
					return new PatchState(Produce.JANGERBERRIES, CropState.DEAD, value - 166, Action.CLEAR);
				}
				if (value >= 175 && value <= 179)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 180 && value <= 187)
				{
					// Dead whiteberry bush[Clear,Inspect,Guide] 7735,7736,7737,7738,7739,7740,...
					return new PatchState(Produce.WHITEBERRIES, CropState.DEAD, value - 179, Action.CLEAR);
				}
				if (value >= 188 && value <= 196)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576,7576,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 197 && value <= 204)
				{
					// Poison Ivy bush[Inspect,Guide] 7662,7663,7664,7665,7666,7667,...
					return new PatchState(Produce.POISON_IVY, CropState.GROWING, value - 197, Action.WAIT);
				}
				if (value >= 205 && value <= 209)
				{
					// Poison Ivy bush[Clear,Inspect,Guide,Pick-from] 7670,7674,7674,7674,7674
					return new PatchState(Produce.POISON_IVY, CropState.HARVESTABLE, value - 205, Action.HARVEST);
				}
				if (value >= 210 && value <= 216)
				{
					// Diseased Poison Ivy bush[Prune,Inspect,Guide] 7676,7677,7678,7679,7680,7681,7682
					return new PatchState(Produce.POISON_IVY, CropState.DISEASED, value - 209, Action.PRUNE);
				}
				if (value >= 217 && value <= 224)
				{
					// Dead Poison Ivy bush[Clear,Inspect,Guide] 7684,7685,7686,7687,7688,7689,...
					return new PatchState(Produce.POISON_IVY, CropState.DEAD, value - 216, Action.CLEAR);
				}
				if (value == 225)
				{
					// Diseased Poison Ivy bush[Prune,Inspect,Guide] 7683
					return new PatchState(Produce.POISON_IVY, CropState.DISEASED, 8, Action.PRUNE);
				}
				if (value >= 226 && value <= 249)
				{
					// Bush Patch[Rake,Inspect,Guide] 7576,7576,7576,7576,7576,7576,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value == 250)
				{
					// Redberry bush[Check-health,Inspect,Guide] 7702
					return new PatchState(Produce.REDBERRIES, CropState.GROWING, Produce.REDBERRIES.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 251)
				{
					// Cadavaberry bush[Check-health,Inspect,Guide] 7592
					return new PatchState(Produce.CADAVABERRIES, CropState.GROWING, Produce.CADAVABERRIES.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 252)
				{
					// Dwellberry bush[Check-health,Inspect,Guide] 7617
					return new PatchState(Produce.DWELLBERRIES, CropState.GROWING, Produce.DWELLBERRIES.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 253)
				{
					// Jangerberry bush[Check-health,Inspect,Guide] 7645
					return new PatchState(Produce.JANGERBERRIES, CropState.GROWING, Produce.JANGERBERRIES.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 254)
				{
					// Whiteberry bush[Check-health,Inspect,Guide] 7726
					return new PatchState(Produce.WHITEBERRIES, CropState.GROWING, Produce.WHITEBERRIES.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 255)
				{
					// Poison Ivy bush[Check-health,Inspect,Guide] 7675
					return new PatchState(Produce.POISON_IVY, CropState.GROWING, Produce.POISON_IVY.getStages() - 1, Action.CHECK_HEALTH);
				}
				return null;
			}
		},
	HARDWOOD_TREE("Hardwood Trees", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Tree patch[Rake,Inspect,Guide] 30479,30478,30477,30476
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Tree patch[Rake,Inspect,Guide] 30479,30479,30479,30479
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 14)
				{
					// Teak Tree[Inspect,Guide] 30437,30438,30439,30440,30441,30442,30443
					return new PatchState(Produce.TEAK, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 15)
				{
					// Teak Tree[Check-health,Inspect,Guide] 30444
					return new PatchState(Produce.TEAK, CropState.GROWING, Produce.TEAK.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 16)
				{
					// Teak Tree[Chop down,Inspect,Guide] 30445
					return new PatchState(Produce.TEAK, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 17)
				{
					// Tree stump[Clear,Inspect,Guide] 30446
					return new PatchState(Produce.TEAK, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 18 && value <= 23)
				{
					// Diseased Teak[Prune,Inspect,Guide] 30447,30448,30449,30450,30451,30452
					return new PatchState(Produce.TEAK, CropState.DISEASED, value - 17, Action.PRUNE);
				}
				if (value >= 24 && value <= 29)
				{
					// Dead Teak[Clear,Inspect,Guide] 30453,30454,30455,30456,30457,30458
					return new PatchState(Produce.TEAK, CropState.DEAD, value - 23, Action.CLEAR);
				}
				if (value >= 30 && value <= 37)
				{
					// Mahogany sapling,Mahogany tree[Inspect,Guide] 30406,30407,30408,30409,30410,30411,...
					return new PatchState(Produce.MAHOGANY, CropState.GROWING, value - 30, Action.WAIT);
				}
				if (value == 38)
				{
					// Mahogany tree[Check-health,Inspect,Guide] 30416
					return new PatchState(Produce.MAHOGANY, CropState.GROWING, Produce.MAHOGANY.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 39)
				{
					// Mahogany tree[Chop down,Inspect,Guide] 30417
					return new PatchState(Produce.MAHOGANY, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 40)
				{
					// Mahogany tree stump[Clear,Inspect,Guide] 30418
					return new PatchState(Produce.MAHOGANY, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 41 && value <= 47)
				{
					// Diseased Mahogany[Prune,Inspect,Guide] 30419,30420,30421,30422,30423,30424,30425
					return new PatchState(Produce.MAHOGANY, CropState.DISEASED, value - 40, Action.PRUNE);
				}
				if (value >= 48 && value <= 54)
				{
					// Dead Mahogany[Clear,Inspect,Guide] 30428,30429,30430,30431,30432,30433,30434
					return new PatchState(Produce.MAHOGANY, CropState.DEAD, value - 47, Action.CLEAR);
				}
				if (value >= 55 && value <= 62)
				{
					// Camphor sapling,Camphor tree[Inspect,Guide] 58712,58713,58714,58715,58716,58717,...
					return new PatchState(Produce.CAMPHOR, CropState.GROWING, value - 55, Action.WAIT);
				}
				if (value == 63)
				{
					// Camphor tree[Check-health,Inspect,Guide] 58722
					return new PatchState(Produce.CAMPHOR, CropState.GROWING, Produce.CAMPHOR.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 64)
				{
					// Camphor tree[Chop down,Inspect,Guide] 58723
					return new PatchState(Produce.CAMPHOR, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 65)
				{
					// Camphor tree stump[Clear,Inspect,Guide] 58724
					return new PatchState(Produce.CAMPHOR, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 66 && value <= 72)
				{
					// Diseased Camphor[Prune,Inspect,Guide] 58725,58726,58727,58728,58729,58730,58731
					return new PatchState(Produce.CAMPHOR, CropState.DISEASED, value - 65, Action.PRUNE);
				}
				if (value >= 73 && value <= 79)
				{
					// Dead Camphor[Clear,Inspect,Guide] 58734,58735,58736,58737,58738,58739,58740
					return new PatchState(Produce.CAMPHOR, CropState.DEAD, value - 72, Action.CLEAR);
				}
				if (value >= 80 && value <= 87)
				{
					// Ironwood sapling,Ironwood tree[Inspect,Guide] 58743,58744,58745,58746,58747,58748,...
					return new PatchState(Produce.IRONWOOD, CropState.GROWING, value - 80, Action.WAIT);
				}
				if (value == 88)
				{
					// Ironwood tree[Check-health,Inspect,Guide] 58753
					return new PatchState(Produce.IRONWOOD, CropState.GROWING, Produce.IRONWOOD.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 89)
				{
					// Ironwood tree[Chop down,Inspect,Guide] 58754
					return new PatchState(Produce.IRONWOOD, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 90)
				{
					// Ironwood tree stump[Clear,Inspect,Guide] 58755
					return new PatchState(Produce.IRONWOOD, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 91 && value <= 97)
				{
					// Diseased Ironwood[Prune,Inspect,Guide] 58756,58757,58758,58759,58760,58761,58762
					return new PatchState(Produce.IRONWOOD, CropState.DISEASED, value - 90, Action.PRUNE);
				}
				if (value >= 98 && value <= 104)
				{
					// Dead Ironwood[Clear,Inspect,Guide] 58765,58766,58767,58768,58769,58770,58771
					return new PatchState(Produce.IRONWOOD, CropState.DEAD, value - 97, Action.CLEAR);
				}
				if (value >= 105 && value <= 113)
				{
					// Rosewood sapling,Rosewood tree[Inspect,Guide] 58774,58775,58776,58777,58778,58779,...
					return new PatchState(Produce.ROSEWOOD, CropState.GROWING, value - 105, Action.WAIT);
				}
				if (value == 114)
				{
					// Rosewood tree[Check-health,Inspect,Guide] 58784
					return new PatchState(Produce.ROSEWOOD, CropState.GROWING, Produce.ROSEWOOD.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 115)
				{
					// Rosewood tree[Chop down,Inspect,Guide] 58785
					return new PatchState(Produce.ROSEWOOD, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 116)
				{
					// Rosewood tree stump[Clear,Inspect,Guide] 58786
					return new PatchState(Produce.ROSEWOOD, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 117 && value <= 124)
				{
					// Diseased Rosewood[Prune,Inspect,Guide] 58787,58788,58789,58790,58791,58792,...
					return new PatchState(Produce.ROSEWOOD, CropState.DISEASED, value - 116, Action.PRUNE);
				}
				if (value >= 125 && value <= 132)
				{
					// Dead Rosewood[Clear,Inspect,Guide] 58796,58797,58798,58799,58800,58801,...
					return new PatchState(Produce.ROSEWOOD, CropState.DEAD, value - 124, Action.CLEAR);
				}
				if (value >= 133 && value <= 255)
				{
					// Tree patch[Rake,Inspect,Guide] 30479,30479,30479,30479,30479,30479,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	REDWOOD("Redwood Trees", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Redwood tree patch[Rake,Inspect,Guide] 34050,34049,34048,34047
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Redwood tree patch[Rake,Inspect,Guide] 34050,34050,34050,34050
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 17)
				{
					// Redwood tree[Inspect,Guide] 34205,34206,34207,34208,34209,34210,...
					return new PatchState(Produce.REDWOOD, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 18)
				{
					// Redwood tree[Clear,Inspect,Guide] 34278
					return new PatchState(Produce.REDWOOD, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 19 && value <= 27)
				{
					// Diseased Redwood tree[Prune,Inspect,Guide] 34130,34131,34132,34133,34134,34139,...
					return new PatchState(Produce.REDWOOD, CropState.DISEASED, value - 18, Action.PRUNE);
				}
				if (value >= 28 && value <= 36)
				{
					// Dead Redwood tree[Clear,Inspect,Guide] 34061,34062,34063,34064,34065,34070,...
					return new PatchState(Produce.REDWOOD, CropState.DEAD, value - 27, Action.CLEAR);
				}
				if (value == 37)
				{
					// Redwood tree[Check-health,Inspect,Guide] 34297
					return new PatchState(Produce.REDWOOD, CropState.GROWING, Produce.REDWOOD.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 41 && value <= 55)
				{
					// Redwood tree[Clear,Inspect,Guide] 34278,34278,34278,34278,34278,34278,...
					return new PatchState(Produce.REDWOOD, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				return null;
			}
		},
	SPIRIT_TREE("Spirit Trees", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Spirit Tree Patch[Rake,Inspect,Guide] 8342,8341,8340,8339
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Spirit Tree Patch[Rake,Inspect,Guide] 8342,8342,8342,8342
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 19)
				{
					// Spirit Tree[Inspect,Guide] 8343,8344,8345,8346,8347,8348,...
					return new PatchState(Produce.SPIRIT_TREE, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 20)
				{
					// Spirit Tree[Travel,Talk-to,Inspect,Guide,Clear] 8355
					return new PatchState(Produce.SPIRIT_TREE, CropState.GROWING, 12, Action.GROWN);
				}
				if (value >= 21 && value <= 31)
				{
					// Diseased Spirit Tree[Prune,Inspect,Guide] 8358,8359,8360,8361,8362,8363,...
					return new PatchState(Produce.SPIRIT_TREE, CropState.DISEASED, value - 20, Action.PRUNE);
				}
				if (value >= 32 && value <= 43)
				{
					// Dead Spirit Tree[Clear,Inspect,Guide] 8370,8371,8372,8373,8374,8375,...
					return new PatchState(Produce.SPIRIT_TREE, CropState.DEAD, value - 31, Action.CLEAR);
				}
				if (value == 44)
				{
					// Spirit Tree[Check-health,Inspect,Guide] 8356
					return new PatchState(Produce.SPIRIT_TREE, CropState.GROWING, Produce.SPIRIT_TREE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 45 && value <= 63)
				{
					// Spirit Tree Patch[Rake,Inspect,Guide] 8342,8342,8342,8342,8342,8342,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	ANIMA("", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Anima patch[Rake,Inspect,Guide] 33983,33982,33981,33980
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Anima patch[Rake,Inspect,Guide] 33983,33983,33983,33983
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 16)
				{
					// Attas plant[Inspect,Guide] 33991,33992,33993,33994,33995
					// Attas plant[Inspect,Guide] 33995,33995
					// Withering Attas plant[Inspect,Guide] 33996
					// Dead Attas plant[Clear,Inspect,Guide] 33997
					return new PatchState(Produce.ATTAS, value - 8 == 8 ? CropState.DEAD : CropState.GROWING, value - 8, value - 8 == 8 ? Action.CLEAR : Action.WAIT);
				}
				if (value >= 17 && value <= 25)
				{
					// Iasor plant[Inspect,Guide] 33984,33985,33986,33987,33988
					// Iasor plant[Inspect,Guide] 33988,33988
					// Withering Iasor plant[Inspect,Guide] 33989
					// Dead Iasor plant[Clear,Inspect,Guide] 33990
					return new PatchState(Produce.IASOR, value - 17 == 8 ? CropState.DEAD : CropState.GROWING, value - 17, value - 17 == 8 ? Action.CLEAR : Action.WAIT);
				}
				if (value >= 26 && value <= 34)
				{
					// Kronos plant[Inspect,Guide] 33999,34000,34001,34002,34003
					// Kronos plant[Inspect,Guide] 34003,34003
					// Withering Kronos plant[Inspect,Guide] 34004
					// Dead Kronos plant[Clear,Inspect,Guide] 34005
					return new PatchState(Produce.KRONOS, value - 26 == 8 ? CropState.DEAD : CropState.GROWING, value - 26, value - 26 == 8 ? Action.CLEAR : Action.WAIT);
				}
				if (value >= 35 && value <= 255)
				{
					// Anima patch[Rake,Inspect,Guide] 33983,33983,33983,33983,33983,33983,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	BELLADONNA("Belladonna", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Belladonna patch[Rake,Inspect,Guide] 7560,7559,7558,7557
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Belladonna[Inspect,Guide] 7561,7562,7563,7564
					return new PatchState(Produce.BELLADONNA, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value == 8)
				{
					// Belladonna[Pick,Inspect,Guide] 7565
					return new PatchState(Produce.BELLADONNA, CropState.HARVESTABLE, 0, Action.HARVEST);
				}
				if (value >= 9 && value <= 11)
				{
					// Diseased Belladonna[Cure,Inspect,Guide] 7566,7567,7568
					return new PatchState(Produce.BELLADONNA, CropState.DISEASED, value - 8, Action.CURE);
				}
				if (value >= 12 && value <= 14)
				{
					// Dead Belladonna[Clear,Inspect,Guide] 7569,7570,7571
					return new PatchState(Produce.BELLADONNA, CropState.DEAD, value - 11, Action.CLEAR);
				}
				if (value >= 15 && value <= 255)
				{
					// Belladonna patch[Rake,Inspect,Guide] 7560,7560,7560,7560,7560,7560,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	CACTUS("Cactus", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Cactus patch[Rake,Inspect,Guide] 7746,7745,7744,7743
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Cactus patch[Rake,Inspect,Guide] 7746,7746,7746,7746
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 14)
				{
					// Cactus[Inspect,Guide] 7747,7748,7749,7750,7751,7752,7753
					return new PatchState(Produce.CACTUS, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value >= 15 && value <= 18)
				{
					// Cactus[Clear,Inspect,Guide,Pick-spine] 7754,7757,7757,7757
					return new PatchState(Produce.CACTUS, CropState.HARVESTABLE, value - 15, Action.HARVEST);
				}
				if (value >= 19 && value <= 24)
				{
					// Diseased cactus[Cure,Inspect,Guide] 7759,7760,7761,7762,7763,7764
					return new PatchState(Produce.CACTUS, CropState.DISEASED, value - 18, Action.CURE);
				}
				if (value >= 25 && value <= 30)
				{
					// Dead cactus[Clear,Inspect,Guide] 7765,7766,7767,7768,7769,7770
					return new PatchState(Produce.CACTUS, CropState.DEAD, value - 24, Action.CLEAR);
				}
				if (value == 31)
				{
					// Cactus[Check-health,Inspect,Guide] 7758
					return new PatchState(Produce.CACTUS, CropState.GROWING, Produce.CACTUS.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 32 && value <= 38)
				{
					// Potato cactus[Inspect,Guide] 33734,33735,33736,33737,33738,33739,33740
					return new PatchState(Produce.POTATO_CACTUS, CropState.GROWING, value - 32, Action.WAIT);
				}
				if (value >= 39 && value <= 45)
				{
					// Potato cactus[Clear,Inspect,Guide,Pick] 33741,33742,33743,33744,33745,33746,33747
					return new PatchState(Produce.POTATO_CACTUS, CropState.HARVESTABLE, value - 39, Action.HARVEST);
				}
				if (value >= 46 && value <= 51)
				{
					// Diseased Potato cactus[Cure,Inspect,Guide] 33749,33750,33751,33752,33753,33754
					return new PatchState(Produce.POTATO_CACTUS, CropState.DISEASED, value - 45, Action.CURE);
				}
				if (value >= 52 && value <= 57)
				{
					// Dead Potato cactus[Clear,Inspect,Guide] 33755,33756,33757,33758,33759,33760
					return new PatchState(Produce.POTATO_CACTUS, CropState.DEAD, value - 51, Action.CLEAR);
				}
				if (value == 58)
				{
					// Potato cactus[Check-health,Inspect,Guide] 33748
					return new PatchState(Produce.POTATO_CACTUS, CropState.GROWING, Produce.POTATO_CACTUS.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 59 && value <= 255)
				{
					// Cactus patch[Rake,Inspect,Guide] 7746,7746,7746,7746,7746,7746,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	SEAWEED("Seaweed", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Seaweed patch[Rake,Inspect,Guide] 30486,30485,30484,30483
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Seaweed[Inspect,Guide] 30487,30488,30489,30490
					return new PatchState(Produce.SEAWEED, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value >= 8 && value <= 10)
				{
					// Seaweed[Pick,Inspect,Guide] 30491,30492,30493
					return new PatchState(Produce.SEAWEED, CropState.HARVESTABLE, value - 8, Action.HARVEST);
				}
				if (value >= 11 && value <= 13)
				{
					// Diseased seaweed[Cure,Inspect,Guide] 30494,30495,30496
					return new PatchState(Produce.SEAWEED, CropState.DISEASED, value - 10, Action.CURE);
				}
				if (value >= 14 && value <= 16)
				{
					// Dead seaweed[Clear,Inspect,Guide] 30497,30498,30499
					return new PatchState(Produce.SEAWEED, CropState.DEAD, value - 13, Action.CLEAR);
				}
				if (value >= 17 && value <= 255)
				{
					// Seaweed patch[Rake,Inspect,Guide] 30486,30486,30486,30486,30486,30486,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	CALQUAT("Calquat", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Calquat patch[Rake,Inspect,Guide] 7775,7774,7773,7772
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 11)
				{
					// Calquat Tree[Inspect,Guide] 7776,7777,7778,7779,7780,7781,...
					return new PatchState(Produce.CALQUAT, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value >= 12 && value <= 18)
				{
					// Calquat Tree[Clear,Inspect,Guide,Pick-fruit] 7784,7785,7786,7787,7788,7789,7790
					return new PatchState(Produce.CALQUAT, CropState.HARVESTABLE, value - 12, Action.HARVEST);
				}
				if (value >= 19 && value <= 25)
				{
					// Diseased Calquat[Prune,Inspect,Guide] 7792,7793,7794,7795,7796,7797,7798
					return new PatchState(Produce.CALQUAT, CropState.DISEASED, value - 18, Action.PRUNE);
				}
				if (value >= 26 && value <= 33)
				{
					// Dead Calquat[Clear,Inspect,Guide] 7799,7800,7801,7802,7803,7804,...
					return new PatchState(Produce.CALQUAT, CropState.DEAD, value - 25, Action.CLEAR);
				}
				if (value == 34)
				{
					// Calquat Tree[Check-health,Inspect,Guide] 7791
					return new PatchState(Produce.CALQUAT, CropState.GROWING, Produce.CALQUAT.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 35 && value <= 255)
				{
					// Calquat patch[Rake,Inspect,Guide] 7775,7775,7775,7775,7775,7775,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	CELASTRUS("Celastrus", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Celastrus patch[Rake,Inspect,Guide] 33698,33697,33696,33695
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Celastrus patch[Rake,Inspect,Guide] 33698,33698,33698,33698
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 12)
				{
					// Celastrus tree[Inspect,Guide] 33699,33700,33701,33702,33703
					return new PatchState(Produce.CELASTRUS, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 13)
				{
					// Celastrus tree[Check-health,Inspect,Guide] 33704
					return new PatchState(Produce.CELASTRUS, CropState.GROWING, Produce.CELASTRUS.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 14 && value <= 16)
				{
					// Celastrus tree[Harvest,Inspect,Guide] 33719,33718,33717
					return new PatchState(Produce.CELASTRUS, CropState.HARVESTABLE, value - 14, Action.HARVEST);
				}
				if (value == 17)
				{
					// Harvested Celastrus tree[Chop,Inspect,Guide] 33720
					return new PatchState(Produce.CELASTRUS, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value >= 18 && value <= 22)
				{
					// Diseased celastrus tree[Prune,Inspect,Guide] 33705,33706,33707,33708,33709
					return new PatchState(Produce.CELASTRUS, CropState.DISEASED, value - 17, Action.PRUNE);
				}
				if (value >= 23 && value <= 27)
				{
					// Dead celastrus tree[Clear,Inspect,Guide] 33711,33712,33713,33714,33715
					return new PatchState(Produce.CELASTRUS, CropState.DEAD, value - 22, Action.CLEAR);
				}
				if (value == 28)
				{
					// Celastrus tree stump[Clear,Inspect,Guide] 33721
					return new PatchState(Produce.CELASTRUS, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 29 && value <= 255)
				{
					// Celastrus patch[Rake,Inspect,Guide] 33698,33698,33698,33698,33698,33698,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	CRYSTAL_TREE("Crystal Tree", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Crystal tree patch[Rake,Inspect,Guide] 34910,34909,34908,34907
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 8 && value <= 13)
				{
					// Crystal tree[Inspect,Guide] 34911,34912,34913,34914,34915,34916
					return new PatchState(Produce.CRYSTAL_TREE, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 14)
				{
					// Crystal tree[Check-health,Inspect,Guide] 34917
					return new PatchState(Produce.CRYSTAL_TREE, CropState.GROWING, Produce.CRYSTAL_TREE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 15)
				{
					// Crystal tree[Chop-down,Inspect,Guide] 34918
					return new PatchState(Produce.CRYSTAL_TREE, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				return null;
			}
		},
	CORAL("Coral", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Coral nursery[Inspect,Guide] 58676: no weeds grow on a nursery, it is ready for a frag
					return new PatchState(Produce.WEEDS, CropState.GROWING, 0, Action.RAKE);
				}
				for (int c = 0; c < CORALS.length; c++)
				{
					// Each coral takes eleven values: four growing, one to collect, three diseased, three dead.
					int base = 4 + 11 * c;
					Produce coral = CORALS[c];
					if (value >= base && value <= base + 3)
					{
						// Elkhorn 58677-58680, pillar 58682-58685, umbral 58687-58690 [Inspect,Guide]
						return new PatchState(coral, CropState.GROWING, value - base, Action.WAIT);
					}
					if (value == base + 4)
					{
						// Elkhorn 58681, pillar 58686, umbral 58691 [Collect,Inspect,Guide]
						return new PatchState(coral, CropState.HARVESTABLE, 0, Action.HARVEST);
					}
					if (value >= base + 5 && value <= base + 7)
					{
						// Diseased coral [Prune,Inspect,Guide]
						return new PatchState(coral, CropState.DISEASED, value - base - 4, Action.PRUNE);
					}
					if (value >= base + 8 && value <= base + 10)
					{
						// Dead coral [Clear,Inspect,Guide]
						return new PatchState(coral, CropState.DEAD, value - base - 7, Action.CLEAR);
					}
				}
				return null;
			}
		},
	HERB("Herbs", false)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Herb patch[Rake,Inspect,Guide] 8135,8134,8133,8132
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Herbs[Inspect,Guide] 14209,26825,26826,26827
					return new PatchState(Produce.GUAM, CropState.GROWING, value - 4, Action.WAIT);
				}
				if (value >= 8 && value <= 10)
				{
					// Herbs[Pick,Inspect,Guide] 26828,26828,26828
					return new PatchState(Produce.GUAM, CropState.HARVESTABLE, 10 - value, Action.HARVEST);
				}
				if (value >= 11 && value <= 14)
				{
					// Herbs[Inspect,Guide] 26829,39748,39749,39750
					return new PatchState(Produce.MARRENTILL, CropState.GROWING, value - 11, Action.WAIT);
				}
				if (value >= 15 && value <= 17)
				{
					// Herbs[Pick,Inspect,Guide] 39751,39751,39751
					return new PatchState(Produce.MARRENTILL, CropState.HARVESTABLE, 17 - value, Action.HARVEST);
				}
				if (value >= 18 && value <= 21)
				{
					// Herbs[Inspect,Guide] 39752,39753,39754,39755
					return new PatchState(Produce.TARROMIN, CropState.GROWING, value - 18, Action.WAIT);
				}
				if (value >= 22 && value <= 24)
				{
					// Herbs[Pick,Inspect,Guide] 39756,39756,39756
					return new PatchState(Produce.TARROMIN, CropState.HARVESTABLE, 24 - value, Action.HARVEST);
				}
				if (value >= 25 && value <= 28)
				{
					// Herbs[Inspect,Guide] 39757,39758,39759,39760
					return new PatchState(Produce.HARRALANDER, CropState.GROWING, value - 25, Action.WAIT);
				}
				if (value >= 29 && value <= 31)
				{
					// Herbs[Pick,Inspect,Guide] 39761,39761,39761
					return new PatchState(Produce.HARRALANDER, CropState.HARVESTABLE, 31 - value, Action.HARVEST);
				}
				if (value >= 32 && value <= 35)
				{
					// Herbs[Inspect,Guide] 39762,39763,39764,39765
					return new PatchState(Produce.RANARR, CropState.GROWING, value - 32, Action.WAIT);
				}
				if (value >= 36 && value <= 38)
				{
					// Herbs[Pick,Inspect,Guide] 39766,39766,39766
					return new PatchState(Produce.RANARR, CropState.HARVESTABLE, 38 - value, Action.HARVEST);
				}
				if (value >= 39 && value <= 42)
				{
					// Herbs[Inspect,Guide] 39777,39778,39779,39780
					return new PatchState(Produce.TOADFLAX, CropState.GROWING, value - 39, Action.WAIT);
				}
				if (value >= 43 && value <= 45)
				{
					// Herbs[Pick,Inspect,Guide] 39781,39781,39781
					return new PatchState(Produce.TOADFLAX, CropState.HARVESTABLE, 45 - value, Action.HARVEST);
				}
				if (value >= 46 && value <= 49)
				{
					// Herbs[Inspect,Guide] 39767,39768,39769,39770
					return new PatchState(Produce.IRIT, CropState.GROWING, value - 46, Action.WAIT);
				}
				if (value >= 50 && value <= 52)
				{
					// Herbs[Pick,Inspect,Guide] 39771,39771,39771
					return new PatchState(Produce.IRIT, CropState.HARVESTABLE, 52 - value, Action.HARVEST);
				}
				if (value >= 53 && value <= 56)
				{
					// Herbs[Inspect,Guide] 39772,39773,39774,39775
					return new PatchState(Produce.AVANTOE, CropState.GROWING, value - 53, Action.WAIT);
				}
				if (value >= 57 && value <= 59)
				{
					// Herbs[Pick,Inspect,Guide] 39776,39776,39776
					return new PatchState(Produce.AVANTOE, CropState.HARVESTABLE, 59 - value, Action.HARVEST);
				}
				if (value >= 60 && value <= 63)
				{
					// Herbs[Inspect,Guide] 55347,55348,55349,55350
					return new PatchState(Produce.HUASCA, CropState.GROWING, value - 60, Action.WAIT);
				}
				if (value >= 64 && value <= 66)
				{
					// Herbs[Pick,Inspect,Guide] 55351,55351,55351
					return new PatchState(Produce.HUASCA, CropState.HARVESTABLE, 66 - value, Action.HARVEST);
				}
				if (value == 67)
				{
					// Herb patch[Rake,Inspect,Guide] 8135
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 68 && value <= 71)
				{
					// Herbs[Inspect,Guide] 39782,39783,39784,39785
					return new PatchState(Produce.KWUARM, CropState.GROWING, value - 68, Action.WAIT);
				}
				if (value >= 72 && value <= 74)
				{
					// Herbs[Pick,Inspect,Guide] 39786,39786,39786
					return new PatchState(Produce.KWUARM, CropState.HARVESTABLE, 74 - value, Action.HARVEST);
				}
				if (value >= 75 && value <= 78)
				{
					// Herbs[Inspect,Guide] 39807,39808,39809,39810
					return new PatchState(Produce.SNAPDRAGON, CropState.GROWING, value - 75, Action.WAIT);
				}
				if (value >= 79 && value <= 81)
				{
					// Herbs[Pick,Inspect,Guide] 39811,39811,39811
					return new PatchState(Produce.SNAPDRAGON, CropState.HARVESTABLE, 81 - value, Action.HARVEST);
				}
				if (value >= 82 && value <= 85)
				{
					// Herbs[Inspect,Guide] 39787,39788,39789,39790
					return new PatchState(Produce.CADANTINE, CropState.GROWING, value - 82, Action.WAIT);
				}
				if (value >= 86 && value <= 88)
				{
					// Herbs[Pick,Inspect,Guide] 39791,39791,39791
					return new PatchState(Produce.CADANTINE, CropState.HARVESTABLE, 88 - value, Action.HARVEST);
				}
				if (value >= 89 && value <= 92)
				{
					// Herbs[Inspect,Guide] 39792,39793,39794,39795
					return new PatchState(Produce.LANTADYME, CropState.GROWING, value - 89, Action.WAIT);
				}
				if (value >= 93 && value <= 95)
				{
					// Herbs[Pick,Inspect,Guide] 39796,39796,39796
					return new PatchState(Produce.LANTADYME, CropState.HARVESTABLE, 95 - value, Action.HARVEST);
				}
				if (value >= 96 && value <= 99)
				{
					// Herbs[Inspect,Guide] 39797,39798,39799,39800
					return new PatchState(Produce.DWARF_WEED, CropState.GROWING, value - 96, Action.WAIT);
				}
				if (value >= 100 && value <= 102)
				{
					// Herbs[Pick,Inspect,Guide] 39801,39801,39801
					return new PatchState(Produce.DWARF_WEED, CropState.HARVESTABLE, 102 - value, Action.HARVEST);
				}
				if (value >= 103 && value <= 106)
				{
					// Herbs[Inspect,Guide] 39802,39803,39804,39805
					return new PatchState(Produce.TORSTOL, CropState.GROWING, value - 103, Action.WAIT);
				}
				if (value >= 107 && value <= 109)
				{
					// Herbs[Pick,Inspect,Guide] 39806,39806,39806
					return new PatchState(Produce.TORSTOL, CropState.HARVESTABLE, 109 - value, Action.HARVEST);
				}
				if (value >= 128 && value <= 130)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.GUAM, CropState.DISEASED, value - 127, Action.CURE);
				}
				if (value >= 131 && value <= 133)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.MARRENTILL, CropState.DISEASED, value - 130, Action.CURE);
				}
				if (value >= 134 && value <= 136)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.TARROMIN, CropState.DISEASED, value - 133, Action.CURE);
				}
				if (value >= 137 && value <= 139)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.HARRALANDER, CropState.DISEASED, value - 136, Action.CURE);
				}
				if (value >= 140 && value <= 142)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.RANARR, CropState.DISEASED, value - 139, Action.CURE);
				}
				if (value >= 143 && value <= 145)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.TOADFLAX, CropState.DISEASED, value - 142, Action.CURE);
				}
				if (value >= 146 && value <= 148)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.IRIT, CropState.DISEASED, value - 145, Action.CURE);
				}
				if (value >= 149 && value <= 151)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.AVANTOE, CropState.DISEASED, value - 148, Action.CURE);
				}
				if (value >= 152 && value <= 154)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.KWUARM, CropState.DISEASED, value - 151, Action.CURE);
				}
				if (value >= 155 && value <= 157)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.SNAPDRAGON, CropState.DISEASED, value - 154, Action.CURE);
				}
				if (value >= 158 && value <= 160)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.CADANTINE, CropState.DISEASED, value - 157, Action.CURE);
				}
				if (value >= 161 && value <= 163)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.LANTADYME, CropState.DISEASED, value - 160, Action.CURE);
				}
				if (value >= 164 && value <= 166)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.DWARF_WEED, CropState.DISEASED, value - 163, Action.CURE);
				}
				if (value >= 167 && value <= 169)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.TORSTOL, CropState.DISEASED, value - 166, Action.CURE);
				}
				if (value >= 170 && value <= 172)
				{
					// Dead herbs[Clear,Inspect,Guide] 8147,8148,8149
					return new PatchState(Produce.ANYHERB, CropState.DEAD, value - 169, Action.CLEAR);
				}
				if (value >= 173 && value <= 175)
				{
					// Diseased herbs[Cure,Inspect,Guide] 8144,8145,8146
					return new PatchState(Produce.HUASCA, CropState.DISEASED, value - 172, Action.CURE);
				}
				if (value >= 176 && value <= 191)
				{
					// Herb patch[Rake,Inspect,Guide] 8135,8135,8135,8135,8135,8135,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 192 && value <= 195)
				{
					// Goutweed[Inspect,Guide] 9044,9045,9046,9047
					return new PatchState(Produce.GOUTWEED, CropState.GROWING, value - 192, Action.WAIT);
				}
				if (value >= 196 && value <= 197)
				{
					// Goutweed[Pick,Inspect,Guide] 9048,9048
					return new PatchState(Produce.GOUTWEED, CropState.HARVESTABLE, 197 - value, Action.HARVEST);
				}
				if (value >= 198 && value <= 200)
				{
					// Diseased goutweed[Cure,Inspect,Guide] 9049,9050,9051
					return new PatchState(Produce.GOUTWEED, CropState.DISEASED, value - 197, Action.CURE);
				}
				if (value >= 201 && value <= 203)
				{
					// Dead goutweed[Clear,Inspect,Guide] 9052,9053,9054
					return new PatchState(Produce.GOUTWEED, CropState.DEAD, value - 200, Action.CLEAR);
				}
				if (value >= 204 && value <= 219)
				{
					// Herb patch[Rake,Inspect,Guide] 8135,8135,8135,8135,8135,8135,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 221 && value <= 255)
				{
					// Herb patch[Rake,Inspect,Guide] 8135,8135,8135,8135,8135,8135,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	TREE("Trees", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8394,8393,8392
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395,8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 11)
				{
					// Oak[Inspect,Guide] 8462,8463,8464,8465
					return new PatchState(Produce.OAK, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value == 12)
				{
					// Oak[Check-health,Inspect,Guide] 8466
					return new PatchState(Produce.OAK, CropState.GROWING, Produce.OAK.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 13)
				{
					// Oak[Chop down,Inspect,Guide] 8467
					return new PatchState(Produce.OAK, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 14)
				{
					// Oak tree stump[Clear,Inspect,Guide] 8468
					return new PatchState(Produce.OAK, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 15 && value <= 20)
				{
					// Willow Tree[Inspect,Guide] 8481,8482,8483,8484,8485,8486
					return new PatchState(Produce.WILLOW, CropState.GROWING, value - 15, Action.WAIT);
				}
				if (value == 21)
				{
					// Willow Tree[Check-health,Inspect,Guide] 8487
					return new PatchState(Produce.WILLOW, CropState.GROWING, Produce.WILLOW.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 22)
				{
					// Willow Tree[Chop down,Inspect,Guide] 8488
					return new PatchState(Produce.WILLOW, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 23)
				{
					// Willow tree stump[Clear,Inspect,Guide] 8489
					return new PatchState(Produce.WILLOW, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 24 && value <= 31)
				{
					// Maple Tree[Inspect,Guide] 8435,8436,8437,8438,8439,8440,...
					return new PatchState(Produce.MAPLE, CropState.GROWING, value - 24, Action.WAIT);
				}
				if (value == 32)
				{
					// Maple Tree[Check-health,Inspect,Guide] 8443
					return new PatchState(Produce.MAPLE, CropState.GROWING, Produce.MAPLE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 33)
				{
					// Maple Tree[Chop down,Inspect,Guide] 8444
					return new PatchState(Produce.MAPLE, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 34)
				{
					// Tree stump[Clear,Inspect,Guide] 8445
					return new PatchState(Produce.MAPLE, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 35 && value <= 44)
				{
					// Yew sapling,Yew tree[Inspect,Guide] 8502,8503,8504,8505,8506,8507,...
					return new PatchState(Produce.YEW, CropState.GROWING, value - 35, Action.WAIT);
				}
				if (value == 45)
				{
					// Yew tree[Check-health,Inspect,Guide] 8512
					return new PatchState(Produce.YEW, CropState.GROWING, Produce.YEW.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 46)
				{
					// Yew tree[Chop down,Inspect,Guide] 8513
					return new PatchState(Produce.YEW, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 47)
				{
					// Yew tree stump[Clear,Inspect,Guide] 8514
					return new PatchState(Produce.YEW, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 48 && value <= 59)
				{
					// Magic Tree[Inspect,Guide] 8396,8397,8398,8399,8400,8401,...
					return new PatchState(Produce.MAGIC, CropState.GROWING, value - 48, Action.WAIT);
				}
				if (value == 60)
				{
					// Magic Tree[Check-health,Inspect,Guide] 8408
					return new PatchState(Produce.MAGIC, CropState.GROWING, Produce.MAGIC.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value == 61)
				{
					// Magic Tree[Chop down,Inspect,Guide] 8409
					return new PatchState(Produce.MAGIC, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value == 62)
				{
					// Magic Tree Stump[Clear,Inspect,Guide] 8410
					return new PatchState(Produce.MAGIC, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value >= 63 && value <= 72)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395,8395,8395,8395,8395,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 73 && value <= 75)
				{
					// Diseased Oak[Prune,Inspect,Guide] 8473,8474,8475
					return new PatchState(Produce.OAK, CropState.DISEASED, value - 72, Action.PRUNE);
				}
				if (value == 77)
				{
					// Diseased Oak[Prune,Inspect,Guide] 8476
					return new PatchState(Produce.OAK, CropState.DISEASED, 4, Action.PRUNE);
				}
				if (value >= 78 && value <= 79)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 80 && value <= 84)
				{
					// Diseased Willow[Prune,Inspect,Guide] 8490,8491,8492,8493,8494
					return new PatchState(Produce.WILLOW, CropState.DISEASED, value - 79, Action.PRUNE);
				}
				if (value == 86)
				{
					// Diseased Willow[Prune,Inspect,Guide] 8495
					return new PatchState(Produce.WILLOW, CropState.DISEASED, 6, Action.PRUNE);
				}
				if (value >= 87 && value <= 88)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 89 && value <= 95)
				{
					// Diseased Maple[Prune,Inspect,Guide] 8446,8447,8448,8449,8450,8451,...
					return new PatchState(Produce.MAPLE, CropState.DISEASED, value - 88, Action.PRUNE);
				}
				if (value == 97)
				{
					// Diseased Maple[Prune,Inspect,Guide] 8453
					return new PatchState(Produce.MAPLE, CropState.DISEASED, 8, Action.PRUNE);
				}
				if (value >= 98 && value <= 99)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 100 && value <= 108)
				{
					// Diseased Yew[Prune,Inspect,Guide] 8515,8516,8517,8518,8519,8520,...
					return new PatchState(Produce.YEW, CropState.DISEASED, value - 99, Action.PRUNE);
				}
				if (value == 110)
				{
					// Diseased Yew[Prune,Inspect,Guide] 8524
					return new PatchState(Produce.YEW, CropState.DISEASED, 10, Action.PRUNE);
				}
				if (value >= 111 && value <= 112)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 113 && value <= 123)
				{
					// Diseased Magic Tree[Prune,Inspect,Guide] 8411,8412,8413,8414,8415,8416,...
					return new PatchState(Produce.MAGIC, CropState.DISEASED, value - 112, Action.PRUNE);
				}
				if (value == 125)
				{
					// Diseased Magic Tree[Prune,Inspect,Guide] 8422
					return new PatchState(Produce.MAGIC, CropState.DISEASED, 12, Action.PRUNE);
				}
				if (value >= 126 && value <= 136)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395,8395,8395,8395,8395,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 137 && value <= 139)
				{
					// Dead Oak[Clear,Inspect,Guide] 8477,8478,8479
					return new PatchState(Produce.OAK, CropState.DEAD, value - 136, Action.CLEAR);
				}
				if (value == 141)
				{
					// Dead Oak[Clear,Inspect,Guide] 8480
					return new PatchState(Produce.OAK, CropState.DEAD, 4, Action.CLEAR);
				}
				if (value >= 142 && value <= 143)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 144 && value <= 148)
				{
					// Dead Willow[Clear,Inspect,Guide] 8496,8497,8498,8499,8500
					return new PatchState(Produce.WILLOW, CropState.DEAD, value - 143, Action.CLEAR);
				}
				if (value == 150)
				{
					// Dead Willow[Clear,Inspect,Guide] 8501
					return new PatchState(Produce.WILLOW, CropState.DEAD, 6, Action.CLEAR);
				}
				if (value >= 151 && value <= 152)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 153 && value <= 159)
				{
					// Dead Maple[Clear,Inspect,Guide] 8454,8455,8456,8457,8458,8459,...
					return new PatchState(Produce.MAPLE, CropState.DEAD, value - 152, Action.CLEAR);
				}
				if (value == 161)
				{
					// Dead Maple[Clear,Inspect,Guide] 8461
					return new PatchState(Produce.MAPLE, CropState.DEAD, 8, Action.CLEAR);
				}
				if (value >= 162 && value <= 163)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 164 && value <= 172)
				{
					// Dead Yew[Clear,Inspect,Guide] 8525,8526,8527,8528,8529,8530,...
					return new PatchState(Produce.YEW, CropState.DEAD, value - 163, Action.CLEAR);
				}
				if (value == 174)
				{
					// Dead Yew[Clear,Inspect,Guide] 8534
					return new PatchState(Produce.YEW, CropState.DEAD, 10, Action.CLEAR);
				}
				if (value >= 175 && value <= 176)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 177 && value <= 187)
				{
					// Dead Magic Tree[Clear,Inspect,Guide] 8423,8424,8425,8426,8427,8428,...
					return new PatchState(Produce.MAGIC, CropState.DEAD, value - 176, Action.CLEAR);
				}
				if (value == 189)
				{
					// Dead Magic Tree[Clear,Inspect,Guide] 8434
					return new PatchState(Produce.MAGIC, CropState.DEAD, 12, Action.CLEAR);
				}
				if (value >= 190 && value <= 191)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 192 && value <= 197)
				{
					// Willow Tree[Chop down,Inspect,Guide] 8488,8488,8488,8488,8488,8488
					return new PatchState(Produce.WILLOW, CropState.HARVESTABLE, 0, Action.CHOP);
				}
				if (value >= 198 && value <= 255)
				{
					// Tree patch[Rake,Inspect,Guide] 8395,8395,8395,8395,8395,8395,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		},
	FRUIT_TREE("Fruit Trees", true)
		{
			@Override
			public PatchState forVarbitValue(int value)
			{
				if (value >= 0 && value <= 3)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8049,8048,8047
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3 - value, Action.RAKE);
				}
				if (value >= 4 && value <= 7)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8050,8050,8050
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 8 && value <= 13)
				{
					// Apple tree[Inspect,Guide] 7935,7936,7937,7938,7939,7940
					return new PatchState(Produce.APPLE, CropState.GROWING, value - 8, Action.WAIT);
				}
				if (value >= 14 && value <= 20)
				{
					// Apple tree[Chop-down,Inspect,Guide,Pick-apple] 7941,7942,7943,7944,7945,7946,...
					return new PatchState(Produce.APPLE, CropState.HARVESTABLE, value - 14, Action.CHOP);
				}
				if (value >= 21 && value <= 26)
				{
					// Diseased apple tree[Prune,Inspect,Guide] 7949,7950,7951,7952,7953,7954
					return new PatchState(Produce.APPLE, CropState.DISEASED, value - 20, Action.PRUNE);
				}
				if (value >= 27 && value <= 32)
				{
					// Dead apple tree[Clear,Inspect,Guide] 7955,7956,7957,7958,7959,7960
					return new PatchState(Produce.APPLE, CropState.DEAD, value - 26, Action.CLEAR);
				}
				if (value == 33)
				{
					// Apple tree stump[Clear,Inspect,Guide] 7961
					return new PatchState(Produce.APPLE, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 34)
				{
					// Apple tree[Check-health,Inspect,Guide] 7948
					return new PatchState(Produce.APPLE, CropState.GROWING, Produce.APPLE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 35 && value <= 40)
				{
					// Banana tree[Inspect,Guide] 7993,7994,7995,7996,7997,7998
					return new PatchState(Produce.BANANA, CropState.GROWING, value - 35, Action.WAIT);
				}
				if (value >= 41 && value <= 47)
				{
					// Banana tree[Chop-down,Inspect,Guide,Pick-banana] 8000,8001,8002,8003,8004,8005,...
					return new PatchState(Produce.BANANA, CropState.HARVESTABLE, value - 41, Action.CHOP);
				}
				if (value >= 48 && value <= 53)
				{
					// Diseased banana tree[Prune,Inspect,Guide] 8007,8008,8009,8010,8011,8012
					return new PatchState(Produce.BANANA, CropState.DISEASED, value - 47, Action.PRUNE);
				}
				if (value >= 54 && value <= 59)
				{
					// Dead banana tree[Clear,Inspect,Guide] 8013,8014,8015,8016,8017,8018
					return new PatchState(Produce.BANANA, CropState.DEAD, value - 53, Action.CLEAR);
				}
				if (value == 60)
				{
					// Banana tree stump[Clear,Inspect,Guide] 8019
					return new PatchState(Produce.BANANA, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 61)
				{
					// Banana tree[Check-health,Inspect,Guide] 7999
					return new PatchState(Produce.BANANA, CropState.GROWING, Produce.BANANA.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 62 && value <= 71)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8050,8050,8050,8050,8050,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 72 && value <= 77)
				{
					// Orange tree[Inspect,Guide] 8051,8052,8053,8054,8055,8056
					return new PatchState(Produce.ORANGE, CropState.GROWING, value - 72, Action.WAIT);
				}
				if (value >= 78 && value <= 84)
				{
					// Orange tree[Chop-down,Inspect,Guide,Pick-orange] 8057,8058,8059,8060,8061,8062,...
					return new PatchState(Produce.ORANGE, CropState.HARVESTABLE, value - 78, Action.CHOP);
				}
				if (value >= 85 && value <= 89)
				{
					// Diseased orange tree[Prune,Inspect,Guide] 8065,8066,8067,8068,8069
					return new PatchState(Produce.ORANGE, CropState.DISEASED, value - 84, Action.PRUNE);
				}
				if (value == 90)
				{
					// Diseased orange tree[Chop-down,Inspect,Guide] 8070
					return new PatchState(Produce.ORANGE, CropState.DISEASED, 6, Action.CHOP);
				}
				if (value >= 91 && value <= 96)
				{
					// Dead orange tree[Clear,Inspect,Guide] 8071,8072,8073,8074,8075,8076
					return new PatchState(Produce.ORANGE, CropState.DEAD, value - 90, Action.CLEAR);
				}
				if (value == 97)
				{
					// Orange tree stump[Clear,Inspect,Guide] 8077
					return new PatchState(Produce.ORANGE, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 98)
				{
					// Orange tree[Check-health,Inspect,Guide] 8064
					return new PatchState(Produce.ORANGE, CropState.GROWING, Produce.ORANGE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 99 && value <= 104)
				{
					// Curry tree[Inspect,Guide] 8020,8021,8022,8023,8024,8025
					return new PatchState(Produce.CURRY, CropState.GROWING, value - 99, Action.WAIT);
				}
				if (value >= 105 && value <= 111)
				{
					// Curry tree[Chop-down,Inspect,Guide,Pick-leaf] 8026,8027,8028,8029,8030,8031,...
					return new PatchState(Produce.CURRY, CropState.HARVESTABLE, value - 105, Action.CHOP);
				}
				if (value >= 112 && value <= 117)
				{
					// Diseased curry tree[Prune,Inspect,Guide] 8034,8035,8036,8037,8038,8039
					return new PatchState(Produce.CURRY, CropState.DISEASED, value - 111, Action.PRUNE);
				}
				if (value >= 118 && value <= 123)
				{
					// Dead curry tree[Clear,Inspect,Guide] 8040,8041,8042,8043,8044,8045
					return new PatchState(Produce.CURRY, CropState.DEAD, value - 117, Action.CLEAR);
				}
				if (value == 124)
				{
					// Curry tree stump[Clear,Inspect,Guide] 8046
					return new PatchState(Produce.CURRY, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 125)
				{
					// Curry tree[Check-health,Inspect,Guide] 8033
					return new PatchState(Produce.CURRY, CropState.GROWING, Produce.CURRY.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 126 && value <= 135)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8050,8050,8050,8050,8050,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 136 && value <= 141)
				{
					// Pineapple plant[Inspect,Guide] 7966,7967,7968,7969,7970,7971
					return new PatchState(Produce.PINEAPPLE, CropState.GROWING, value - 136, Action.WAIT);
				}
				if (value >= 142 && value <= 148)
				{
					// Pineapple plant[Chop down,Inspect,Guide,Pick-pineapple] 7972,7973,7974,7975,7976,7977,...
					return new PatchState(Produce.PINEAPPLE, CropState.HARVESTABLE, value - 142, Action.CHOP);
				}
				if (value >= 149 && value <= 154)
				{
					// Diseased pineapple plant[Prune,Inspect,Guide] 7980,7981,7982,7983,7984,7985
					return new PatchState(Produce.PINEAPPLE, CropState.DISEASED, value - 148, Action.PRUNE);
				}
				if (value >= 155 && value <= 160)
				{
					// Dead pineapple plant[Clear,Inspect,Guide] 7986,7987,7988,7989,7990,7991
					return new PatchState(Produce.PINEAPPLE, CropState.DEAD, value - 154, Action.CLEAR);
				}
				if (value == 161)
				{
					// Pineapple plant stump[Clear,Inspect,Guide] 7992
					return new PatchState(Produce.PINEAPPLE, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 162)
				{
					// Pineapple plant[Check-health,Inspect,Guide] 7979
					return new PatchState(Produce.PINEAPPLE, CropState.GROWING, Produce.PINEAPPLE.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 163 && value <= 168)
				{
					// Papaya tree[Inspect,Guide] 8105,8106,8107,8108,8109,8110
					return new PatchState(Produce.PAPAYA, CropState.GROWING, value - 163, Action.WAIT);
				}
				if (value >= 169 && value <= 175)
				{
					// Papaya tree[Chop-down,Inspect,Guide,Pick-fruit] 8111,8112,8113,8114,8115,8116,...
					return new PatchState(Produce.PAPAYA, CropState.HARVESTABLE, value - 169, Action.CHOP);
				}
				if (value >= 176 && value <= 181)
				{
					// Diseased papaya tree[Prune,Inspect,Guide] 8119,8120,8121,8122,8123,8124
					return new PatchState(Produce.PAPAYA, CropState.DISEASED, value - 175, Action.PRUNE);
				}
				if (value >= 182 && value <= 187)
				{
					// Dead papaya tree[Clear,Inspect,Guide] 8125,8126,8127,8128,8129,8130
					return new PatchState(Produce.PAPAYA, CropState.DEAD, value - 181, Action.CLEAR);
				}
				if (value == 188)
				{
					// Papaya tree stump[Clear,Inspect,Guide] 8131
					return new PatchState(Produce.PAPAYA, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 189)
				{
					// Papaya tree[Check-health,Inspect,Guide] 8118
					return new PatchState(Produce.PAPAYA, CropState.GROWING, Produce.PAPAYA.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 190 && value <= 199)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8050,8050,8050,8050,8050,...
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				if (value >= 200 && value <= 205)
				{
					// Palm tree[Inspect,Guide] 8078,8079,8080,8081,8082,8083
					return new PatchState(Produce.PALM, CropState.GROWING, value - 200, Action.WAIT);
				}
				if (value >= 206 && value <= 212)
				{
					// Palm tree[Chop-down,Inspect,Guide,Pick-coconut] 8084,8085,8086,8087,8088,8089,...
					return new PatchState(Produce.PALM, CropState.HARVESTABLE, value - 206, Action.CHOP);
				}
				if (value >= 213 && value <= 218)
				{
					// Diseased palm tree[Prune,Inspect,Guide] 8092,8093,8094,8095,8096,8097
					return new PatchState(Produce.PALM, CropState.DISEASED, value - 212, Action.PRUNE);
				}
				if (value >= 219 && value <= 224)
				{
					// Dead palm tree[Clear,Inspect,Guide] 8098,8099,8100,8101,8102,8103
					return new PatchState(Produce.PALM, CropState.DEAD, value - 218, Action.CLEAR);
				}
				if (value == 225)
				{
					// Palm tree stump[Clear,Inspect,Guide] 8104
					return new PatchState(Produce.PALM, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 226)
				{
					// Palm tree[Check-health,Inspect,Guide] 8091
					return new PatchState(Produce.PALM, CropState.GROWING, Produce.PALM.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 227 && value <= 232)
				{
					// Dragonfruit tree[Inspect,Guide] 34008,34009,34010,34011,34012,34013
					return new PatchState(Produce.DRAGONFRUIT, CropState.GROWING, value - 227, Action.WAIT);
				}
				if (value >= 233 && value <= 239)
				{
					// Dragonfruit tree[Chop down,Inspect,Guide,Pick-dragonfruit] 34014,34015,34016,34017,34018,34019,...
					return new PatchState(Produce.DRAGONFRUIT, CropState.HARVESTABLE, value - 233, Action.CHOP);
				}
				if (value >= 240 && value <= 245)
				{
					// Diseased dragonfruit plant[Prune,Inspect,Guide] 34022,34023,34024,34025,34026,34027
					return new PatchState(Produce.DRAGONFRUIT, CropState.DISEASED, value - 239, Action.PRUNE);
				}
				if (value >= 246 && value <= 251)
				{
					// Dead dragonfruit plant[Clear,Inspect,Guide] 34028,34029,34030,34031,34032,34033
					return new PatchState(Produce.DRAGONFRUIT, CropState.DEAD, value - 245, Action.CLEAR);
				}
				if (value == 252)
				{
					// Dragonfruit tree stump[Clear,Inspect,Guide] 34034
					return new PatchState(Produce.DRAGONFRUIT, CropState.HARVESTABLE, 0, Action.CLEAR);
				}
				if (value == 253)
				{
					// Dragonfruit tree[Check-health,Inspect,Guide] 34021
					return new PatchState(Produce.DRAGONFRUIT, CropState.GROWING, Produce.DRAGONFRUIT.getStages() - 1, Action.CHECK_HEALTH);
				}
				if (value >= 254 && value <= 255)
				{
					// Fruit Tree Patch[Rake,Inspect,Guide] 8050,8050
					return new PatchState(Produce.WEEDS, CropState.GROWING, 3, Action.RAKE);
				}
				return null;
			}
		};

	/** The corals in the order their varbit values come in. */
	private static final Produce[] CORALS = {Produce.ELKHORN_CORAL, Produce.PILLAR_CORAL, Produce.UMBRAL_CORAL};

	/** The patch's state for a varbit value, or null for a value the game never sends. */
	public abstract PatchState forVarbitValue(int value);

	private final String name;

	private final boolean healthCheckRequired;

	PatchKind(String name, boolean healthCheckRequired)
	{
		this.name = name;
		this.healthCheckRequired = healthCheckRequired;
	}

	public String getName()
	{
		return name;
	}

	public boolean isHealthCheckRequired()
	{
		return healthCheckRequired;
	}
}
