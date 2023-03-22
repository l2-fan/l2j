
package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.MonsterSelectUtil;
import l2s.gameserver.botscript.PetTargetChoose;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotPetOwnerIdleAction;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.core.Geometry;
import l2s.gameserver.core.IBotActionHandler;
import java.util.LinkedList;
import java.util.Optional;
import l2s.gameserver.ai.ServitorAI;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.instances.BossInstance;

public class BotAttack implements IBotActionHandler
{
	private static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		boolean doAttack = this.doAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		boolean doSummon = this.doSummonAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		return doAttack || doSummon;
	}

	private boolean doAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAutoAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		if(actor.isSitting())
		{
			return false;
		}
		if(this.isActionsDisabledExcludeAttack(actor) && config.isCoverMember() || !actor.isActionsDisabled())
		{
			Optional<MonsterInstance> mob = Optional.empty();
			MonsterInstance monster = null;
			MonsterInstance petTarget = this.petTarget(actor);	
			if(config.isCoverMember() || (actor.getTarget() == null || !actor.getTarget().isMonster() || this.getMonster(actor.getTarget()).isDead()) && petTarget == null)
			{
				mob = MonsterSelectUtil.apply(actor);
				if(!mob.isPresent())
				{
					if(!actor.isImmobilized())
					{
						this.returnHome(actor);
					}
					return false;
				}
				monster = mob.get();
			}
			else
			{
				monster = petTarget != null ? petTarget : this.getMonster(actor.getTarget());
			}
			if(actor.getTarget() != monster)
			{
				actor.setTarget(monster);
			}
			if(!actor.getTarget().isBoss())
			{
				actor.setTarget(monster);
			}
			if(!GeoEngine.canSeeTarget(actor, monster))
			{
				actor.getMovement().moveToLocation(monster.getLoc(), 0, !actor.getVarBoolean("no_pf"), true, false);
				if(monster.getObjectId() != config.getCurrentTargetObjectId())
				{
					config.setCurrentTargetObjectId(monster.getObjectId());
				}
				config.setTryTimes(config.getTryTimes() + 1);//(config.getTryTimes() + 1) 修改攻擊間隔時間+1
				if(config.getTryTimes() >= 5)
				{
					actor.sendMessage("\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807");
					/*\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807 攻击超时，切换目标*/
					config.setTryTimes(0);
					config.addBlockTargetId(monster.getObjectId());
					actor.setTarget(null);
				}
				return false;
			}
			if(config.getTryTimes() != 0)
			{
				config.setTryTimes(0);
			}
			boolean useStrategy = false;
			for(BotSkillStrategy s : config.getAttackStrategy())
			{
				useStrategy = s.useMe(actor, monster);
				if(useStrategy)
					break;
			}
			if(!useStrategy && config.isUsePhysicalAttack())
			{
				actor.getTarget().onAction(actor, false);
			}		
		}
			return true;
	}

	private boolean doSummonAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isSummonAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		SummonInstance pet = actor.getSummon();
		if(pet == null || pet.isActionsDisabled())
		{
			return false;
		}
		GameObject target = pet.getTarget();
		if(target != null)
		{
			MonsterInstance mob = null;
			mob = this.getMonster(target);
			if(mob == null || mob.isDead() || !Geometry.calc(actor, mob))
			{
				this.resetTarget(pet, actor, config);
			}
		}
		else
		{
			this.resetTarget(pet, actor, config);
		}
		if((target = pet.getTarget()) != null)
		{
			pet.getAI().Attack(this.getMonster(target), false, false);
			if(config.getBpoidleAction() != BotPetOwnerIdleAction.\u539f\u5730\u4e0d\u52a8 && (!config.isFollowAttack() || config.isAutoAttack() && !config.isUsePhysicalAttack() && config.getAttackStrategy().isEmpty()))
			/*\u539f\u5730\u4e0d\u52a8 原地不动*/
			{
				if(config.getBpoidleAction() == BotPetOwnerIdleAction.\u9760\u8fd1\u53ec\u5524\u517d)
				/*\u9760\u8fd1\u53ec\u5524\u517d 靠近召唤兽*/
				{
					actor.getMovement().moveToLocation(pet.getLoc(), 400, !actor.getVarBoolean("no_pf"));
				}
				else
				{
					double dist;
					Party party = actor.getParty();
					if(party != null && (dist = actor.getDistance(party.getPartyLeader())) > 400.0 && dist < 3500.0)
					{
						actor.getMovement().moveToLocation(party.getPartyLeader().getLoc(), 400, !actor.getVarBoolean("no_pf"));
					}
				}
			}
			return true;
		}
		return false;
	}

	private void resetTarget(SummonInstance pet, Player actor, BotConfigImp config)
	{
		GameObject target;
		if(pet.getTarget() != null)
		{
			pet.setTarget(null);
		}
		if((target = actor.getTarget()) != null && target.isMonster() && !this.getMonster(target).isDead() && Geometry.calc(actor, this.getMonster(target)))
		{
			pet.setTarget(target);
		}
		else if(target == null)
		{
			Optional<MonsterInstance> mob = Optional.empty();
			switch(BotAttack.$SWITCH_TABLE$botscript$PetTargetChoose()[config.getPetTargetChoose().ordinal()])
			{
				case 1:
				{
					mob = MonsterSelectUtil.apply(actor);
					break;
				}
				case 2:
				{
					mob = target == null ? Optional.empty() : Optional.of(this.getMonster(target));
					break;
				}
				case 3:
				{
					Party party = actor.getParty();
					if(party == null)
						break;
					target = party.getPartyLeader().getTarget();
					mob = target == null || !target.isMonster() ? Optional.empty() : Optional.of(this.getMonster(target));
				}
			}
			if(!mob.isPresent())
			{
				if(!actor.isImmobilized())
				{
					this.returnHome(actor);
				}
				return;
			}
			MonsterInstance monster = mob.get();
			pet.setTarget(monster);
		}
	}

	private void returnHome(Player actor)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		if(actor.getDistance(config.getStartX(), config.getStartY(), config.getStartZ()) < 5000.0)
		{
			actor.standUp();
			actor.getMovement().moveToLocation(config.getStartX(), config.getStartY(), config.getStartZ(), 100, !actor.getVarBoolean("no_pf"), true, false);
		}
	}

	private MonsterInstance petTarget(Player actor)
	{
		SummonInstance instance = actor.getSummon();
		if(instance == null)
		{
			return null;
		}
		if(instance.getTarget() == null)
		{
			return null;
		}
		MonsterInstance target = this.getMonster(instance.getTarget());
		if(target == null || target.isDead())
		{
			return null;
		}
		return target;
	}

	static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose()
	{
		if($SWITCH_TABLE$botscript$PetTargetChoose != null)
		{
			//int[] arrn;
			return $SWITCH_TABLE$botscript$PetTargetChoose;
		}
		int[] arrn = new int[PetTargetChoose.values().length];
		try
		{
			arrn[PetTargetChoose.自主选怪.ordinal()] = 1;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随主人.ordinal()] = 2;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随队长.ordinal()] = 3;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$botscript$PetTargetChoose = arrn;
		return $SWITCH_TABLE$botscript$PetTargetChoose;
	}
}