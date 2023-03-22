package l2s.gameserver.network.l2.s2c.subjugation;

import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExSubjugationGachaUI extends L2GameServerPacket
{
	private int _keys;
	public ExSubjugationGachaUI(int keys)
	{
		_keys = keys;
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_keys); // keys
	}
}