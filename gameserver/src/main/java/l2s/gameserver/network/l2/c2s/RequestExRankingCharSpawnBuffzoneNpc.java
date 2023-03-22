package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcInfo;
import l2s.gameserver.network.l2.s2c.ExRankingCharBuffzoneNpcPosition;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;


public class RequestExRankingCharSpawnBuffzoneNpc extends L2GameClientPacket
{
	private ScheduledFuture<?> _taskBuff, _taskClear;
	private NpcInstance _hiddenNpc;
	private DecoyInstance _decoy;
	
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		
		if (!activeChar.isInPeaceZone())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.RANKERS_AUTHORITY_CANNOT_BE_USED_IN_THIS_AREA));
			return;
		}
		
		if (!activeChar.getInventory().destroyItemByItemId(57, 20_000_000))
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.FAILED_BECAUSE_THERES_NOT_ENOUGH_ADENA));
			return;
		}
		
		NpcTemplate DecoyTemplate = NpcHolder.getInstance().getTemplate(18485);
		
		_hiddenNpc = new NpcInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, StatsSet.EMPTY);
		_hiddenNpc.setTargetable(false);
		_hiddenNpc.spawnMe(activeChar.getLoc());
		
		_decoy = new DecoyInstance(IdFactory.getInstance().getNextId(), DecoyTemplate, activeChar, 43_200_000);
		_decoy.setCurrentHp(_decoy.getMaxHp(), false);
		_decoy.setCurrentMp(_decoy.getMaxMp());
		_decoy.setHeading(activeChar.getHeading());
		_decoy.setReflection(activeChar.getReflection());
		_decoy.setTargetable(false);
		_decoy.spawnMe(activeChar.getLoc());
		for (Player plr : GameObjectsStorage.getPlayers(false, false))
			plr.sendPacket(new SystemMessagePacket(SystemMsg.SERVER_RANK_1_C1_HAS_CREATED_RANKERS_AUTHORITY_IN_S2).addName(activeChar).addZoneName(activeChar.getLoc()));
		
		ServerVariables.set("buffNpcActive", true);
		ServerVariables.set("buffNpcX", _decoy.getX());
		ServerVariables.set("buffNpcY", _decoy.getY());
		ServerVariables.set("buffNpcZ", _decoy.getZ());
		ServerVariables.set("buffNpcReset", (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)));
		activeChar.sendPacket(new ExRankingCharBuffzoneNpcPosition((byte) 1, _decoy.getX(), _decoy.getY(), _decoy.getZ()));
		activeChar.sendPacket(new ExRankingCharBuffzoneNpcInfo());
		
		_taskBuff = ThreadPoolManager.getInstance().scheduleAtFixedRate(() ->
		{
			if (_decoy.getPlayer() != null)
			{
				SkillEntry buff = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 52018, 1);
				_hiddenNpc.doCast(buff, _hiddenNpc, true);
				for (Creature crt : _decoy.getAroundCharacters(300, 1000))
				{
					if (crt.isPlayer() || crt.isSummon())
						crt.startAttackStanceTask();
				}
				_decoy.broadcastPacket(new SocialActionPacket(_decoy.getObjectId(), SocialActionPacket.GREETING));
			}
			else
				clearNpc();
		}, 5000, 10000);
		
		_taskClear = ThreadPoolManager.getInstance().schedule(() ->
		{
			clearNpc();
		}, 43200000L);
	}
	
	private void clearNpc()
	{
		ServerVariables.unset("buffNpcActive");
		ServerVariables.unset("buffNpcX");
		ServerVariables.unset("buffNpcY");
		ServerVariables.unset("buffNpcZ");
		_hiddenNpc.deleteMe();
		_decoy.deleteMe();
		if (_taskClear != null)
		{
			_taskClear.cancel(true);
			_taskClear = null;
		}
		if (_taskBuff != null)
		{
			_taskBuff.cancel(true);
			_taskBuff = null;
		}
	}
}