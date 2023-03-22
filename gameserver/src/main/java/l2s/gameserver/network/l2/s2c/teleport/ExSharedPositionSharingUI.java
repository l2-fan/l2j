package l2s.gameserver.network.l2.s2c.teleport;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExSharedPositionSharingUI extends L2GameServerPacket
{

	public ExSharedPositionSharingUI(Player player)
	{
	}

	@Override
	protected final void writeImpl()
	{
		writeQ(Config.SHARE_POSITION_COST);
	}
}