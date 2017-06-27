<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="head.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>文件上传成功</title>
</head>
<body>
文件上传成功
<c:forEach items="${filename }" var="file">
<img id="myImage" alt="文件" src="${ctx}/${file}"/>
</c:forEach>

<c:forEach items="${filenamesy }" var="file">
<img id="myImage" alt="文件" src="${ctx}/${file}"/>
</c:forEach>

<c:forEach items="${filenamesyt }" var="file">
<img id="myImage" alt="文件" src="${ctx}/${file}"/>
</c:forEach>
</body>
</html>