package com.mail.serviceImpl;

import org.springframework.stereotype.Service;

import com.mail.service.MailReaderService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;

@Service(value = "mailReader")
public class MailReaderServiceImpl implements MailReaderService {

	@Override
	public Boolean readMailsFromExchangeServer() {
		// TODO Auto-generated method stub
		
		 String host = "mail.livemail.co.uk";
	     String port = "143";
	     String userName = "van@accsolve.co.uk";
	     String password = "Subash133";
	     
	     setSaveDirectory(saveDirectory);
	     downloadEmailAttachments(host, port, userName, password);
	        
	        
	     return true;
	}
	
	private String saveDirectory;

    /**
     * Sets the directory where attached files will be stored.
     * @param dir absolute path of the directory
     */
    public void setSaveDirectory(String dir) {
        this.saveDirectory = dir;
    }

    /**
     * Downloads new messages and saves attachments to disk if any.
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    public void downloadEmailAttachments(String host, String port,
                                         String userName, String password) {
        Properties properties = new Properties();

        // server setting

        //Set properties
        Properties props = new Properties();
        props.put("mail.store.protocol", "IMAP");
        props.put("mail.IMAP.host", host);
        props.put("mail.IMAP.port", "143");
        props.put("mail.IMAP.starttls.enable", "true");

        Session session = Session.getDefaultInstance(properties);

        try {
            // connects to the message store
            Store store = session.getStore("imaps");
            store.connect(host, userName, password);

//            Store storeArchive = session.getStore("");
//            storeArchive.connect(host, userName, password);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            Folder destFolder = store.getFolder("Archive");

            Folder[] f = store.getDefaultFolder().list();
            for(Folder fd:f){


                System.out.println("-------"+fd.getName()+"------");


            }
            folderInbox.open(Folder.READ_WRITE);
            destFolder.open(Folder.READ_WRITE);

            // fetches new messages from server
            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = 0; i < arrayMessages.length; i++) {
                System.out.println(" Message number  ********************** " + i);
                Message message = arrayMessages[i];
                if (!message.getFlags().contains(Flags.Flag.DELETED)) {
                    Address[] fromAddress = message.getFrom();
                    String from = fromAddress[0].toString();
                    String subject = message.getSubject();
                    String sentDate = message.getSentDate().toString();

                    String contentType = message.getContentType();
                    String messageContent = "";
                    
                    String senderDirectory = System.getProperty("user.home") + "/project" + "/" + from;
                    
                    if (!Files.isDirectory(Paths.get(senderDirectory))) {
                        System.out.println("Output Files parent path does not exist:"+senderDirectory);
                        File file = new File(senderDirectory);
                        if (!file.exists()) {
                            if (file.mkdir()) {
                                System.out.println("Output files directory is created!");
                            } else {
                                System.out.println("Failed to create output directory!");
                            }
                        }
                    }
                    

                    // store attachment file name, separated by comma
                    String attachFiles = "";

                    if (contentType.contains("multipart")) {
                        // content may contain attachments
                        Multipart multiPart = (Multipart) message.getContent();
                        int numberOfParts = multiPart.getCount();
                        for (int partCount = 0; partCount < numberOfParts; partCount++) {
                            MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                                // this part is attachment
                                String fileName = part.getFileName();
                                attachFiles += fileName + ", ";
                                part.saveFile(senderDirectory + File.separator + fileName);
                            } else {
                                // this part may be the message content
                                messageContent = part.getContent().toString();
                            }
                        }

                        if (attachFiles.length() > 1) {
                            attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
                        }
                    } else if (contentType.contains("text/plain")
                            || contentType.contains("text/html")) {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    }

                    // print out details of each message
                    
                    System.out.println("Message #" + (i + 1) + ":");
                    System.out.println("\t From: " + from);
                    System.out.println("\t Subject: " + subject);
                    System.out.println("\t Sent Date: " + sentDate);
                    System.out.println("\t Message: " + messageContent);
                    System.out.println("\t Attachments: " + attachFiles);

                }
            }
            folderInbox.copyMessages(arrayMessages, destFolder);
//            for (Message message: arrayMessages) {
//                message.setFlag(Flags.Flag.DELETED, true);
//            }

            Flags deleted = new Flags(Flags.Flag.DELETED);
            folderInbox.setFlags(arrayMessages, deleted, true);

            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for pop3.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
