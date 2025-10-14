package com.ctm.servlet;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ctm.daoimpl.ScheduleDaoRoundRobin;
import com.ctm.daoimpl.TeamDaoImpl;
import com.ctm.daoimpl.TournamentDaoImpl;
import com.ctm.model.Match;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

@WebServlet("/fixturesgen")
public class FixturesGenerateServlet extends HttpServlet {
    private final TournamentDaoImpl tDao = new TournamentDaoImpl();
    private final TeamDaoImpl teamDao = new TeamDaoImpl();
    private final ScheduleDaoRoundRobin roundRobin = new ScheduleDaoRoundRobin();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equalsIgnoreCase((String)s.getAttribute("role"))) {
            resp.sendRedirect("index.jsp");
            return;
        }

        String action = req.getParameter("action");
        long tid = parseLong(req.getParameter("tid"));
        if (tid <= 0) {
            req.setAttribute("mode", "list");
            req.setAttribute("tournaments", tDao.listAllTournaments());
            req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
            return;
        }

        Tournament tour = tDao.findTournament(tid).orElse(null);
        if (tour == null) { resp.sendRedirect("fixturesgen"); return; }

        boolean fixturesExist = tDao.fixturesExist(tid);

        if ("run".equalsIgnoreCase(action)) {
            List<TeamStanding> teams = tDao.listTeamsInTournament(tid);
            if (teams.size() < 3) {
                req.setAttribute("err", "notenough");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            boolean allHave11 = teams.stream().allMatch(ts ->
                    teamDao.listPlayersOfTeam(ts.getTeamId()).size() == 11);
            if (!allHave11) {
                req.setAttribute("err", "squads");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            if (fixturesExist) {
                req.setAttribute("err", "exists");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            String venue = req.getParameter("venue");
            boolean ok = roundRobin.generateFixtures(tid, teams, venue);
            if (!ok) {
                req.setAttribute("err", "gen");
                req.setAttribute("mode", "confirm");
                req.setAttribute("tournament", tour);
                req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
                return;
            }

            List<Match> created = new ArrayList<>();
            try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                    "SELECT match_id, team_a_id, team_b_id, venue, datetime FROM matches WHERE tournament_id=? ORDER BY match_id")) {
                ps.setLong(1, tid);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Match m = new Match();
                    m.setMatchId(rs.getLong("match_id"));
                    m.setVenue(rs.getString("venue"));
                    m.setDateTime(rs.getTimestamp("datetime").toLocalDateTime());
                    created.add(m);
                }
            } catch (Exception e) { e.printStackTrace(); }

            req.setAttribute("tournament", tour);
            req.setAttribute("mode", "result");
            req.setAttribute("msg", "Fixtures generated successfully.");
            req.setAttribute("matches", created);
            req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
            return;
        }

        req.setAttribute("mode", "confirm");
        req.setAttribute("tournament", tour);
        req.getRequestDispatcher("admin_fixtures.jsp").forward(req, resp);
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return -1L; }
    }
}
