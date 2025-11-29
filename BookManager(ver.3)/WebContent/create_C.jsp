<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false" import="bookDAO.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	request.setCharacterEncoding("utf-8");
%>
<%
	BooksDAO dao = new BooksDAO();
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Book create</title>
<script type="text/javascript">
	// author db check & add row
	function checkbtn() {
		// db connection
		// Button & text box add
		var oTbl = document.getElementById("author");
		var btn = document.getElementById("checkBtn");
		btn.parentNode.removeChild(btn);
		var oRow = oTbl.insertRow();
		oRow.onmouseover = function() {
			oTbl.clickedRowIndex = this.rowIndex
		}; //clickedRowIndex - 클릭한 Row의 위치를 확인;
		var oCell = oRow.insertCell();

		//삽입될 Form Tag
		var frmTag = "<input type=text name=Bauth_info style=width:150px; height:20px;> ";
		frmTag += "<input type=button id='checkBtn' onclick='checkbtn()' value='+'>";
		oCell.innerHTML = frmTag;
	}
</script>
</head>
<body>
	<form action="/bookmanager/controller/Create_Book.do" method="post">
		도서 번호 : ${booknum} <input type="hidden" name="Bnum_info"
			value="${booknum}"><br><br> 제 목 : <input type="text"
			name="Bname_info"><br> <br>
		<table id="author">
			<tr>
				<td>저 자 :<input type="text" name="Bauth_info"
					style="width: 150px; height: 20px;"></td>
				<td><input type="button" id="checkBtn" onclick="checkbtn()"
					value="+"></td>
			</tr>
			<td><font color="#FF0000">*</font> 이름/생년월일 ex) 홍길동/19900508 </td>
		</table>

		<br> 출 판 사 : <input type="text" name="Bpub_info"><br>
		<br> 상세 설명 : <input type="text" name="Binf_info"><br>
		<input type = "hidden" name = "btn_create" value = "second_visit">
		<br> <input type="submit" value="도서 추가">
	</form>
	<form action="/bookmanager/controller/CreateAuthor.do" method="post">
		<input type="hidden" name="booknum" value="${booknum}"> <input
			type="hidden" name="btn_createA" value="first"> <input
			type="submit" value="작가 추가">
	</form>
	<br>
	<br>
	<form action="/bookmanager/controller/Back.do" method="post">
		<input type="submit" name="btn_Back" value="back">
	</form>
</body>
</html>