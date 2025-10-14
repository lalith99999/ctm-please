<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Team, com.ctm.model.TeamStanding, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  Tournament tour = (Tournament) request.getAttribute("tournament");
  List<TeamStanding> enrolled = (List<TeamStanding>) request.getAttribute("enrolled");
  List<Team> available = (List<Team>) request.getAttribute("available");
  Boolean locked = (Boolean) request.getAttribute("locked");
  if (locked == null) locked = Boolean.FALSE;

  String msg = (String) request.getAttribute("msg");
  String err = (String) request.getAttribute("err");
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Enroll Teams — <%= tour.getName() %></title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="resources/css/admin_enroll.css">
</head>
<body>
<div class="top">
  <div class="brand">🏏 Enroll Teams — <b><%= tour.getName() %></b>
    <% if (locked) { %><span class="badge lock">ENROLLMENT LOCKED</span><% } %>
  </div>
  <div class="nav">
    <a href="admtournaments">Back</a>
    <a href="adminmain.jsp">Home</a>
    <a href="logout">Logout</a>
  </div>
</div>

<div class="wrap">
  <% if ("added".equalsIgnoreCase(msg)) { %>
    <div class="toast ok">✅ Team added to the tournament.</div>
  <% } else if ("removed".equalsIgnoreCase(msg)) { %>
    <div class="toast ok">✅ Team removed from the tournament.</div>
  <% } else if ("locked".equalsIgnoreCase(err)) { %>
    <div class="toast err">🔒 Enrollment is locked because fixtures already exist for this tournament.</div>
  <% } else if ("1".equals(err)) { %>
    <div class="toast err">❌ Operation failed. Please try again.</div>
  <% } %>

  <% if (locked) { %>
    <div class="banner lock">
      <div>🔒</div>
      <div>
        <b>Enrollment locked.</b>
        <div class="meta">Fixtures are already generated. You can view teams below, but adding or removing is disabled.</div>
      </div>
    </div>
  <% } else { %>
    <div class="banner ok">
      <div>✅</div>
      <div>
        <b>Enrollment open.</b>
        <div class="meta">You can add or remove teams until fixtures are generated.</div>
      </div>
    </div>
  <% } %>

  <div class="chip">Tournament ID: <%= tour.getId() %></div>
  <div class="chip">Format: <%= tour.getFormat() %></div>
  <div class="chip">Enrolled: <%= (enrolled==null?0:enrolled.size()) %></div>

  <div class="cols">
    <div class="card">
      <h2>Available Teams</h2>
      <table>
        <tr><th style="width:70px">ID</th><th>Name</th><th>City</th><th class="right">Action</th></tr>
        <% if (available==null || available.isEmpty()) { %>
          <tr><td colspan="4" class="empty">No more teams to enroll.</td></tr>
        <% } else { for (Team t : available) { %>
          <tr>
            <td><%= t.getId() %></td>
            <td><%= t.getName() %></td>
            <td><%= t.getCity() %></td>
            <td class="right">
              <% if (locked) { %>
                <span class="pill disabled">Add</span>
              <% } else { %>
                <a class="pill add" href="enroll?action=add&tid=<%= tour.getId() %>&teamId=<%= t.getId() %>">Add</a>
              <% } %>
            </td>
          </tr>
        <% } } %>
      </table>
    </div>

    <div class="card">
      <h2>Enrolled Teams</h2>
      <table>
        <tr><th style="width:70px">ID</th><th>Name</th><th>City</th><th>Points</th><th>Played</th><th class="right">Action</th></tr>
        <% if (enrolled==null || enrolled.isEmpty()) { %>
          <tr><td colspan="6" class="empty">No teams enrolled yet.</td></tr>
        <% } else { for (TeamStanding ts : enrolled) { %>
          <tr>
            <td><%= ts.getTeamId() %></td>
            <td><%= ts.getName() %></td>
            <td><%= ts.getCity() %></td>
            <td><%= ts.getPoints() %></td>
            <td><%= ts.getPlayed() %></td>
            <td class="right">
              <% if (locked) { %>
                <span class="pill disabled">Remove</span>
              <% } else { %>
                <a class="pill del" href="enroll?action=remove&tid=<%= tour.getId() %>&teamId=<%= ts.getTeamId() %>"
                   onclick="return confirm('Remove this team from the tournament?');">Remove</a>
              <% } %>
            </td>
          </tr>
        <% } } %>
      </table>
    </div>
  </div>
</div>


</body>
</html>
