package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.boat.Boat;

public class RequestGetOffVehicle extends L2GameClientPacket
{
	// Format: cdddd
	private int _objectId;
	private Location _location = new Location();

	@Override
	protected boolean readImpl()
	{
		_objectId = readD();
		_location.x = readD();
		_location.y = readD();
		_location.z = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Boat boat = BoatHolder.getInstance().getBoat(_objectId);
		if(boat == null || boat.getMovement().isMoving())
		{
			player.sendActionFailed();
			return;
		}

		boat.oustPlayer(player, _location, false);
	}
}