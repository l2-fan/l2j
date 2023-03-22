package l2s.gameserver.network.l2.s2c.subjugation;

import l2s.gameserver.data.xml.holder.SubjugationsHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExSubjugationList extends L2GameServerPacket
{
	private Player _player;
	public ExSubjugationList(Player player)
	{
		_player = player;
	}

	@Override
	protected final void writeImpl()
	{
		int count = 0;
		if (_player.getLevel() > 85)
			count = 6;
		else if (_player.getLevel() > 83)
			count = 5;
		else if (_player.getLevel() > 82)
			count = 4;
		else if (_player.getLevel() > 81)
			count = 3;
		else if (_player.getLevel() > 79)
			count = 2;
		
		writeD(count);
		for (int i = 0; i < count; i++)
		{
			int zoneId = i + 1;
			int points = _player.getVarInt(PlayerVariables.SUBJUGATION_ZONE_POINTS + "_" + zoneId, 0);
			int keysHave = points / 1000000 - _player.getVarInt(PlayerVariables.SUBJUGATION_ZONE_KEYS + "_" + zoneId, 0);
			points %= 1000000;
			int maximumKeys = SubjugationsHolder.getInstance().getFields().get(zoneId).getMaximumKeys() - keysHave;
			
			writeD(zoneId);
			writeD(points);
			writeD(keysHave);
			writeD(maximumKeys);
		}
	}
}