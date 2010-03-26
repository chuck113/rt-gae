<%@ page import="com.rt.web.RhymeUtil" %>
<%@ page import="com.rt.indexing.RhymeLines" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Rhymetime</title>
<!--<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>-->
<meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/>
<link type="text/css" rel="stylesheet" href="stylesheets/main.css"/>
</head>

<body>
<div id="title">Rhyme Time</div>
<p>Type a word to see rap lines that rhyme the word...</p>
<form action="index.jsp">
    <table>
        <tr>
            <td><input type="text" name="word"/></td>
            <td><input type="submit" value="GO"/></td>
        </tr>
    </table>
</form>
<form action="index.jsp">
    <table>
        <tr>
            <td>..or hit for a </td>
            <td><input type="submit" name="random" value="Random Word"/></td>
        </tr>
    </table>
</form>

<%
    String word = request.getParameter("word");
    String random = request.getParameter("random");
    List<RhymeUtil.RhymeData> rhymes = null;
    if(word != null){
        rhymes = RhymeUtil.findRhymes(word);
    }else if(random != null){
        RhymeUtil.RandomResult result = RhymeUtil.random();
        word = result.getWord();
        rhymes = result.getRhymeData();
    }
    if(rhymes == null){
        %><p>No rhymes found for <%=word%></p> <%
    }else{
        %><p>Lines that rhyme <b><%=word%></b></p><%
        for(RhymeUtil.RhymeData rhyme:rhymes){
            %><div class="resultCell">
            <div class="lyricsCell">
                <%
            for(int i=0; i<rhyme.getLines().size(); i++){
                String line = RhymeUtil.wrapLastWordInCss(rhyme.getLines().get(i));
                line = (i < rhyme.getLines().size()-1) ? line+" /":line;
                %><%=line%> <%
            }
               %>
                </div>

        <div class="artistCell"><span class="artistname"><%=rhyme.getArtist()%></span> - <%=rhyme.getTitle()%></div>
    </div>
    <%
        }
    }
%>

</body>
</html>