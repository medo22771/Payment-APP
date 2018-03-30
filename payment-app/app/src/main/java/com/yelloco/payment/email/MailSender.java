package com.yelloco.payment.email;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.yelloco.payment.DevicePreferences;
import com.yelloco.payment.R;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

/**
 * Contributors:
 * Peter Janicka (peterj@amarulasolutions.com)
 */

public class MailSender extends Authenticator {

    private static final String TAG = "MailSender";

    private String user;
    private String password;
    private Session session;

    private Context mContext;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(Context context) {

        mContext = context;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        user = DevicePreferences.EMAIL_ADDRESS.getValue(pref);
        password = DevicePreferences.EMAIL_PASSWORD.getValue(pref);
        String host = DevicePreferences.EMAIL_HOST.getValue(pref);

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");// 587
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized boolean sendMail(String subject, String body,
                                      String recipients) {
        Log.d(TAG, "Sending email...");
        try {
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(
                    body.getBytes(), "text/plain"));
            InternetAddress address = new InternetAddress(user, mContext.getString(R.string.app_name));
            message.setSender(address);
            message.setFrom(address);
            message.setSentDate(new Date());
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipients));
            Transport.send(message);
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
            return false;
        }
        return true;
    }

    public synchronized void sendMail(String subject, String body, String senderEmail,
                                      String recipients, String attachementFile) {
        //TODO: send mail with attachement
    }


}
