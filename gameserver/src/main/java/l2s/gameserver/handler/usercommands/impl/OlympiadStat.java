package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadMatchMakingResult;
import l2s.gameserver.network.l2.s2c.olympiad.ExOlympiadRecord;

/**
 * Support for /olympiadstat command
 */
public class OlympiadStat implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 109 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;

		if(activeChar == null || !activeChar.isPlayer() || activeChar.getPlayer().getClassLevel().ordinal() < ClassLevel.SECOND.ordinal() || activeChar.getPlayer().getLevel() < 70)
		{
			activeChar.sendPacket(SystemMsg.ONLY_CHARACTERS_OF_LEVEL_70_OR_HIGHER_WHO_HAVE_COMPLETED_THE_2ND_CLASS_TRANSFER_CAN_USE_THIS_COMMAND);
			return true;
		}

		SystemMessagePacket sm = new SystemMessagePacket(SystemMsg.FOR_THE_CURRENT_GRAND_OLYMPIAD_YOU_HAVE_PARTICIPATED_IN_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_CURRENTLY_HAVE_S4_OLYMPIAD_POINTS);
		sm.addInteger(Olympiad.getCompetitionDone(activeChar.getObjectId()));
		sm.addInteger(Olympiad.getCompetitionWin(activeChar.getObjectId()));
		sm.addInteger(Olympiad.getCompetitionLoose(activeChar.getObjectId()));
		sm.addInteger(Olympiad.getParticipantPoints(activeChar.getObjectId()));

		activeChar.sendPacket(sm);

		int[] ar = Olympiad.getDailyGameCounts(activeChar.getObjectId());
		sm = new SystemMessagePacket(SystemMsg.YOU_CAN_PARTICIPATE_IN_A_TOTAL_OF_S1_MATCHES_TODAY);
		sm.addInteger(ar[0]);
		activeChar.sendPacket(sm);
		
		activeChar.sendPacket(new ExOlympiadRecord(activeChar));
		if (Olympiad.isRegistered(activeChar))
			activeChar.sendPacket(new ExOlympiadMatchMakingResult(true));
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}