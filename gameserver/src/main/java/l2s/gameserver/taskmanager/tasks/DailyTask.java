package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.database.mysql;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.instancemanager.TrainingCampManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class DailyTask extends AutomaticTask
{
	private static final Logger _log = LoggerFactory.getLogger(DailyTask.class);

	public DailyTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		_log.info("Daily Global Task: launched.");
		for(Player player : GameObjectsStorage.getPlayers(true, true))
			player.restartDailyCounters(false);
		ClanTable.getInstance().refreshClanAttendanceInfo();
		TrainingCampManager.getInstance().refreshTrainingCamp();
		mysql.set("DELETE FROM `character_variables` WHERE `name` = \"donation_blocked\";");
		mysql.set("DELETE FROM `character_variables` WHERE `name` = \"donations_available\";");
		
		RankManager.getInstance().saveClanData();
		RankManager.getInstance().loadPreviousClanData();
		ServerVariables.set("lastClanUpdate", System.currentTimeMillis());
		
		_log.info("Daily Global Task: completed.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return TimeUtils.DAILY_DATE_PATTERN.next(System.currentTimeMillis());
	}
}