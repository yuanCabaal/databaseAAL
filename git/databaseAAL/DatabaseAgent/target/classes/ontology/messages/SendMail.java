package ontology.messages;

import ontology.Message;
import ontology.MessageType;
import ontology.messages.MailData.Mail;

public class SendMail extends Message{

   int userID;
   Mail mail;

   public SendMail(String senderID, String receiverID, int userID, Mail mail) {
       super(senderID, receiverID, MessageType.SAVE_COMM);
       this.userID = userID;
       this.mail = mail;
   }

   public int getUserID() {
       return userID;
   }

   public Mail getMail() {
       return mail;
   }
}