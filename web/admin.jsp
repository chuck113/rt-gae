
<%@ page import="com.rt.data.DataLoader" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Rhymetime Admin</title>
<!--<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>-->
<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
<link type="text/css" rel="stylesheet" href="stylesheets/main.css"/>
</head>

<body>
<form action="admin.jsp" name="action" method="get">
   <p>Configure data store with uploaded data</p> 
   <input type="submit" value="init" name="action"/>
   <input type="submit" value="deleteAll" name="action"/>
</form>

<%
    String action = request.getParameter("action");
    System.out.println("JspClass.jsp_service_method action is "+ action);
    if(action != null) {
        if(action.equals("init")){
            DataLoader.InsertResult result = new DataLoader().load(new com.rt.data.RhymeDao());
            %>
                <p>albums: <%=result.getAlbums()%></p>
                <p>songs: <%=result.getSongs()%></p>
                <p>rhymes: <%=result.getRhymes()%></p>
            <%
        }else if(action.equals("deleteAll")){
            new DataLoader().deleteAll();
        }
    }
%>

</body>
</html>