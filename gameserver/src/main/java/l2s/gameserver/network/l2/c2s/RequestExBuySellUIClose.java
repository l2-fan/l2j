package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestExBuySellUIClose extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setBuyListId(0);
		activeChar.sendItemList(true);
	}
}