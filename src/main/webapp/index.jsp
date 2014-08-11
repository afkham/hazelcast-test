<%@ page import="org.wso2.hazelcast.HzTest" %>
<%@ page import="java.util.UUID" %>
<%
    HzTest.putItem(UUID.randomUUID().toString());
%>