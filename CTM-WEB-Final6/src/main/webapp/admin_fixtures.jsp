<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, com.ctm.model.Tournament, com.ctm.model.Match" %>
<%
  String role=(String)session.getAttribute("role");
  if(role==null||!"admin".equalsIgnoreCase(role)){response.sendRedirect("index.jsp");return;}
  response.setHeader("Cache-Control","no-cache,no-store,must-revalidate");
  response.setHeader("Pragma","no-cache");
  response.setDateHeader("Expires",0);

  String mode=(String)request.getAttribute("mode");
  if(mode==null)mode="list";
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Generate Fixtures</title>
<link rel="stylesheet" href="resources/css/admin_fixtures.css">
</head>
<body>
<div class="top">
  <div class="brand">🏏 Generate Fixtures</div>
  <div><a href="adminmain.jsp" class="logout">Home</a><a href="logout" class="logout">Logout</a></div>
</div>

<div class="wrap">
<% if("list".equals(mode)){ %>
  <h2>All Tournaments</h2>
  <p>Select one to generate fixtures.</p>
<% } else if("confirm".equals(mode)){ 
     Tournament t=(Tournament)request.getAttribute("tournament");
     String err=(String)request.getAttribute("err");
%>
  <h2>Generate Fixtures — <%=t.getName()%></h2>
  <% if("notenough".equals(err)){ %><div class="msg">⚠ Need at least 3 teams.</div><% } %>
  <% if("squads".equals(err)){ %><div class="msg">⚠ All teams must have 11 players.</div><% } %>
  <% if("exists".equals(err)){ %><div class="msg">ℹ Fixtures already exist.</div><% } %>

  <form action="fixturesgen" method="get">
    <input type="hidden" name="tid" value="<%=t.getId()%>">
    <input type="hidden" name="action" value="run">
    <label>Venue</label>
    <select name="venue" required>
      <option value="">-- Select Stadium --</option>
      <% for(com.ctm.model.Stadium s:com.ctm.model.Stadium.values()){ %>
        <option value="<%=s.getFullName()%>"><%=s.getFullName()%></option>
      <% } %>
    </select>
    <button class="btn btn-primary">Generate Fixtures</button>
  </form>
<% } else if("result".equals(mode)){ 
     Tournament t=(Tournament)request.getAttribute("tournament");
     String msg=(String)request.getAttribute("msg");
     List<Match> matches=(List<Match>)request.getAttribute("matches");
%>
  <h2><%=t.getName()%> Fixtures</h2>
  <% if(msg!=null){ %><div class="msg">✅ <%=msg%></div><% } %>
  <table>
    <tr><th>#</th><th>Date/Time</th><th>Venue</th></tr>
    <% int i=1; for(Match m:matches){ %>
      <tr><td><%=i++%></td><td><%=m.getDateTime()%></td><td><%=m.getVenue()%></td></tr>
    <% } %>
  </table>
<% } %>
</div>

<script>
if(window.history.replaceState) window.history.replaceState(null,null,window.location.href);
window.onpopstate=function(){window.location.replace("adminmain.jsp");};
</script>
</body>
</html>
