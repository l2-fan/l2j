package l2s.gameserver.core;

import java.lang.ref.SoftReference;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.SkillEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BotThinkTask implements Runnable
{

	private static final Logger LOG = LoggerFactory.getLogger(BotThinkTask.class);
	private final SoftReference actor;

	/* member class not found */
	class Task
	{
	}

	public BotThinkTask(Player actor)
	{
		this.actor = new SoftReference(actor);
 	}

	public void run()
	{
		Player actor;
		BotConfig config;
		actor = (Player) this.actor.get();
		if(actor == null)
			return;
		config = BotEngine.getInstance().getBotConfig(actor);

        if(BotEngine.getInstance().getRunTimeChecker().test(actor))
        {
            if(!config.isAbort()){
				ThreadPoolManager.getInstance().schedule(this, 1000L);
			}
            else
            if(actor != null)
                BotEngine.getInstance().stopTask(actor);
            if(actor != null)
                config.releaseMemory(actor);
            return;
        }
		try
		{
			for(IBotActionHandler actionHandler : BotActionHandler.getInstance().getData().values())
			{
		        if(actionHandler.test(actor))
		        {
					if(!config.isAbort())
					{
						ThreadPoolManager.getInstance().schedule(this, 1000L);
						return;
					}
					else
					{
						if(actor != null)
							BotEngine.getInstance().stopTask(actor);
						if(actor != null)
							config.releaseMemory(actor);
					}				
		        }
			}
		}
		catch(Exception e)
		{
			config.setAbort(true, "内挂启动失败，请联系管理员检查");
			LOG.error("内挂执行过程中出现异常:", e);
		}
		Integer time = Integer.valueOf(BotEngine.leftTimeMap.get(String.valueOf(actor.getObjectId())));
		if (actor._isInPlugIn && time>0 ) {
			time--;
			/** 存储剩余时间的map 中更新时间 */
			BotEngine.leftTimeMap.put(String.valueOf(actor.getObjectId()),String.valueOf(time));
		}
		if (time == 0) {
			BotEngine.getInstance().stopTask(actor);
			BotConfig botConfig = BotEngine.getInstance().getBotConfig(actor);
			botConfig.setAbort(true, "");
			return;
		}

		/* 当前角色所在组的队长是否死亡，死亡 并且队长停止挂机时 停止挂机 */
		if (!actor.isDead()) {
			Party party = actor.getParty();
			if(party !=null){
				Player leader = party.getPartyLeader();
				if(leader != null && leader.isDead() && !leader._isInPlugIn){
					BotEngine.getInstance().stopTask(actor);
					BotConfig botConfig = BotEngine.getInstance().getBotConfig(actor);
					botConfig.setAbort(true, "");
					return;
				}
			}
		}
		if(!config.isAbort())
		{
			ThreadPoolManager.getInstance().schedule(this, 1000L);
			return;
		}
		else
		{
			if(actor != null)
				BotEngine.getInstance().stopTask(actor);
			if(actor != null)
				config.releaseMemory(actor);
		}
	}

	public static boolean checkSkillMpCost(Player actor, SkillEntry skill)
	{
		return actor.getCurrentMp() >= skill.getTemplate().getMpConsume();
	}
}
