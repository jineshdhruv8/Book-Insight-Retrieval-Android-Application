package AmazonBook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JINESH on 7/23/2017.
 */
public class TextCleaning {

    public String removePunctuations(String text){

        List<String> punctuationList = new ArrayList<>(Arrays.asList("`", "~", "!", "@", "#", "$", "%", "^", "&", "\\*", "\\(",
                "\\)", "_", "-", "=", "\\+", "\\{", "\\}", ";", ":", "\'", "\"", ",", "<", ">", "\\.", "\\?", "/", "\\[", "\\]", "|", "\\\\"));

        String tokenList[] = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for(String token : tokenList){
            for (String punct: punctuationList){
                token = token.replaceAll(punct, "");
            }
            token = token.trim().replaceAll(" +", " ");   // Remove extra spaces
            sb.append(token);
            sb.append(" ");
        }
        return sb.toString();
    }

    public ArrayList<String> readFile(String absoluteFilePath){
        ArrayList<String> fileContent = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(absoluteFilePath))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                fileContent.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    public String removeStopWords(String text, String path){

        ArrayList<String> stopWordList = readFile(path);

        String tokenList[] = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for(String token : tokenList){
            if (!stopWordList.contains(token)){
                sb.append(token);
                sb.append(" ");
            }
        }
        return sb.toString();
    }



    public String porterStem(String text){

        Stemmer stemmer = new Stemmer();
        String tokenList[] = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for(String token : tokenList){

            int lengthOfSingleWord = token.length();

            // Now iterate for each character in that word
            for (int j = 0; j < lengthOfSingleWord; j++) {
                char eachCharacter = token.charAt(j);
                stemmer.add(eachCharacter);
            }
            stemmer.stem();     // Find stem
            sb.append(stemmer.toString());
            sb.append(" ");
        }
        return sb.toString();
    }

    public static void main(String[] args) {

        TextCleaning textCleaning = new TextCleaning();
        String text = "Game of Thrones is a very well written book, it has an interesting story line that holds you to it throughout.";
//        String t = textCleaning.removePunctuations(text);
//        System.out.println(t);
        String stopWordFilePath = "F:\\RIT Courses\\Knowledge Processing Technology\\Java Codes\\src\\lab4\\stopwords.txt";

        text = textCleaning.removeStopWords(text, stopWordFilePath);
        System.out.println(text);
//        System.out.println(textCleaning.porterStem(t));

    }
}
