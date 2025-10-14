<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament" %>
<%
  String role = (String) session.getAttribute("role");
  if (role == null || !"admin".equalsIgnoreCase(role)) { response.sendRedirect("index.jsp"); return; }
  response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  List<Tournament> tournaments = (List<Tournament>) request.getAttribute("tournaments");
  Set<Long> lockedIds = (Set<Long>) request.getAttribute("lockedIds"); // from servlet
  if (lockedIds == null) lockedIds = Collections.emptySet();
%>


<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Select a Tournament</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="resources/css/admin_enroll_select.css">
</head>
<body>
<div class="top">
  <div class="brand">🏏 Enroll Teams — Select a Tournament</div>
  <div class="nav">
    <a href="adminmain.jsp">Home</a>
    <a href="logout">Logout</a>
  </div>
</div>

<div class="wrap">
  <h2>Select a Tournament</h2>
  <p class="muted">If fixtures are already generated for a tournament, enrollment is locked. You can still open it to view teams.</p>

  <% if (tournaments == null || tournaments.isEmpty()) { %>
    <p>No tournaments found.</p>
  <% } else {
       for (Tournament t : tournaments) {
         boolean locked = lockedIds.contains(t.getId());
  %>
    <div class="row">
      <div>
        <b><%= t.getName() %></b> &nbsp; <span class="muted">(<%= t.getFormat() %>)</span>
        <% if (locked) { %><span class="badge lock">FIXTURES GENERATED</span><% } %>
      </div>
      <div>
        <a class="pill view" href="enroll?tid=<%= t.getId() %>">Open</a>
      </div>
    </div>
  <% } } %>
</div>


</body>
</html>
