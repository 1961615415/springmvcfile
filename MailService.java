package cn.knet.businesstask.util;

import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Autowired(required = false)
    private JavaMailSender mailsender;
    @Autowired(required = false)
    private FreeMarkerConfigurer freeMarker;
    @Value("${spring.mail.username:regist@knetreg.cn}")
    private String from = "regist@knetreg.cn";


    /**
     * 生成html模板字符串
     *
     * @param root 存储动态数据的map
     * @return
     */
    public String getMailText(Map<String, Object> root, String templateName) {
        String htmlText = "";
        try {
            // 通过指定模板名获取FreeMarker模板实例
            Template tpl = freeMarker.getConfiguration().getTemplate(templateName);
            htmlText = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, root);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlText;
    }

    /**
     * 发送邮件
     *
     * @param root    存储动态数据的map
     * @param toEmail 邮件地址
     * @param subject 邮件主题
     * @return
     */
    public boolean sendTemplateMail(Map<String, Object> root, String toEmail, String subject, String templateName) {
        try {
            MimeMessage msg = mailsender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");// 由于是html邮件，不是mulitpart类型
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            String htmlText = getMailText(root, templateName);
            helper.setText(htmlText, true);
            mailsender.send(msg);
            logger.info("邮箱发送完毕：" + toEmail);
            return true;
        } catch (Exception e) {
            logger.info("邮件发送出错" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送邮件
     *
     * @param root    存储动态数据的map
     * @param toEmail 邮件地址
     * @param subject 邮件主题
     * @return
     */
    public boolean sendTemplateMail(Map<String, Object> root, String[] toEmail, String subject, String templateName) {
        try {
            MimeMessage msg = mailsender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");// 由于是html邮件，不是mulitpart类型
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            String htmlText = getMailText(root, templateName);
            helper.setText(htmlText, true);
            mailsender.send(msg);
            logger.info("邮箱发送完毕：" + toEmail);
            return true;
        } catch (Exception e) {
            logger.info("邮件发送出错" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 给多人发邮件，不带附件
     *
     * @param isHtml
     * @param toEmail
     * @param subject
     * @param body
     * @return
     */
    public boolean sendTemplateMail(String[] toEmail, String subject, String body, boolean isHtml) {
        try {
            MimeMessage msg = mailsender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");// 由于是html邮件，不是mulitpart类型
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml);
            mailsender.send(msg);
            logger.info("邮箱发送完毕:" + body);
            return true;
        } catch (Exception e) {
            logger.info("邮件发送出错" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 带附件
     *
     * @param toEmail
     * @param subject
     * @param body
     * @param isHtml
     * @param attachments
     * @return
     */
    public boolean sendTemplateMail(String[] toEmail, String subject, String body, boolean isHtml, String[] attachments) {
        try {
            MimeMessage msg = mailsender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");// 由于是html邮件，不是mulitpart类型
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            for (int i = 0; i < attachments.length; i++) {
                helper.addAttachment("附件" + (i + 1), new File(attachments[i]));
            }
            mailsender.send(msg);
            logger.info("邮箱发送完毕：");
            return true;
        } catch (MailException e) {
            logger.info("邮件发送出错" + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 带附件
     *
     * @param toEmail
     * @param subject
     * @param body
     * @param isHtml
     * @param attachments
     * @return
     */
    public boolean sendTemplateMail(String[] toEmail, String subject, String body, boolean isHtml, String[] attachments, String[] excelName) {
        try {
            logger.info("邮箱发送开启：");
            MimeMessage msg = mailsender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "utf-8");// 由于是html邮件，不是mulitpart类型
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            for (int i = 0; i < attachments.length; i++) {
                helper.addAttachment(excelName[i], new File(attachments[i]));
            }
            mailsender.send(msg);
            logger.info("邮箱发送完毕：");
            return true;
        } catch (MailException e) {
            logger.error("邮箱发送出错{}", e.getMessage());
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            logger.error("邮箱发送出错{}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}