package l2s.gameserver.network.l2.c2s;

public class RequestExCleftEnter extends L2GameClientPacket
{
	private int unk;

	/**
	 * format: d
	 */
	@Override
	protected boolean readImpl()
	{
		unk = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		//TODO not implemented
	}
}