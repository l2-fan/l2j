package l2s.gameserver.network.l2.s2c.spectating;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Spectating;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

/**
 * @author nexvill
 */
public class ExUserWatcherTargetList extends L2GameServerPacket
{
	private Spectating[] _spectatings;
	
	public ExUserWatcherTargetList(Player player)
	{
		_spectatings = player.getSpectatingList().values();
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_spectatings.length);
		for (Spectating s : _spectatings)
		{
			writeString(s.getName());
			writeD(Config.REQUEST_ID); // server id
			writeD(s.getLevel());
			writeD(s.getClassId());
			writeC(s.isOnline());
		}
		
		//@formatter:off
		/*
		int count = 0;
		writeD(count);
		for (int i = 0; i < count; i++)
		{
			writeString(""); // name
			writeD(Config.REQUEST_ID); // Server Id
			writeD(1); // level
			writeD(1); // class id
			writeC(0); // online?
		}
		*/
	}
}