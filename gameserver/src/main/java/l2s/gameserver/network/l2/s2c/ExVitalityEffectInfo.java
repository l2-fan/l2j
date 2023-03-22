package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

/**
 * @author Bonux
 */
public class ExVitalityEffectInfo extends L2GameServerPacket
{
	private final int _sayhasGracePoints;
	private final int _bonusPercent;

	public ExVitalityEffectInfo(Player player)
	{
		_sayhasGracePoints = player.getSayhasGrace();
		_bonusPercent = (int) (player.getSayhasGraceBonus() * 100);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_sayhasGracePoints); // sayha's grace points
		writeD(_bonusPercent); // XP Bonus
		writeH(0x00); // Additional XP bonus
		writeH(5); // Vitality items allowed???
		writeH(5); // Total vitality items allowed???
	}
}
