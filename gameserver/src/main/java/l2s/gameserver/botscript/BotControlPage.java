package l2s.gameserver.botscript;

import l2s.gameserver.botscript.bypasshandler.BotTransactionBank;
import l2s.gameserver.core.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.skillclasses.Summon;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.HtmlUtils;

public class BotControlPage
{

	public static void restPage(Player activeChar)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(activeChar);
		String html = HtmCache.getInstance().getHtml("bot/rest.htm", activeChar);
		html = html.replace("%followRest%", config.isFollowRest() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%idleRest%", config.isIdleRest() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%info%", config.getHpProtected() == 0 && config.getMpProtected() == 0 ? "\u4e0d\u4e3b\u52a8\u5750\u4e0b\u4f11\u606f" : "HP\u4f4e\u4e8e" + config.getHpProtected() + "%\u6216MP\u4f4e\u4e8e" + config.getMpProtected() + "\u65f6\u4f11\u606f");
		/*\u4e0d\u4e3b\u52a8\u5750\u4e0b\u4f11\u606f 不主动坐下休息	\u4f4e\u4e8e 低于	\u6216MP\u4f4e\u4e8e 或MP低于	\u65f6\u4f11\u606f 时休息*/		
		Functions.show( html, activeChar);
	}

	public static void mainPage(Player activeChar)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(activeChar);
		String html = HtmCache.getInstance().getHtml("bot/main.htm", activeChar);
		html = html.replace("%runtimeStats%", config.isAbort() ? "<font color=FF0000>\u25a0</font>" : "<font color=00FF00>\u25a0</font>");
		/*\u25a0 ■*/
		html = html.replace("%run%", config.isAbort() ? "start" : "stop");
		html = html.replace("%runbutton%", config.isAbort() ? "\u542f\u52a8" : "\u505c\u6b62");
		/*\u542f\u52a8 启动	\u505c\u6b62 停止*/
		html = html.replace("%autoAttack%", config.isAutoAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%pickUpItem%", config.isPickUpItem() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%pickUpFirst%", config.isPickUpFirst() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%autoSweep%", config.isAutoSweep() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%absorbBody%", config.isAbsorbBody() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%hpmpShift%", config.isHpmpShift() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%acceptRes%", config.isAcceptRes() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%usePhysicalAttack%", config.isUsePhysicalAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followMove%", config.isFollowMove() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followAttack%", config.isFollowAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%antidote%", config.isAntidote() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%bondage%", config.isBondage() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyAntidote%", config.isPartyAntidote() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyBondage%", config.isPartyBondage() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%partyParalysis%", config.isPartyParalysis() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%coverMember%", config.isCoverMember() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%followAttackWhenChoosed%", config.isFollowAttackWhenChoosed() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = html.replace("%hpmpshiftpercent%", String.valueOf(config.getHpShiftPercent() == 0 && config.getMpShiftPercent() == 0 ? "\u8a2d\u7f6e\u5fc3\u8f49" : "HP\u9ad8\u65bc" + config.getHpShiftPercent() + "%,MP\u4f4e\u65bc" + config.getMpShiftPercent() + "%\u4f7f\u7528"));
		html = html.replace("%scriptTime%","10\u5c0f\u65f6");
		String leftTime = BotEngine.leftTimeMap.get(String.valueOf(activeChar.getObjectId()));

		/** 新 或未使用内挂的老用户 */
		if (leftTime==null ) {
			/* 给缓存设置初始值 */
			BotEngine.leftTimeMap.put(String.valueOf(activeChar.getObjectId()),"36000");
			/* 数据库初始化挂机时间 */
			BotHangUpTimeDao.getInstance().insertScriptTime(activeChar.getObjectId(),Player.scriptTime,Player.scriptTime);
			leftTime = "36000";
		}
		Integer time = Integer.parseInt(leftTime);
		int hourTemplate = time / 3600;
		int minuteTemplate = (time - hourTemplate*3600 ) / 60;
		String hour = null;
		String minute = null;
		if(hourTemplate<10){
			hour = "0"+hourTemplate;
		}else {
			hour = String.valueOf(hourTemplate);
		}
		if(minuteTemplate<10){
			minute = "0"+minuteTemplate;
		}else {
			minute = String.valueOf(minuteTemplate);
		}
		html = html.replace("%scriptRemainderTime%", hour+"\u5c0f\u65f6"+minute+"\u5206\u949f");
		Functions.show( html,activeChar);
	}

	public static void fightPage(Player activeChar)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(activeChar);
		String html = HtmCache.getInstance().getHtml("bot/fight.htm", activeChar);
		Skill[] skills = activeChar.getAllSkills().stream().filter(Objects::nonNull).map(SkillEntry::getTemplate).filter(Skill::isActive).filter(Skill::isDebuff).filter(sk -> {
			for(BotSkillStrategy skillStrategy : BotEngine.getInstance().getBotConfig(activeChar).getAttackStrategy())
			{
				if(skillStrategy.getSkillId() != sk.getId())
					continue;
				return false;
			}
			return true;
		}).sorted(Comparator.comparing(Skill::getMagicLevel)).toArray(n -> new Skill[n]);
		StringBuilder skillList = new StringBuilder();
		for(int i = 1; i <= skills.length; ++i)
		{
			Skill skill;
			if(i % 3 == 1)
			{
				skillList.append("<tr>");
			}
			skillList.append("<td fixwidth=65>").append((skill = skills[i - 1]).getName().length() <= 5 ? skill.getName() : skill.getName().substring(0, 5)).append("</td>");
			skillList.append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.page skilledit " + skill.getId() + "\" width=16 height=15 back=\"Inventory_DF_Btn_Align_Down\" fore=\"Inventory_DF_Btn_Align\">").append("</td>");
			if(i % 3 != 0)
				continue;
			skillList.append("</tr>");
		}
		if(skills.length % 3 != 0)
		{
			skillList.append("</tr>");
		}
		html = html.replace("%skills%", skillList.toString());
		StringBuilder skillstrategys = new StringBuilder();
		skillstrategys.append("<table cellpadding=0 cellspacing=0>");
		int index = 0;
		for(BotSkillStrategy skillStrategy : config.getAttackStrategy())
		{
			skillstrategys.append("<tr>").append(skillStrategy.toTableTd()).append("<td width=18>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillOrderUp " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Up_Down\" fore=\"L2UI_CT1.Button_DF_Up\">").append("</td>").append("<td width=18>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillOrderDown " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Down_Down\" fore=\"L2UI_CT1.Button_DF_Down_Over\">").append("</td>").append("<td width=15>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.skillRemove " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			++index;
		}
		skillstrategys.append("</table>");
		html = html.replace("%skillStrategy%", skillstrategys.toString());
		Functions.show(html, activeChar);
	}

	public static void skillPage(Player player, Integer skillId)
	{
		SkillEntry se = player.getKnownSkill(skillId.intValue());
		if(se == null)
		{
			return;
		}
		Skill skill = se.getTemplate();
		if(skill == null)
		{
			return;
		}
		String html = HtmCache.getInstance().getHtml("bot/skilledit.htm", player);
		html = html.replace("%skillName%", skill.getName());
		html = html.replace("%skillId%", Integer.toString(skill.getId()));
		Functions.show(html, player);
	}

	public static void pathPage(Player player)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/path.htm", player);
		html = html.replace("%findMobMaxDistance%", Integer.toString(config.getFindMobMaxDistance()));
		html = html.replace("%findMobMaxHeight%", Integer.toString(config.getFindMobMaxHeight()));
		html = html.replace("%geometry%", config.getGeometry().cnName());
		html = html.replace("%x%", Integer.toString(config.getStartX()));
		html = html.replace("%y%", Integer.toString(config.getStartY()));
		html = html.replace("%z%", Integer.toString(config.getStartZ()));
		Functions.show(html, player);
	}

	public static void petPage(Player player)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/pet.htm", player);
		StringJoiner joiner = new StringJoiner(";");
		StringBuilder buffer = new StringBuilder();
		StringBuilder cubics = new StringBuilder();
		cubics.append("<tr><td></td><td></td></tr>");
		joiner.add("\u7121");
		/*\u65e0 无*/
		AtomicInteger index = new AtomicInteger(0);

		for(SkillEntry skillEntry : player.getAllSkillsArray())
		{
			if(skillEntry.getTemplate() instanceof Summon)
			{
				joiner.add(skillEntry.getName());
			}
			skillEntry.getTemplate().getEffectTemplates(EffectUseType.NORMAL).forEach(effect -> {
				if(effect.getHandler().getName().equalsIgnoreCase("i_summon_cubic"))
				{
					cubics.append("<tr>");
					cubics.append("<td background=\"" + (config.getAutoCubic()[index.get()] == skillEntry.getId() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox") + "\" width=14 height=16><button value=\" \" action=\"bypass -h htmbypass_bot.configSet cubic " + index.get() + " " + skillEntry.getId() + "\" width=15 height=14 back=\"\" fore=\"\"></td>");
					cubics.append("<td fixwidth=100>$name</td>".replace("$name", skillEntry.getName()));
					cubics.append("</tr>");
					index.incrementAndGet();
				}
			});
		}

		html = html.replace("%summonIds%", joiner.toString());
		html = html.replace("%cubics%", cubics.toString());
		joiner = new StringJoiner(";");
		BotPetOwnerIdleAction[] arrbotPetOwnerIdleAction = BotPetOwnerIdleAction.values();
		int n = arrbotPetOwnerIdleAction.length;
		for(int i = 0; i < n; ++i)
		{
			BotPetOwnerIdleAction action = arrbotPetOwnerIdleAction[i];
			joiner.add(action.name());
		}
		html = html.replace("%idleActions%", joiner.toString());
		html = html.replace("%idleAction%", config.getBpoidleAction().name());
		html = html.replace("%targetchoose%", config.getPetTargetChoose().name());
		int petBuffIndex = 0;
		for(SkillEntry skillEntry : player.getAllSkillsArray())
		{
			Skill skill = skillEntry.getTemplate();
			if(skill.getSkillType() != Skill.SkillType.BUFF || skill.getAbnormalTime() == -1)
				continue;
			if(petBuffIndex == 0)
			{
				buffer.append("<tr>");
			}
			buffer.append("<td background=" + (config.getPetBuffs().contains(skill.getId()) ? "L2UI.CheckBox_checked" : "L2UI.CheckBox") + " width=14 height=16><button value=\" \" action=\"bypass -h htmbypass_bot.configSet petbuff " + skill.getId() + "\" width=15 height=14 back=\"\" fore=\"\"></td>");
			buffer.append("<td fixwidth=75>" + skill.getName() + "</td>");
			if(++petBuffIndex != 2)
				continue;
			buffer.append("</tr>");
			petBuffIndex = 0;
		}
		if(petBuffIndex != 0)
		{
			buffer.append("</tr>");
		}
		html = html.replace("%buffs%", buffer.toString());
		html = html.replace("%summonAttack%", config.isSummonAttack() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		html = config.getSummonSkillId() != 0 ? html.replace("%summonName%", SkillHolder.getInstance().getSkill(config.getSummonSkillId(), 1).getName()) : html.replace("%summonName%", "\u7121");
		Functions.show(html, player);
	}

	public static void protectPage(Player player)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/protect.htm", player);
		StringJoiner joiner = new StringJoiner(";");
		StringJoiner joinerG = new StringJoiner(";");
		for(SkillEntry skillEntry : player.getAllSkillsArray())
		{
			if(skillEntry.getSkillType() != Skill.SkillType.HEAL && skillEntry.getSkillType() != Skill.SkillType.HEAL_PERCENT && skillEntry.getId() != 1256 && skillEntry.getId() != 1229 && skillEntry.getId() != 1553)
			/*技能ID1256 帕格立歐之心	技能ID1229 生命禮讚	技能ID1553 連鎖治癒*/
				continue;
			if(skillEntry.getTemplate().getSkillType() == Skill.SkillType.HOT)
			{
				joiner.add(skillEntry.getName());
				joinerG.add(skillEntry.getName());
				continue;
			}
			if(skillEntry.getId() == 1553)
			/*技能ID1553 連鎖治癒*/
			{
				joiner.add(skillEntry.getName());
				joinerG.add(skillEntry.getName());
				continue;
			}
			if(skillEntry.getTemplate().getTargetType() != Skill.SkillTargetType.TARGET_PARTY)
			{
				joiner.add(skillEntry.getName());
				continue;
			}
			joinerG.add(skillEntry.getName());
		}
		html = html.replace("%skills%", joiner.toString());
		html = html.replace("%skillsGroup%", joinerG.toString());
		StringJoiner members = new StringJoiner(";");
		Party party = player.getParty();
		if(party != null)
		{
			for(Player member : party)
			{
				if(member == player)
					continue;
				members.add(member.getName());
			}
		}
		html = html.replace("%members%", members.length() > 0 ? members.toString() : "");
		html = html.replace("%isUseRes%", config.isUseRes() ? "L2UI.CheckBox_checked" : "L2UI.CheckBox");
		StringBuilder priority = new StringBuilder();
		config.getResType().remove((Object) BotResType.DEFAULT);
		for(int index = 0; index < config.getResType().size(); ++index)
		{
			BotResType botResType = config.getResType().get(index);
			priority.append("<td>").append(botResType.getName()).append("</td>");
			if(index >= 1)
				continue;
			priority.append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.resOrder " + index + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Right\" fore=\"L2UI_CT1.Button_DF_Right\">").append("</td>");
		}
		html = html.replace("%priority%", priority.toString());
		StringBuilder builder = new StringBuilder();
		if(config.getEvaPercent() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u4f0a]").append("MP" + config.getEvaPercent()).append("%\u65f6\u5bf9\u81ea\u5df1\u4f7f\u7528:").append("\u4f0a\u5a03\u795d\u798f").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet evaPercent 0%\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u4f0a 伊	\u65f6\u5bf9\u81ea\u5df1\u4f7f\u7528: 时对自己使用:	\u4f0a\u5a03\u795d\u798f 伊娃祝福*/
		}
		if(config.getBalancePercent() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u5747]").append(String.valueOf(config.getBalanceSize()) + "\u4ebaHP" + config.getBalancePercent()).append("%\u65f6\u4f7f\u7528:").append("\u751f\u547d\u5747\u8861").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet balance 0% 0%\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u5747 均	\u4ebaHP 人HP	\u65f6\u4f7f\u7528: 时使用:	\u751f\u547d\u5747\u8861 生命均衡*/
		}
		if(config.getSelfHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]HP").append(config.getSelfHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill1(), 1).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet sprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPotionHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]HP").append(config.getPotionHpHeal()).append("%\u65f6\u4f7f\u7528:").append(ItemHolder.getInstance().getTemplate(config.getHpPotionId()).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet hppotion 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPotionMpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u81ea]MP").append(config.getPotionMpHeal()).append("%\u65f6\u4f7f\u7528:").append(ItemHolder.getInstance().getTemplate(config.getMpPotionId()).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet mppotion 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u81ea 自	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPartyHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u961f]HP").append(config.getPartyHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill2(), 1).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet pprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u961f 队	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPartyHealSkillId() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u7fa4]").append(String.valueOf(config.getPartyHealSize()) + "\u4ebaHP" + config.getPartyHealPercent()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getPartyHealSkillId(), 1).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet gprotect 0% 0% 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u7fa4 群	\u4ebaHP 人HP:	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(config.getPetHpHeal() != 0)
		{
			builder.append("<tr>").append("<td width=250>[\u5ba0]HP").append(config.getPetHpHeal()).append("%\u65f6\u4f7f\u7528:").append(SkillHolder.getInstance().getSkill(config.getHealSkill3(), 1).getName()).append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet petprotect 0 0\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
			/*\u5ba0 宠	\u4ebaHP 人HP:	\u65f6\u4f7f\u7528: 时使用:*/
		}
		if(!config.getPartyMpHeal().isEmpty())
		{
			for(Map.Entry<Integer, Integer> entry : config.getPartyMpHeal().entrySet())
			{
				int charId = entry.getKey();
				int value = entry.getValue();
				Player member = GameObjectsStorage.getPlayer(charId);
				String name = member != null ? member.getName() : "\u4e0d\u5728\u7ebf";
				/*\u4e0d\u5728\u7ebf 不在线*/
				String color = member == null || !member.isInSameParty(player) ? "AAAAAA" : "FFFF00";
				builder.append("<tr>").append("<td width=250><font color=" + color + ">" + name + "</font>MP").append(value).append("%\u65f6\u4f7f\u7528:").append("\u56de\u590d\u672f").append("</td>").append("<td>").append("<button value=\" \" action=\"bypass -h htmbypass_bot.configSet removempp " + charId + "\" width=16 height=16 back=\"L2UI_CT1.Button_DF_Delete_Down\" fore=\"L2UI_CT1.Button_DF_Delete\">").append("</td>").append("</tr>");
				/*\u65f6\u4f7f\u7528: 时使用:	\u56de\u590d\u672f 回复术*/
			}
		}
		html = html.replace("%keepMp%", Integer.toString(config.getKeepMp()));
		html = html.replace("%mpPolicy%", config.getMpHealOrder().name());
		html = html.replace("%info%", builder.toString());
		Functions.show(html, player);
	}

	public static void itemUsePage(Player player)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(player);
		StringBuilder builder = new StringBuilder();
		String html = HtmCache.getInstance().getHtml("bot/useitem.htm", player);
		//<$contents$>

		builder.append("<table>");
		int index = 0;
		for(int itemId : BotProperties.BUFF_ITEM_IDS)
		{
			if(player.getInventory().getItemByItemId(itemId) == null)
				continue;
			if(index == 0)
			{
				builder.append("<tr>");
			}
			builder.append("<td align=CENTER width=150>").append("<button width=32 height=32 itemtooltip=\"" + itemId + "\" back=\"L2UI_CH3.aboutotpicon\" fore=\"L2UI_CH3.aboutotpicon\"></button\ufeff>").append("<br1>").append(HtmlUtils.htmlItemName((int) itemId)).append("<br1>").append(config.getAutoItemBuffs().contains(itemId) ? "<font color=00FF00>" + HtmlUtils.htmlButton((String) "YES", (String) new StringBuilder("bypass -h htmbypass_bot.configSet itemId ").append(itemId).toString(), (int) 30) + "</font>" : "<font color=FF0000>" + HtmlUtils.htmlButton((String) "NO", (String) new StringBuilder("bypass -h htmbypass_bot.configSet itemId ").append(itemId).toString(), (int) 30) + "</font>").append("</td>");
			if(++index != 2)
				continue;
			builder.append("</tr>");
			index = 0;
		}
		if(index != 0)
		{
			builder.append("</tr>");
		}
		builder.append("</table>");
		html = html.replace("<$contents$>", builder.toString()).replace("<table></table>","<table><tr><td></td></tr></table>");
		Functions.show(html, player);
	}

	public static void party(Player player)
	{
		BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
		String html = HtmCache.getInstance().getHtml("bot/party.htm", player);
		StringBuilder builder = new StringBuilder();
		config.getPartyMemberHolder().forEach((name, invite) -> {
			builder.append("<table background=\"L2UI_CT1.Windows.Windows_DF_TooltipBG\">");
			builder.append("<tr>");
			builder.append("<td width=115>").append(name).append("</td>");
			builder.append("<td width=65>").append(invite == false ? HtmlUtils.htmlButton("\u81ea\u52a8\u9080\u8bf7",  "bypass -h htmbypass_bot.auto_invite " + name,  65) : HtmlUtils.htmlButton("\u53d6\u6d88\u9080\u8bf7", "bypass -h htmbypass_bot.remove_invite " + name, 65)).append("</td>");
			/*\u81ea\u52a8\u9080\u8bf7 自动邀请 \u53d6\u6d88\u9080\u8bf7 取消邀请*/
			Player p = GameObjectsStorage.getPlayer(name);
			boolean abort = p == null || BotEngine.getInstance().getBotConfig(p).isAbort();
			builder.append("<td width=65>").append(!abort ? HtmlUtils.htmlButton( "\u505c\u6b62\u5185\u6302", "bypass -h htmbypass_bot.p_abort " + name, 65) : HtmlUtils.htmlButton("\u542f\u52a8\u5185\u6302", "bypass -h htmbypass_bot.p_run " + name, 65)).append("</td>");
			/*\u505c\u6b62\u5185\u6302 停止内挂	\u542f\u52a8\u5185\u6302 启动内挂*/
			builder.append("<td width=50>").append(HtmlUtils.htmlButton("\u5220\u9664", "bypass -h htmbypass_bot.remove_p " + name,  50)).append("</td>");
			/*\u5220\u9664 删除*/
			builder.append("</tr>");
			builder.append("</table>");
		});
		html = html.replace("%loottype%", config.getLootType().name());
		html = html.replace("%leaderName%", config.getLeaderName());
		html = html.replace("%list%", builder.toString());
		Functions.show(html, player);
	}

	public static void transactionBank(Player player) {
		String html = HtmCache.getInstance().getHtml("bot/transactionBank.htm", player);
		if (TransactionBankCount.exchangeRate == null) {
			BigDecimal read = TransactionBankCount.getInstance().read();
			html = html.replace("%gold%", numFormat(Long.parseLong(String.valueOf(read))));
		}else {
			html = html.replace("%gold%", numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
		}
//		html = html.replace("%rateRenewTime%", String.valueOf(TimerManager.getInstance().time));
		Functions.show(html, player);
	}

	public static void chooseExchange(Player player, String param) {
		String html = HtmCache.getInstance().getHtml("bot/chooseExchange.htm", player);
		html = html.replace("%exchangeKind%",param.equals("chooseGoldExchange") || param.equals("金币")?"\u91d1\u5e01":"\u8d5e\u52a9\u5e01");

		/** 判断玩家拥有多少金币或者赞助币 来显示红色，当刷新的时候要考虑*/
		long countOfGold = player.getInventory().getCountOf(57);
		long countOfVipGold = player.getInventory().getCountOf(29520);
		// 如果是兑换金币
		if (param.equals("chooseGoldExchange") || param.equals("\u91d1\u5e01")) {
			html = html.replace("%exchangeGoldKind%",numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
			// 如果玩家赞助币币小于30 显示红色
			if (countOfVipGold >=0 && countOfVipGold < 30L) {
			html = html.replaceFirst("%consumeGoldKind%","<font color=\"FF0000\">30</font>");
			}
			html = html.replace("%consume%","赞助币");
			html = html.replace("%consumeGoldKind%","30");
		}else if(param.equals("chooseVipGoldExchange") || param.equals("\u8d5e\u52a9\u5e01")){
			html = html.replace("%exchangeGoldKind%","30");
			// 如果玩家金币小于汇率 显示红色
			if(countOfGold >=0 &&  BigDecimal.valueOf(countOfGold).compareTo(TransactionBankCount.exchangeRate) == -1){
				html = html.replaceFirst("%consumeGoldKind%","<font color=\"FF0000\">"+numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate)))+"</font>");
			}
			html = html.replace("%consume%","金");
			html = html.replace("%consumeGoldKind%",numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
		}
//		html = html.replace("%rateRenewTime%", String.valueOf(TimerManager.getInstance().time));
		html = html.replace("%ownVipGold%","赞助币"+numFormat(countOfVipGold));
		html = html.replace("%ownGold%","金币"+numFormat(countOfGold));
		Functions.show(html, player);
	}
	// 交易行确认交易
	public static void defineExchange(Player player, String param) {
		String html = HtmCache.getInstance().getHtml("bot/defineExchange.htm", player);
		StringBuilder builder = new StringBuilder(html);
		long countOfGold = player.getInventory().getCountOf(57);
		long countOfVipGold = player.getInventory().getCountOf(29520);

		if (param != null && param.equals("chooseGoldExchange")) {
			// 删除金币不足的页面
			int startIndexOf = html.indexOf("<$goldContents$>");
			int lastIndexOf = html.lastIndexOf("<$VIPContents$>");
			StringBuilder stringBuilder = builder.delete(startIndexOf, lastIndexOf);
			html = stringBuilder.toString();
			html = html.replace("<$VIPContents$>","");
			html = html.replace("<$contents$>","<button value=\"前往充值\" action=\"bypass -h htmbypass_bot.\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">");
			html = html.replace("%defineTitle%","\u8d5e\u52a9\u5e01");
			player.sendMessage("您的赞助币不足");
		}else if(param != null && param.equals("chooseVipGoldExchange")){
			int startIndexOf = html.indexOf("<$VIPContents$>");
			int lastIndexOf = html.lastIndexOf("<$contents$>");
			StringBuilder stringBuilder = builder.delete(startIndexOf, lastIndexOf);
			html = stringBuilder.toString();
			html = html.replace("<$contents$>","<table><tr><td>" +
					"<button value=\"确认\" action=\"bypass -h htmbypass_bot.chooseExchange\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">" +
					"</td><td>"+
					"<button value=\"取消\" action=\"bypass -h htmbypass_bot.chooseExchange\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">"+
					"</td></tr></table>");
			html = html.replace("<$goldContents$>","");
			html = html.replace("%defineTitle%","\u91d1\u5e01");
			player.sendMessage("您的金币不足");
		}
		html = html.replace("%ownVipGold%","赞助币"+numFormat(countOfVipGold));
		html = html.replace("%ownGold%","金币"+numFormat(countOfGold));
		Functions.show(html, player);
	}

	public static String numFormat(long num){
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		String format = decimalFormat.format(num);
		if(String.valueOf(num).length()>15){
			return format.substring(0, 19);
		}else {
			return format;
		}
	}
}