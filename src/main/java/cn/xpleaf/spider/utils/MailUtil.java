package cn.xpleaf.spider.utils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * java mail的工具类
 * <p>
 * javax.mail.AuthenticationFailedException: 550 ÓÃ»§ÎÞÈ¨µÇÂ½ 使用java等各种语言进行操作邮箱的时候，
 * 必须要给邮箱设置客户端授权码，不然我们无法发送邮件，可以将授权码当密码来发送邮件
 * <p>
 */
public class MailUtil {
    public static void sendMail(String subject, String content) {
        Transport t = null;
        Session session = null;
        try {
            // 会话需要的相关信息
            Properties prop = new Properties();
            prop.setProperty("mail.transport.protocol", "smtp");// 发送邮件的协议
            prop.setProperty("mail.smtp.host", "smtp.126.com");// 使用的邮箱服务器
            prop.setProperty("mail.smtp.auth", "true");
            session = Session.getInstance(prop);
            session.setDebug(false);//开启调试模式
            // 创建邮件
            MimeMessage message = new MimeMessage(session);
            InternetAddress fromAddr = new InternetAddress("429191942@qq.com", "爬虫节点监控系统");// 发件人的信息，请填写自己的
            InternetAddress toAddr = new InternetAddress("xpleaf@163.com", "叶子");// 收件人的信息，请填写自己的
            message.setFrom(fromAddr);// 在信封上写上
            message.setRecipient(Message.RecipientType.TO, toAddr);

            message.setSubject(subject);//邮件主题
            message.setContent(content, "text/html;charset=UTF-8");//邮件正文

            // 发送邮件
            t = session.getTransport();
            t.connect("lhsteach2017@126.com", "abc123123");//这里登陆的时候最好使用126邮箱经过认证之后的密码，请填写自己的
            t.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (t != null) {
                    t.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        sendMail("大数据学习", "坚定不移地走大数据之路");
    }
}
