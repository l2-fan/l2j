package l2s.gameserver.core;

import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.botscript.bypasshandler.BotTransactionBank;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;

public class TimerManager {
    //时间间隔
    private static final long PERIOD_DAY = 24 * 60 * 60 * 1000;

    private static TimerManager _instance = new TimerManager();

    public static TimerManager getInstance()
    {
        return _instance;
    }

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

    public int time;

    public Timer timer =null;

    public TimerManager() {
        time = 10;
    };

    // 执行方法
    public void statImplement() {

        // 定时执行重置的时间
        Date date = RestartServerHangUpTime.updateTime;

        //如果第一次执行定时任务的时间 小于 当前的时间
        if (date.before(new Date())) {
            date = this.addDay(date, 1);
        }
        Timer timer = new Timer();
        HangUpRenewTimerTask task = new HangUpRenewTimerTask();
        timer.schedule(task,date,PERIOD_DAY);
    }


    // 增加或减少天数
    public Date addDay(Date date, int num) {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DAY_OF_MONTH, num);
        return startDT.getTime();
    }

    public void startRenewCacheTime(Player player){
        player._isInPlugIn = true;/*啟動內掛減少收益*/
        /* 定时更新缓存中的挂机剩余时间 */
        Timer timer = new Timer();
        HangUpRenewCacheTimerTask task = new HangUpRenewCacheTimerTask(player,timer);
        //每5分钟从缓存中去更新倒数据库。
        timer.schedule(task,300000L,300000L);
    }

    // 汇率10S计时
    public void rateRenewTime(boolean renew,String kind,Player player){
        Timer newTimer = new Timer();
        if(!renew && timer != null){
            // true 不更新 ,false 更新
            timer.cancel();
            _log.info("thread"+Thread.currentThread().getId()+"stop");
        }
        if (renew) {
            //10S
            newTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    time--;
                    if(time <0){
                        time =10;
                        TransactionBankCount.exchangeRate = TransactionBankCount.getInstance().read();
//                        // 如果没有传参数 就是transactionBank.htm
//                        if(kind == null){
//                            BotControlPage.transactionBank(player);
//                        }else if(kind.equals("chooseGoldExchange") || kind.equals("chooseVipGoldExchange") ){
//                            // 如果是选择有参数就是
//                            BotControlPage.chooseExchange(player,kind);
//                        }
                    }
                }
            }, 1000L, 1000L);
            timer =newTimer;
        }
    }
}
