package org.superbiz.mdb;


import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;


@Stateless
public class Producer {

    private String smtpHost;
    private String smtpUser;
    private String smtpPassword;
    private Boolean smtpDebug;

    @PostConstruct
    public void init() {
        this.smtpHost = System.getProperty("smtp.host");
        this.smtpUser = System.getProperty("smtp.user");
        // please dont do this on production
        this.smtpPassword = System.getProperty("smtp.password");
        this.smtpDebug = Boolean.getBoolean("smtp.debug");
    }

    @Schedule(hour = "*", minute = "*", second = "*/10", persistent = false)
    public void sendMessage() throws MessagingException, IOException {
        final Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", this.smtpHost);
        props.put("mail.smtp.port", "587");
        props.put("mail.debug", smtpDebug.toString());

        final Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });
        final long ts = System.currentTimeMillis();
        final String text = "Hi from producer [" + ts + "]";
        session.setDebug(this.smtpDebug);
        final MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(this.smtpUser));
        final InternetAddress[] address = {new InternetAddress(this.smtpUser)};
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject(text);
        msg.setSentDate(new Date());

        Multipart multipart = new MimeMultipart();
        MimeBodyPart content = new MimeBodyPart();
        content.setText(text, "UTF-8");
        multipart.addBodyPart(content);
        msg.setContent(multipart);

        File file = new File("/opt/email.jpg");
        MimeBodyPart attachment = new MimeBodyPart();
        FileDataSource source = new FileDataSource(file);
        attachment.setDataHandler(new DataHandler(source));
        attachment.setFileName(file.getName());
        multipart.addBodyPart(attachment);

        msg.writeTo(new FileOutputStream(new File("/opt/messages/", ts + ".eml")));

        Transport.send(msg);
    }

}
