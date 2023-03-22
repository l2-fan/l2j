package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExClaschangeSetAlarm;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public class RequestExRequestClassChange extends L2GameClientPacket
{
	private int _classId;

	@Override
	protected boolean readImpl()
	{
		_classId = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		ClassId classId = ClassId.valueOf(_classId);
		if(classId == null)
		{
			player.sendActionFailed();
			return;
		}

		if(!player.canClassChange())
		{
			player.sendActionFailed();
			return;
		}
		
		if (!player.isInPeaceZone() || player.isInCombat())
		{
			player.sendActionFailed();
			player.sendPacket(ExClaschangeSetAlarm.STATIC);
			return;
		}

		if(!player.setClassId(classId.getId(), false))
		{
			player.sendActionFailed();
			return;
		}
		
		if (player.getClassLevel().ordinal() == ClassLevel.SECOND.ordinal())
		{
			player.checkElementalInfo();
		}

		player.sendPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
		player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);
		player.broadcastUserInfo(true);
	}
}
