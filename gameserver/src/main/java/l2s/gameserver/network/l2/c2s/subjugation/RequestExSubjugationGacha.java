package l2s.gameserver.network.l2.c2s.subjugation;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.s2c.subjugation.ExSubjugationGacha;
import l2s.gameserver.network.l2.s2c.subjugation.ExSubjugationSidebar;

/**
 * @author nexvill
 */
public class RequestExSubjugationGacha extends L2GameClientPacket
{
	int _zoneId, _count;
	@Override
	protected boolean readImpl()
	{
		_zoneId = readD(); // zone id
		_count = readD(); // keys used
		return true;
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		
		player.sendPacket(new ExSubjugationGacha(_zoneId, _count));
		player.sendPacket(new ExSubjugationSidebar(player));
		
		int keysUsed = player.getVarInt(PlayerVariables.SUBJUGATION_ZONE_KEYS + "_" + _zoneId, 0);
		player.setVar(PlayerVariables.SUBJUGATION_ZONE_KEYS + "_" + _zoneId, keysUsed);
	}
}