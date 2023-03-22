package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotResType;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import java.util.LinkedList;


import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.pair.impl.IntObjectPairImpl;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;

public class BotRes implements IBotActionHandler
{
	int[] blessScrollIds = new int[] { 29030, 29700, 49084, 49526, 49542, 3936 };
	/*物品ID29700 微祝福的復活卷軸	物品ID49084 高級復活卷軸	物品ID49526 高級復活卷軸	物品ID49542 高級復活卷軸	物品ID3936 祝福的復活卷軸*/
	int scrollId = 737;
	/*物品ID737 復活卷軸*/
	int skillId = 1016;
	/*技能ID1016 返生術*/
	private static /* synthetic */ int[] $SWITCH_TABLE$core$BotResType;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isUseRes())
		{
			return false;
		}
		if(isSitting || this.isActionsDisabledExcludeAttack(actor))
		{
			return false;
		}
		Party party = actor.getParty();
		if(party == null)
		{
			return false;
		}
		for(Player player : party)
		{
			if(!player.isDead())
				continue;
			for(BotResType botResType : config.getResType())
			{
				switch($SWITCH_TABLE$core$BotResType()[botResType.ordinal()])
				{
					case 1:
					{
						for(int itemId : blessScrollIds)
						//先使用祝福卷
						{
							if(!ItemFunctions.haveItem(actor, itemId, 1))
								continue;
							actor.setTarget(player);
							ItemInstance scroll = actor.getInventory().getItemByItemId(itemId);
							actor.useItem(scroll, false, false);
							return true;
						}
						break;
					}
					case 2:
					//"返生术
					{
						SkillEntry skillEntry = actor.getKnownSkill(this.skillId);
						if(skillEntry == null || actor.isSkillDisabled(skillEntry.getTemplate()) || !BotThinkTask.checkSkillMpCost(actor, skillEntry) || !skillEntry.checkCondition(actor, player, false, false, false))
							continue;
						actor.setTarget(player);
						actor.getAI().Cast(skillEntry, player, false, false);
						return true;
					}
					case 3:
					//"复活卷
					{
						if(!ItemFunctions.haveItem(actor, scrollId, 1))
							return false;
						actor.setTarget(player);
						ItemInstance scroll = actor.getInventory().getItemByItemId(scrollId);
						actor.useItem(scroll, false, false);
						return true;
					}
				}
			}
		}
		return false;
	}
	static int[] $SWITCH_TABLE$core$BotResType()
	{
		if($SWITCH_TABLE$core$BotResType != null)
		{
			return $SWITCH_TABLE$core$BotResType;
		}
		int[] arrn = new int[BotResType.values().length];
		try
		{
			arrn[BotResType.BLESSED.ordinal()] = 1;//BLESSED("祝福的复活卷")
			arrn[BotResType.MAGIC.ordinal()] = 2;//MAGIC("返生术")
			arrn[BotResType.DEFAULT.ordinal()] = 3;//DEFAULT("复活卷")
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$core$BotResType = arrn;
		return $SWITCH_TABLE$core$BotResType;
	}
}