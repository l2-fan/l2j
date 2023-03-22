package l2s.gameserver.core;

import l2s.gameserver.botscript.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.model.Party;
import l2s.gameserver.skills.SkillEntry;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class BotEngine
{
    private static final Logger LOG = LoggerFactory.getLogger(BotEngine.class);
    private static final BotEngine INSTANCE = new BotEngine();
    private IBotConfigDAO dao;
    private IBotRuntimeChecker runTimeChecker;
    private  Map<Integer, ScheduledFuture<?>> tasks;
    private  Map<Integer, ScheduledFuture<?>> renewTasks;

    private Lock switchLock;
    private Map<Integer, BotConfig> configs;
	private SkillEntry _effectSkill;
    public static Map<String,String> leftTimeMap ;
    public static String leftTime = null;
    public static String scriptRemainderTime;
    public static boolean runtimeHuangUpTimeLock = false;

    /* member class not found */
    class Task {}

    public Map<Integer, BotConfig> getConfigs()
    {
        return configs;
    }

    protected BotEngine()
    {
        tasks = new HashMap<Integer, ScheduledFuture<?>>();
        renewTasks = new HashMap<Integer, ScheduledFuture<?>>();
        switchLock = new ReentrantLock();
        configs = new HashMap<Integer, BotConfig>();
        //init();
    }

    public synchronized void init()
    {
        BotScriptsLoader.load();
        dao = new BotConfigDAO();
        runTimeChecker = new BotRuntimeChecker();
        LOG.info("\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F!");
		/*\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F! 挂机系统加载成功!*/
    }

    public IBotConfigDAO getDao()
    {
        return dao;
    }

    public IBotRuntimeChecker getRunTimeChecker()
    {
        return runTimeChecker;
    }

    public BotConfig getBotConfig(Player player)
    {
        BotConfig config = configs.get(Integer.valueOf(player.getObjectId()));
        if(config == null)
        {
            config = new BotConfigImp();
            configs.put(Integer.valueOf(player.getObjectId()), config);
        }
        return configs.get(Integer.valueOf(player.getObjectId()));
    }
    public void startBotTask(Player player)
    {
		/*pvp活动禁用内挂*/
    	if (player.isInPvPEvent())
    	{
    		player.sendMessage("活动中无法开启内挂..");
    		return;
    	}
        /*内挂剩余时间为0禁用*/
        if (Integer.parseInt(BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()))) == 0) {
            player.sendMessage("当前内挂剩余使用时间为0,无法开启内挂..");
            return;
        }
		/*pvp活动禁用内挂*/
		switchLock.lock();
		try
		{
			ScheduledFuture<?> botThinkTask = tasks.get(player.getObjectId());
            if(botThinkTask == null)
			{
                player._isInPlugIn = true;/*啟動內掛減少收益*/
				botThinkTask = ThreadPoolManager.getInstance().schedule(new BotThinkTask(player), 1000L);
				BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
				player.sendMessage("啟動輔助（52級以前免費使用），收益100%不減少...");
                botConfig.setStartX(player.getX());
                botConfig.setStartY(player.getY());
                botConfig.setStartZ(player.getZ());
                if(botConfig.getDeathTime() > 0)
                {
                    botConfig.setDeathTime(0);
                }
                botConfig.setAbort(false, "");
                tasks.put(player.getObjectId(), botThinkTask);
                player.broadcastCharInfo();
            }
            // 每5分钟将挂机剩余时间更新到数据库
            TimerManager.getInstance().startRenewCacheTime(player);
		}
		finally
		{
			switchLock.unlock();
		}
        //player.startAbnormalEffect(AbnormalEffect.UNK_222);//給與特殊效果標識
    }

    public void stopTask(Player player)
    {
		switchLock.lock();
		try
		{
			int objectId = player.getObjectId();
			Future<?> task = tasks.get(player.getObjectId());
			player._isInPlugIn = false;/*關閉內掛減少收益*/

			if(task != null)
			{
				task.cancel(false);
			}
			tasks.remove(objectId);
			player.sendMessage("中断辅助 - " + BotEngine.getInstance().getBotConfig(player).getAbortReason());
			player.broadcastCharInfo();
			Functions.show("<center>辅助已中断<br1>....</center>", player);
            /** 剩余时间放入数据库 */
            String lefttime = BotEngine.leftTimeMap.get(String.valueOf(player.getObjectId()));
            BotHangUpTimeDao.getInstance().updateHangUpTime(player.getObjectId(),Integer.parseInt(lefttime));
		}
		finally
		{
			switchLock.unlock();
		}

        //player.stopAbnormalEffect(AbnormalEffect.UNK_222);//給與特殊效果標識
    }

    public static BotEngine getInstance()
    {
        return INSTANCE;
    }
}