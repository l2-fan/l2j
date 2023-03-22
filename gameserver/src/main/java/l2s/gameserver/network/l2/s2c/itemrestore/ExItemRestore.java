package l2s.gameserver.network.l2.s2c.itemrestore;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExItemRestore extends L2GameServerPacket
{
	public ExItemRestore()
	{
		
	}

	@Override
	protected final void writeImpl()
	{
		writeC(1);
	}
}