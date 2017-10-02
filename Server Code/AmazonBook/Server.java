package AmazonBook;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by JINESH on 8/2/2017.
 */
public class Server extends Thread{

    protected Socket socket;
    String flag;
    static BookReview bookReview;
    static Ranking ranking;

    public Server(String flag, Socket clientSocket) {
        this.socket = clientSocket;
        this.flag = flag;
    }

    public Server(String flag){
        this.flag = flag;
    }

    public void run() {

        if(flag.equals("classifier")){

//            String trainpath = "train.csv";
            String trainpath = "training_Amazon_Book_Reviews.csv";
            try {

                ranking = new Ranking();
                ranking.readFile("AmazonReviewsAll50k.csv");
                bookReview = new BookReview(trainpath);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else{
            OutputStream outToServer;
            InputStream inFromServer;
            DataInputStream stream_input;
            DataOutputStream stream_output;
            ObjectOutputStream objectOutput;
            System.out.println("Connection Successful");
            try {
                outToServer = socket.getOutputStream();
                stream_output = new DataOutputStream(outToServer);
                objectOutput = new ObjectOutputStream(outToServer);

                inFromServer = socket.getInputStream();
                stream_input = new DataInputStream(inFromServer);

                while (true) {
                    try {
                        String identifier = stream_input.readUTF();

                        if(identifier.equals("search")){

//                            ArrayList<String> bookIDList = new ArrayList<>();
                            ArrayList<String> bookIDList = ranking.getAllBookIds();
//                            bookIDList.add("1");bookIDList.add("2");bookIDList.add("3");
                            objectOutput.writeObject(bookIDList);
                            System.out.println("Object Sent");
                        }else if(identifier.equals("rank")){

                            String bookID = stream_input.readUTF();
//                            ArrayList<String> bookReviewList = new ArrayList<>();
                            ArrayList<String> bookReviewList = ranking.process(bookID);
//                            ArrayList<String> bookDateList = new ArrayList<>();
                            ArrayList<String> bookDateList = ranking.dates;
//                            ArrayList<String> ratingList = ranking.ratings;
//                            bookReviewList.add("This book is awesome!!!"); bookReviewList.add("great book to read");
//                            bookDateList.add("8/2/2017"); bookDateList.add("7/26/2017");

                            objectOutput.writeObject(bookReviewList);
                            objectOutput.flush();objectOutput.reset();
                            objectOutput.writeObject(bookDateList);
                            objectOutput.flush();objectOutput.reset();
//                            objectOutput.writeObject(ratingList);
                            System.out.println("Object Sent");

                        } else{
                            System.out.println("\n");
                            System.out.println("Input : ");
                            String userComment = stream_input.readUTF();
                            System.out.println(userComment);
                            String target = (bookReview.classfiy(userComment) == 0)? "Positive Comment": "Negative Comment";
//                            stream_output.writeUTF(target);
                            objectOutput.writeObject(target);
                            System.out.println("Target: "+target);
                        }

                        if ((identifier == null) || identifier.equalsIgnoreCase("QUIT")) {
                            stream_output.flush();
                            stream_input.close();
                            stream_output.close();
                            socket.close();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
