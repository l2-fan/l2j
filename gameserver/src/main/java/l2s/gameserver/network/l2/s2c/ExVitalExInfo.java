package l2s.gameserver.network.l2.s2c;

/**
 * @author nexvill
 */
public class ExVitalExInfo extends L2GameServerPacket
{
	private final int _vitalityBonus;
	private final int _additionalBonus;

	public ExVitalExInfo(int baseVitalityBonus, int additionalBonus)
	{
		_vitalityBonus = baseVitalityBonus;
		_additionalBonus = additionalBonus;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0); //?
		writeD(0); //?
		writeD(_vitalityBonus); // Vitality bonus
		writeD(_additionalBonus); // add XP Bonus
	}
}
