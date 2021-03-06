/*
 * Copyright 1998-2010 Linux.org.ru
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.org.linux.spring;

import java.sql.*;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import ru.org.linux.site.*;
import ru.org.linux.util.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class
  LostPasswordController {
  @RequestMapping(value="/lostpwd.jsp", method= RequestMethod.GET)
  public ModelAndView showForm() {
    return new ModelAndView("lostpwd-form");
  }

  @RequestMapping(value="/reset-password", method= RequestMethod.GET)
  public ModelAndView showCodeForm() {
    return new ModelAndView("reset-password-form");
  }

  @RequestMapping(value="/lostpwd.jsp", method= RequestMethod.POST)
  public ModelAndView sendPassword(@RequestParam("email") String email, HttpServletRequest request) throws Exception {
    Template tmpl = Template.getTemplate(request);

    Connection db = null;
    try {
      db = LorDataSource.getConnection();
      db.setAutoCommit(false);

      PreparedStatement pst = db.prepareStatement("SELECT id, lostpwd>CURRENT_TIMESTAMP-'1 week'::interval as datecheck FROM users WHERE email=? AND not blocked");
      pst.setString(1, email);
      ResultSet rs = pst.executeQuery();

      if (!rs.next()) {
        throw new BadInputException("Ваш email не зарегистрирован");
      }

      User user = User.getUser(db, rs.getInt("id"));
      user.checkBlocked();
      user.checkAnonymous();

      if (user.canModerate()) {
        throw new AccessViolationException("this feature is not for you, ask me directly");
      }

      if (rs.getBoolean("datecheck")) {
        throw new AccessViolationException("нельзя запрашивать пароль чаще одного раза в неделю");
      }

      rs.close();
      pst.close();

      PreparedStatement st = db.prepareStatement("UPDATE users SET lostpwd=? WHERE id=?");
      Timestamp now = new Timestamp(System.currentTimeMillis());

      st.setTimestamp(1, now);
      st.setInt(2, user.getId());

      st.executeUpdate();

      st.close();

      sendEmail(tmpl, user, email, now);

      db.commit();

      st.close();

      return new ModelAndView("action-done", "message", "Инструкция по сбросу пароля была отправлена на ваш email");
    } catch (AddressException ex) {
      throw new UserErrorException("Incorrect email address");
    } finally {
      if (db != null) {
        db.close();
      }
    }
  }

  @RequestMapping(value="/reset-password", method= RequestMethod.POST)
  public ModelAndView resetPassword(
    @RequestParam("nick") String nick,
    @RequestParam("code") String formCode,
    HttpServletRequest request
  ) throws Exception {
    Template tmpl = Template.getTemplate(request);

    Connection db = null;
    try {
      db = LorDataSource.getConnection();
      db.setAutoCommit(false);

      User user = User.getUser(db, nick);
      user.checkBlocked();
      user.checkAnonymous();

      if (user.canModerate()) {
        throw new AccessViolationException("this feature is not for you, ask me directly");
      }

      PreparedStatement pst = db.prepareStatement("SELECT lostpwd FROM users WHERE id=?");
      pst.setInt(1, user.getId());
      ResultSet rs = pst.executeQuery();
      rs.next();
      Timestamp resetDate = rs.getTimestamp("lostpwd");

      String resetCode = getResetCode(tmpl.getSecret(), user.getNick(), user.getEmail(), resetDate);

      if (!resetCode.equals(formCode)) {
        throw new UserErrorException("Код не совпадает");
      }

      String password = user.resetPassword(db);

      db.commit();

      return new ModelAndView("action-done", "message", "Ваш новый пароль: "+password);
    } finally {
      if (db != null) {
        db.close();
      }
    }
  }

  private static String getResetCode(String base, String nick, String email, Timestamp tm) {
    return StringUtil.md5hash(base + ':' + nick + ':' + email+ ':' +Long.toString(tm.getTime())+":reset");
  }

  private static void sendEmail(Template tmpl, User user, String email, Timestamp resetDate) throws MessagingException {
    Properties props = new Properties();
    props.put("mail.smtp.host", "localhost");
    Session mailSession = Session.getDefaultInstance(props, null);

    MimeMessage msg = new MimeMessage(mailSession);
    msg.setFrom(new InternetAddress("no-reply@linux.org.ru"));

    String resetCode = getResetCode(tmpl.getSecret(), user.getNick(), email, resetDate);

    msg.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(email));
    msg.setSubject("Your password @linux.org.ru");
    msg.setSentDate(new Date());
    msg.setText(
      "Здравствуйте!\n\n" +
      "Для сброса вашего пароля перейдите по ссылке http://www.linux.org.ru/reset-password\n\n" +
      "Ваш ник "+user.getNick()+", код подтверждения: " + resetCode + "\n\n" +
      "Удачи!"
    );

    Transport.send(msg);
  }
}
