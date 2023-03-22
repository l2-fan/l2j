package l2s.gameserver.network.l2.c2s.pledge;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.variables.PlayerVariables;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.c2s.L2GameClientPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExNoticePostArrived;
import l2s.gameserver.network.l2.s2c.ExUnReadMailCount;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.pledge.ExPledgeDonationRequest;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author nexvill
 */
public class RequestExPledgeDonationRequest extends L2GameClientPacket
{
	private int _donateType;
	@Override
	protected boolean readImpl()
	{
		_donateType = readC();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (activeChar.getClan() == null)
			return;
		
		if (activeChar.getVarBoolean(PlayerVariables.DONATION_BLOCKED, false) == true)
			return;
		
		int donations = activeChar.getVarInt(PlayerVariables.DONATIONS_AVAILABLE, 3);
		Clan clan = activeChar.getClan();
		if (donations < 1)
			return;
		if (_donateType == 0)
			if (!activeChar.getInventory().destroyItemByItemId(ItemTemplate.ITEM_ID_ADENA, 10000))
				return;
			else
			{
				clan.setPoints(clan.getPoints() + 3);
				int weeklyContribution = activeChar.getVarInt(PlayerVariables.WEEKLY_CONTRIBUTION, 0) + 3;
				int totalContribution = activeChar.getVarInt(PlayerVariables.TOTAL_CONTRIBUTION, 0) + 3;
				activeChar.setVar(PlayerVariables.WEEKLY_CONTRIBUTION, weeklyContribution);
				activeChar.setVar(PlayerVariables.TOTAL_CONTRIBUTION, totalContribution);
			}
		else if (_donateType == 1)
			if (!activeChar.getInventory().destroyItemByItemId(ItemTemplate.ITEM_ID_MONEY_L, 100))
				return;
			else
			{
				clan.setPoints(clan.getPoints() + 10);
				int weeklyContribution = activeChar.getVarInt(PlayerVariables.WEEKLY_CONTRIBUTION, 0) + 10;
				int totalContribution = activeChar.getVarInt(PlayerVariables.TOTAL_CONTRIBUTION, 0) + 10;
				activeChar.setVar(PlayerVariables.WEEKLY_CONTRIBUTION, weeklyContribution);
				activeChar.setVar(PlayerVariables.TOTAL_CONTRIBUTION, totalContribution);
				
				boolean critical = Rnd.get(100) < 5? true : false;
				int coins = 100;
				if (!critical)
				{
					activeChar.getInventory().addItem(95570, coins);
				}
				else
				{
					sendMailToMembers(activeChar, coins);
					coins *= 2;
					activeChar.getInventory().addItem(95570, coins);
					clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_CRITICAL_DONATION).addString(activeChar.getName()));
				}
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HONOR_COINS_OBTAINED).addNumber(coins));
			}
		else if (_donateType == 2)
			if (!activeChar.getInventory().destroyItemByItemId(ItemTemplate.ITEM_ID_MONEY_L, 500))
				return;
			else
			{
				clan.setPoints(clan.getPoints() + 50);
				int weeklyContribution = activeChar.getVarInt(PlayerVariables.WEEKLY_CONTRIBUTION, 0) + 50;
				int totalContribution = activeChar.getVarInt(PlayerVariables.TOTAL_CONTRIBUTION, 0) + 50;
				activeChar.setVar(PlayerVariables.WEEKLY_CONTRIBUTION, weeklyContribution);
				activeChar.setVar(PlayerVariables.TOTAL_CONTRIBUTION, totalContribution);
				
				boolean critical = Rnd.get(100) < 5? true : false;
				int coins = 500;
				if (!critical)
				{
					activeChar.getInventory().addItem(95570, coins);
				}
				else
				{
					sendMailToMembers(activeChar, coins);
					coins *= 2;
					activeChar.getInventory().addItem(95570, coins);
					clan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.S1_CRITICAL_DONATION).addString(activeChar.getName()));
				}
				activeChar.sendPacket(new SystemMessage(SystemMsg.S1_HONOR_COINS_OBTAINED).addNumber(coins));
			}
		
		activeChar.setVar(PlayerVariables.DONATIONS_AVAILABLE, --donations);
		
		activeChar.sendPacket(new ExPledgeDonationRequest(activeChar, _donateType));
	}
	
	private static void sendMailToMembers(Player player, int count)
	{
		for (UnitMember member : player.getClan().getAllMembers())
		{
			if (member.getObjectId() == player.getObjectId())
				continue;
			Mail mail = new Mail();
			mail.setSenderId(1);
			mail.setSenderName(StringsHolder.getInstance().getString(player, "birthday.npc"));
			mail.setReceiverId(member.getObjectId());
			mail.setReceiverName(member.getName());
			mail.setTopic("Critical Donation Reward");
			mail.setBody("Clan member " + player.getName() + " makes critical donation. This is reward to clan members.");
			
			ItemInstance item = ItemFunctions.createItem(95570);
			item.setLocation(ItemInstance.ItemLocation.MAIL);
			item.setCount(count / 2);
			item.save();
			
			mail.addAttachment(item);
			mail.setUnread(true);
			mail.setType(Mail.SenderType.BIRTHDAY);
			mail.setExpireTime(720 * 3600 + (int) (System.currentTimeMillis() / 1000L));
			mail.save();
			
			Player plr = GameObjectsStorage.getPlayer(member.getObjectId());
			if (plr != null)
			{
				plr.sendPacket(ExNoticePostArrived.STATIC_TRUE);
				plr.sendPacket(new ExUnReadMailCount(plr));
				plr.sendPacket(SystemMsg.THE_MAIL_HAS_ARRIVED);
			}
		}
	}
}