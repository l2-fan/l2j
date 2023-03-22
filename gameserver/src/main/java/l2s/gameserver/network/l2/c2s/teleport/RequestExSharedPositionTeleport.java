package l2s.gameserver.network.l2.c2s.teleport;

import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestExSharedPositionTeleport extends L2GameClientPacket
{
	private int _allow, _tpId;
	
	@Override
	protected boolean readImpl()
	{
		_allow = readC();
		_tpId = readH();
		readC(); // ?
		return true;
	}

	@Override
	protected void runImpl()
	{
		if (_allow == 1)
		{
			Player player = getClient().getActiveChar();
			
			ItemInstance l2coin = player.getInventory().getItemByItemId(ItemTemplate.ITEM_ID_MONEY_L);
			
			if ((l2coin == null) || (l2coin.getCount() < 400))
			{
				player.sendPacket(new SystemMessage(SystemMsg.NOT_ENOUGH_L2_COINS));
				return;
			}
			
			if (player.getInventory().destroyItem(l2coin, 400))
			{
				int x = ServerVariables.getInt("tpId_" + _tpId + "_x");
				int y = ServerVariables.getInt("tpId_" + _tpId + "_y");
				int z = ServerVariables.getInt("tpId_" + _tpId + "_z");
				player.teleToLocation(x, y, z);
				int tpCounts = player.getVarInt(PlayerVariables.SHARED_POSITION_TELEPORTS, 5) - 1;
				player.setVar(PlayerVariables.SHARED_POSITION_TELEPORTS, tpCounts);
				player.sendPacket(SystemMessagePacket.removeItems(ItemTemplate.ITEM_ID_MONEY_L, 400));
			}
		}
	}
}