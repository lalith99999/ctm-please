package com.ctm.daoimpl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import com.ctm.model.MatchStatus;
import com.ctm.model.TeamStanding;
import com.ctm.util.DaoUtil;

/**
 * Round-robin fixture generator.
 * Each team plays every other team once.
 */
public class ScheduleDaoRoundRobin {

    public boolean generateFixtures(long tournamentId, List<TeamStanding> teams, String venue) {
        if (teams == null || teams.size() < 3) return false;

        int n = teams.size();
        List<Long> teamIds = new ArrayList<>();
        for (TeamStanding ts : teams) teamIds.add(ts.getTeamId());

        // Ensure even number of teams (if odd, add dummy)
        boolean hasDummy = false;
        if (n % 2 != 0) {
            teamIds.add(-1L); // dummy team (bye)
            hasDummy = true;
            n++;
        }

        int totalRounds = n - 1;
        int matchesPerRound = n / 2;

        try {
            for (int round = 0; round < totalRounds; round++) {
                for (int i = 0; i < matchesPerRound; i++) {
                    long home = teamIds.get(i);
                    long away = teamIds.get(n - 1 - i);

                    if (home == -1L || away == -1L) continue; // skip dummy

                    insertMatch(tournamentId, home, away, venue);
                }

                // rotate list (except first element)
                Long first = teamIds.remove(1);
                teamIds.add(first);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertMatch(long tournamentId, long teamA, long teamB, String venue) throws SQLException {
        String sql = "INSERT INTO matches (match_id, tournament_id, team_a_id, team_b_id, venue, status) " +
                     "VALUES ((SELECT NVL(MAX(match_id),0)+1 FROM matches), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, teamA);
            ps.setLong(3, teamB);
            ps.setString(4, venue);
            ps.setString(5, MatchStatus.SCHEDULED.name());
            ps.executeUpdate();
        }
    }
}
