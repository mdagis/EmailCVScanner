package gr.cloudbiz.emailcvscanner;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.*;
import static javax.mail.internet.MimeUtility.decodeText;
import javax.mail.search.FlagTerm;

public class App {

    static String mailpattern = "(^$|[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)";
    static String mobilepattern = "(69[\\d\\s]{1}.{7,10})";
    static String ppp = "\nCell Phone: +30694 9255 115  asqwwq\n";

    public static void main(String args[]) throws IOException {

        if (args.length == 0) {
            System.out.println("no password provided");
        }

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore("imaps");
            store.connect("mailserveraddress", "username", args[0]);
            System.out.println(store);

            Folder inbox = store.getFolder("Inbox/CV/Pass");
            inbox.open(Folder.READ_ONLY);
            FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
            //Message messages[] = inbox.search(ft);
            Message messages[] = inbox.getMessages();

            int i = 0;
            for (Message message : messages) {

                Multipart mp = (Multipart) message.getContent();
                Object p = mp.getBodyPart(0).getContent();

                while (p instanceof Multipart) {
                    Multipart mpBranch = (Multipart) p;
                    p = mpBranch.getBodyPart(0).getContent();
                }
                String q = p.toString();//object has the body content

                // parse telephones
                Pattern pattern = Pattern.compile(mobilepattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(q);
                String telephoneList = "";
                while (matcher.find()) {
                    telephoneList += " " + matcher.group();
                }

                //parse emails
                pattern = Pattern.compile(mailpattern, Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(q);
                String emailList = "";
                if (matcher.find()) {
                    emailList = matcher.group();
                }


                String subj = decodeText(message.getFrom()[0].toString());

                String name;

                if (subj.indexOf("utf-8") > 0) {
                    String s1 = subj.substring(0, subj.indexOf("utf-8"));
                    String s2 = subj.substring(subj.indexOf("utf-8") - 2);
                    s2 = decodeText(s2);
                    if (s2.indexOf("via") > 0) {
                        s2 = s2.substring(0, s2.indexOf("via"));
                    }
                    name = s1 + s2;
                } else {
                    name = subj.substring(0, subj.indexOf(" <"));
                }



                System.out.println(name + ";" + telephoneList + ";" + emailList);
                i++;

//                if (i == 3) {
//                    break;
//                }
            }

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.exit(2);
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

    }
}
