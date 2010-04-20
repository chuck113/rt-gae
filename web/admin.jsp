
<%@ page import="com.rt.data.DataLoader" %>
<%@ page import="com.rt.data.RhymeDao" %>
<%@ page import="com.rt.data.Rhyme" %>
<%@ page import="java.util.ArrayList" %>
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

<form action="admin.jsp" name="action" method="get">
   <p>Dump all rhymes</p>
   <input type="submit" value="dump" name="action"/>
</form>

<%
    String action = request.getParameter("action");
    System.out.println("JspClass.jsp_service_method action is "+ action);
    if(action != null) {
        if(action.equals("init")){
            
        }else if(action.equals("deleteAll")){
            new DataLoader().deleteAll();
        }else if(action.equals("dump")){
            ArrayList<Rhyme> rhymes = new RhymeDao().allRhymes();
            %>
                <table>
                    <tr>
                        <td>Word</td>
                        <td>Score</td>
                        <td>Matches</td>
                    </tr>
                    <%
            for (Rhyme rhyme : rhymes) {
                   %>
                    <tr>
                        <td><%=rhyme.getWord()%></td>
                    </tr>
                    <%
            }
            %>
                </table>
            <%            
        }

    }
%>

</body>
</html>