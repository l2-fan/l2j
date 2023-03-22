package l2s.gameserver.botscript;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BotHangUpTimeDao {

    private static final Logger _log = LoggerFactory.getLogger(CharacterDAO.class);

    private static BotHangUpTimeDao _instance = new BotHangUpTimeDao();

    public static BotHangUpTimeDao getInstance()
    {
        return _instance;
    }

    /* 重置挂机时间 */
    public void updateHangUpTime(int obj_id) {

    }

    public List<Integer> selectHangUpTime(int obj_id){

        Connection con = null;
        PreparedStatement statement = null;
        List<Integer> hangUpTime = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT script_time, left_time FROM character_script_time WHERE obj_id=?");
            statement.setInt(1, obj_id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                hangUpTime = new ArrayList<>();
                hangUpTime.add(resultSet.getInt(1));
                hangUpTime.add(resultSet.getInt(2));
            }
            DbUtils.close(statement);
            return hangUpTime;
        }
        catch(final SQLException e)
        {
            _log.error("", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
        return hangUpTime;
    }

    public void insertScriptTime(Integer obj_id, Integer scriptTime , Integer leftTime){

        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO character_script_time (obj_Id, script_time, left_time) VALUES (?,?,?)");
            statement.setInt(1, obj_id);
            statement.setInt(2, scriptTime);
            statement.setInt(3, leftTime);
            statement.executeUpdate();
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public void deleteHangUpTime(int objectId) {

        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_script_time WHERE obj_Id = ?");
            statement.setInt(1, objectId);
            statement.execute();
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateHangUpTime(int objectId, int parseInt) {

        Connection con = null;
        PreparedStatement statement = null;

        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE character_script_time SET left_time = ? WHERE obj_Id = ?");
            statement.setInt(1, parseInt);
            statement.setInt(2, objectId);
            statement.executeUpdate();
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /* 查询所有玩家的内挂信息 */
    public List<Map<String,Object>> selectAllPlayer() {
        List<Map<String,Object>> allPlayerHangUpInfo = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT id, obj_id, script_time, left_time, renew_time FROM  character_script_time");
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Map<String,Object> player = new HashMap<>();
                player.put("id",resultSet.getInt(1));
                player.put("obj_id",resultSet.getInt(2));
                player.put("script_time",resultSet.getInt(3));
                player.put("left_time",resultSet.getInt(4));
                player.put("renew_time",resultSet.getObject(5));
                allPlayerHangUpInfo.add(player);
            }
            DbUtils.close(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allPlayerHangUpInfo;
    }
    // 定时更新挂机时间 更新 leftTime renewTime
    public void updateHangUpRenewTime(List<Integer> obj_id , Date date) {

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE character_script_time SET left_time = ? ,renew_time = ? WHERE obj_id=?");
            for (Integer integer : obj_id) {
                statement.setInt(1, Player.scriptTime);
                statement.setDate(2, date);
                statement.setInt(3, integer);
//                statement.executeUpdate();
                statement.addBatch();
            }
            statement.executeBatch();
            DbUtils.close(statement);
        }
        catch(final SQLException e)
        {
            _log.error("", e);
        }
        finally
        {
            DbUtils.closeQuietly(con, statement);
        }
    }
}
