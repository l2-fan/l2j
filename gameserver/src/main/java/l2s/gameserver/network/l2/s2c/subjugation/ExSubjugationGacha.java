package l2s.gameserver.network.l2.s2c.subjugation;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SubjugationsHolder;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.templates.SubjugationTemplate;

/**
 * @author nexvill
 */
public class ExSubjugationGacha extends L2GameServerPacket
{
	int _zoneId, _count;
	public ExSubjugationGacha(int zoneId, int keysCount)
	{
		_zoneId = zoneId;
		_count = keysCount;
	}

	@Override
	protected final void writeImpl()
	{
		SubjugationTemplate temp = SubjugationsHolder.getInstance().getFields().get(_zoneId);
		int[] rewardCount = new int[6];
		for (int i = 0; i < _count; i++)
		{
			int chance = Rnd.get(10000);
			if (chance > 4000)
				rewardCount[0]++;
			else if (chance > 1000)
				rewardCount[1]++;
			else if (chance > 500)
				rewardCount[2]++;
			else if (chance > 300)
				rewardCount[3]++;
			else if (chance > 50)
				rewardCount[4]++;
			else if (chance > 25)
				rewardCount[5]++;
		}
		
		writeD(_count);
		if (rewardCount[0] > 0)
		{
			writeD(temp.getRewardItems()[0].getId());
			writeD(rewardCount[0]);
		}
		if (rewardCount[1] > 0)
		{
			writeD(temp.getRewardItems()[1].getId());
			writeD(rewardCount[1]);
		}
		if (rewardCount[2] > 0)
		{
			writeD(temp.getRewardItems()[2].getId());
			writeD(rewardCount[2]);
		}
		if (rewardCount[3] > 0)
		{
			writeD(temp.getRewardItems()[3].getId());
			writeD(rewardCount[3]);
		}
		if (rewardCount[4] > 0)
		{
			writeD(temp.getRewardItems()[4].getId());
			writeD(rewardCount[4]);
		}
		if (rewardCount[5] > 0)
		{
			writeD(temp.getRewardItems()[5].getId());
			writeD(rewardCount[5]);
		}
	}
}