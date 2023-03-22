package l2s.gameserver.taskmanager.tasks;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.RankManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.ItemFunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author nexvill
**/
public class SubjugationStopTask extends AutomaticTask
{
	private static final Logger _log = LoggerFactory.getLogger(SubjugationStopTask.class);
	private static final int VARKA_SILENOS_PURGE_RANK_REWARD = 99300;
	private static final int KETRA_ORC_PURGE_RANK_REWARD = 99301;
	private static final int DRAGON_VALLEY_PURGE_RANK_REWARD = 99302;
	private static final int IMPERIAL_TOMB_PURGE_RANK_REWARD = 99299;
	private static final int ANTHARAS_LAIR_PURGE_RANK_REWARD = 99303;
	private static final int TOWER_OF_INSOLENCE_PURGE_RANK_REWARD = 99304;

	private static final SchedulingPattern PATTERN = new SchedulingPattern("00 12 * * 1");

	public SubjugationStopTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		_log.info("Subjugation Stop Global Task: launched.");
		Config.SUBJUGATION_ENABLED = false;
		for (int i = 1; i < 7; i++)
		{
			Map<Integer, StatsSet> ranking = RankManager.getInstance().getSubjugationRanks(i);
			int count = 5;
			for (int id : ranking.keySet())
			{
				StatsSet player = ranking.get(id);
				Mail mail = new Mail();
				mail.setSenderId(1);
				mail.setSenderName("Subjugation System");
				mail.setReceiverId(player.getInteger("charId"));
				mail.setReceiverName(player.getString("name"));
				mail.setTopic("Subjugation Reward");
				mail.setBody("Subjugation Reward to top5 rankers");
					
				ItemInstance item = null;
				if (i == 1)
					item = ItemFunctions.createItem(VARKA_SILENOS_PURGE_RANK_REWARD);
				else if (i == 2)
					item = ItemFunctions.createItem(KETRA_ORC_PURGE_RANK_REWARD);
				else if (i == 3)
					item = ItemFunctions.createItem(DRAGON_VALLEY_PURGE_RANK_REWARD);
				else if (i == 4)
					item = ItemFunctions.createItem(IMPERIAL_TOMB_PURGE_RANK_REWARD);
				else if (i == 5)
					item = ItemFunctions.createItem(ANTHARAS_LAIR_PURGE_RANK_REWARD);
				else if (i == 6)
					item = ItemFunctions.createItem(TOWER_OF_INSOLENCE_PURGE_RANK_REWARD);
				if (item != null)
				{
					item.setLocation(ItemInstance.ItemLocation.MAIL);
					item.setCount(count);
					count--;
					item.save();
					
					mail.addAttachment(item);
					mail.setType(Mail.SenderType.BIRTHDAY);
					mail.setUnread(true);
					mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
					mail.save();
					
					Player plr = GameObjectsStorage.getPlayer(player.getInteger("charId"));
					if (plr != null)
					{
						plr.sendPacket(ExNoticePostArrived.STATIC_TRUE);
						plr.sendPacket(new ExUnReadMailCount(plr));
						plr.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
					}
				}
			}
		}
		
		_log.info("Subjugation Stop Global Task: completed.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}