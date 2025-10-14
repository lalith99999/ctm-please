package com.ctm.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ctm.dao.TournamentDao;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

public class TournamentDaoImpl implements TournamentDao {

    @Override
    public Tournament createTournament(String name, String format) {
        if (existsByName(name)) return null;

        long newId = 0;
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery("SELECT NVL(MAX(tournament_id),0)+1 AS next_id FROM tournaments")) {
            if (rs.next()) newId = rs.getLong("next_id");
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "INSERT INTO tournaments (tournament_id, name, format) VALUES (?, ?, ?)")) {
            ps.setLong(1, newId);
            ps.setString(2, name.trim());
            ps.setString(3, format.trim());
            ps.executeUpdate();
            return new Tournament(newId, name, format);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM tournaments WHERE LOWER(name)=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public Optional<Tournament> findTournament(long tournamentId) {
        String sql = "SELECT tournament_id, name, format FROM tournaments WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Tournament(
                        rs.getLong("tournament_id"),
                        rs.getString("name"),
                        rs.getString("format")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Tournament> listAllTournaments() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT tournament_id, name, format FROM tournaments ORDER BY tournament_id";
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Tournament(
                        rs.getLong("tournament_id"),
                        rs.getString("name"),
                        rs.getString("format")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Tournament updateTournament(long id, String newName, String newFormat) {
        String sql = "UPDATE tournaments SET name=?, format=? WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newFormat);
            ps.setLong(3, id);
            ps.executeUpdate();
            return new Tournament(id, newName, newFormat);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
 // inside TournamentDaoImpl class (below your updateTournament method)
    public void updateTournamentName(long id, String newName) {
        updateTournament(id, newName, "T20");
    }


    @Override
    public Tournament deleteTournament(long id) {
        Optional<Tournament> before = findTournament(id);
        if (before.isEmpty()) return null;

        try (Connection con = DaoUtil.getMyConnection()) {
            con.setAutoCommit(false);

            con.prepareStatement(
                    "DELETE FROM player_performance WHERE match_id IN (SELECT match_id FROM matches WHERE tournament_id=?)")
                    .executeUpdate();

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM matches WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM tournament_teams WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM tournaments WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            con.commit();
            return before.get();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean fixturesExist(long tournamentId) {
        String sql = "SELECT 1 FROM matches WHERE tournament_id=? FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Compatibility methods (not implemented)
    @Override public Tournament getTournamentOrThrow(long tournamentId) { return findTournament(tournamentId).orElseThrow(); }
    @Override
    public List<TeamStanding> listTeamsInTournament(long tournamentId) {
        List<TeamStanding> teams = new ArrayList<>();
        String sql =
                "SELECT tt.team_id, "
              + "       NVL(tt.name, t.name) AS name, "
              + "       NVL(tt.city, t.city) AS city, "
              + "       NVL(tt.points, 0) AS points, "
              + "       NVL(tt.nrr, 0) AS nrr, "
              + "       NVL(tt.played, 0) AS played "
              + "FROM tournament_teams tt "
              + "LEFT JOIN teams t ON tt.team_id = t.team_id "
              + "WHERE tt.tournament_id = ? "
              + "ORDER BY tt.team_id";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    teams.add(new TeamStanding(
                            rs.getLong("team_id"),
                            rs.getString("name"),
                            rs.getString("city"),
                            rs.getInt("points"),
                            rs.getDouble("nrr"),
                            rs.getInt("played")));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return teams;
    }

    @Override
    public void enrollTeam(long tournamentId, long teamId) {
        if (isTeamEnrolled(tournamentId, teamId)) return;
        String sql =
                "INSERT INTO tournament_teams (tournament_id, team_id, name, city, points, nrr, played) "
              + "SELECT ?, team_id, name, city, 0, 0, 0 FROM teams WHERE team_id = ?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, teamId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void removeTeam(long tournamentId, long teamId) {
        deleteTournamentTeam(tournamentId, teamId);
    }

    @Override
    public boolean isTeamEnrolled(long tournamentId, long teamId) {
        String sql = "SELECT 1 FROM tournament_teams WHERE tournament_id=? AND team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void registerMatch(long tournamentId, long matchId) {
        // Method kept for interface compatibility. Matches are persisted via ScheduleDaoRoundRobin.
    }

    @Override
    public int updateTournamentTeam(long tournamentId, long teamId, String newName, String newCity) {
        String sql = "UPDATE tournament_teams SET name=?, city=? WHERE tournament_id=? AND team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newCity);
            ps.setLong(3, tournamentId);
            ps.setLong(4, teamId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int deleteTournamentTeam(long tournamentId, long teamId) {
        String sql = "DELETE FROM tournament_teams WHERE tournament_id=? AND team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, teamId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    @Override
    public int countFixtures(long tournamentId) {
        String sql = "SELECT COUNT(*) FROM matches WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}
