<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="ru.org.linux.site.User,ru.org.linux.site.UserInfo"  %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="lor" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
  ~ Copyright 1998-2010 Linux.org.ru
  ~    Licensed under the Apache License, Version 2.0 (the "License");
  ~    you may not use this file except in compliance with the License.
  ~    You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~    Unless required by applicable law or agreed to in writing, software
  ~    distributed under the License is distributed on an "AS IS" BASIS,
  ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~    See the License for the specific language governing permissions and
  ~    limitations under the License.
  --%>

<%--@elvariable id="userInfoText" type="java.lang.String"--%>
<%--@elvariable id="error" type="java.lang.String"--%>

<jsp:include page="head.jsp"/>

<title>Регистрация пользователя</title>
<script src="/js/jquery.validate.pack.js" type="text/javascript"></script>
<script src="/js/jquery.validate.ru.js" type="text/javascript"></script>
<script type="text/javascript">
  $(document).ready(function() {
    $("#registerForm").validate({
      rules : {
        password2: {
          equalTo: "#password"
        }
      }
    });
    $("#changeForm").validate();    
  });
</script>

<jsp:include page="header.jsp"/>
<%
  response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
%>
  <table class=nav><tr>
    <td id="navPath" align=left valign=middle>
      Изменение регистрации
    </td>

    <td align=right valign=middle>
      [<a style="text-decoration: none" href="../../addphoto.jsp">Добавить фотографию</a>]
      [<a style="text-decoration: none" href="../../rules.jsp">Правила форума</a>]
     </td>
    </tr>
 </table>
<h1 class="optional">Изменение регистрации</h1>
<%
  User user = (User) request.getAttribute("user");
  UserInfo userInfo = (UserInfo) request.getAttribute("userInfo");
%>

<c:if test="${error!=null}">
  <div class="error">Ошибка: ${error}</div>
</c:if>

<form method=POST action="register.jsp" id="changeForm">
<input type=hidden name=mode value="change">
Полное имя:
<input type=text name="name" size="40" value="<%
if (user.getName()!=null) {
 out.print(user.getName());
}
%>"><br>
Пароль:
<input class="required" type=password name="oldpass" size="20"><br>
Новый пароль:
<input type=password name="password" size="20"> (не заполняйте если не хотите менять)<br>
Повторите новый пароль:
<input type=password name="password2" size="20"><br>
URL:
<input type=text name="url" size="50" value="<%
	if (userInfo.getUrl()!=null) {
      out.print(userInfo.getUrl());
    }
%>"><br>
(не забудьте добавить <b>http://</b>)<br>
Email:
<input type=text class="required email" name="email" size="50" value="<%= user.getEmail() %>"><br>
Город (просьба писать русскими буквами без сокращений, например: <b>Москва</b>,
<b>Нижний Новгород</b>, <b>Троицк (Московская область)</b>):
<input type=text name="town" size="50" value="<%
 if (userInfo.getTown()!=null) {
  out.print(userInfo.getTown());
 }
%>"><br>
<label>Дополнительная информация:<br>
  <textarea name=info cols=50 rows=5>${userInfoText}</textarea>
</label>
<br>
<input type=submit value="Обновить">
</form>
<jsp:include page="footer.jsp"/>
