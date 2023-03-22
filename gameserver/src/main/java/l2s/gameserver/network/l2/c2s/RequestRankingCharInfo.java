package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExRankingCharInfo;

/**
 * @author JoeAlisson
 */
public class RequestRankingCharInfo extends L2GameClientPacket 
{

	@Override
	protected boolean readImpl() 
	{
		readC(); // unk
		return true;
	}

	@Override
	protected void runImpl() 
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.sendPacket(new ExRankingCharInfo(activeChar));
	}
}