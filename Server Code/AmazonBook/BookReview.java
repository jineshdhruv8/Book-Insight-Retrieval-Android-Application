package AmazonBook;

/**
 * Created by JINESH on 7/24/2017.
 */

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;



public class BookReview {

    HashMap<String, ArrayList<String>> bookReviewDoc;
    ArrayList<Integer> classLabel;
    ArrayList<String> reviewList;
    ArrayList<String> bookIdList;
    ArrayList<Integer> ratingList;

    String trainingDocs[], testingDocs[];
    int trainingLabels[], testingLabels[], predictTestingLabel[];
    int correctCount=0;

    int numClasses;
    int[] classCounts; //number of docs per class
    String[] classStrings; //concatenated string for a given class
    int[] classTokenCounts; //total number of tokens per class
    HashMap<String,Double>[] condProb;
    HashSet<String> vocabulary; //entire vocabulary
    TextCleaning textCleaning;
    Map<String, Integer> termFrequency = new HashMap<>();


    BookReview(String trainDataFile) throws IOException {
        textCleaning = new TextCleaning();
        long startTime = System.currentTimeMillis();
        classLabel = new ArrayList<>();
        reviewList = new ArrayList<>();
        bookIdList = new ArrayList<>();
        ratingList = new ArrayList<>();

        Reader in = new FileReader(trainDataFile);
        int x =0;
        for(CSVRecord rec : CSVFormat.EXCEL.parse(in)){

            if(x > 0){
                // After skipping the header
                StringBuilder sb = new StringBuilder();
                sb.append(rec.get(0));  // append review
                String cleanWord = sb.toString().toLowerCase();

                // Text cleaning
                cleanWord = textCleaning.removePunctuations(cleanWord);
                cleanWord = textCleaning.removeStopWords(cleanWord, "stopwords.txt");
                cleanWord = textCleaning.porterStem(cleanWord);

                reviewList.add(cleanWord);
                classLabel.add(Integer.parseInt(rec.get(4)));   // add sentiment (class label)
                bookIdList.add(rec.get(1)); // add book id
                ratingList.add(Integer.parseInt(rec.get(3))); // add rating
            }
            x++;
        }

        int length = reviewList.size();
        trainingDocs = new String[length];
        trainingLabels = new int[length];

        for (int i = 0; i < length; i++) {
            trainingDocs[i] = reviewList.get(i);
            trainingLabels[i] = (int)classLabel.get(i);
        }

        numClasses = 2;

        classCounts = new int[numClasses];
        classStrings = new String[numClasses];
        classTokenCounts = new int[numClasses];
        condProb = new HashMap[numClasses];
        vocabulary = new HashSet<String>();
        for(int i=0;i<numClasses;i++){
            classStrings[i] = "";
            condProb[i] = new HashMap<String,Double>();
        }
        for(int i=0;i<trainingLabels.length;i++){
            classCounts[trainingLabels[i]]++;
            classStrings[trainingLabels[i]] += (trainingDocs[i] + " ");
        }

        for(int i=0;i<numClasses;i++){
            String[] tokens = classStrings[i].split(" ");
            classTokenCounts[i] = tokens.length;
            //collecting the counts
            for(String token:tokens){
                vocabulary.add(token);
                if (termFrequency.containsKey(token)){
                    termFrequency.put(token,termFrequency.get(token) + new Integer(1));
                }else{
                    termFrequency.put(token, 1);
                }
                if(condProb[i].containsKey(token)){
                    double count = condProb[i].get(token);
                    condProb[i].put(token, count+1);
                }
                else
                    condProb[i].put(token, 1.0);
            }
        }

        //computing the class conditional probability
        for(int i=0;i<numClasses;i++){
            Iterator<Map.Entry<String, Double>> iterator = condProb[i].entrySet().iterator();
            int vSize = vocabulary.size();
            while(iterator.hasNext())
            {
                Map.Entry<String, Double> entry = iterator.next();
                String token = entry.getKey();
                Double count = entry.getValue();
                count = (count+1)/(classTokenCounts[i]+vSize);
                condProb[i].put(token, count);
            }
        }
        System.out.println("Vocabulary Size = "+ vocabulary.size());

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to build the model: " + (Math.round((endTime - startTime) * 100.0) / 100.0) / 60000 + " min");
//        display(condProb);
    }

    void display(){

        for(Map.Entry<String, Integer> entry : termFrequency.entrySet())
        {   //print keys and values
            if (entry.getValue() > 5)
            {
                System.out.println(entry.getKey() + " : " +entry.getValue());
            }
        }

    }

    void display(HashMap<String,Double>[] prob){
        for (int i = 0; i < 2; i++) {

            HashMap<String,Double> temp = prob[i];
//            System.out.println(temp.entrySet());
            for(Map.Entry<String, Double> entry : temp.entrySet())
            {   //print keys and values

                System.out.println(entry.getKey() + " : " +entry.getValue());

            }
            System.out.println();
            System.out.println();
            break;
        }


    }


    /**
     * Classify a test doc
     * @param doc test doc
     * @return class label
     */
    public int classfiy(String doc){
        int label = 0;
        int vSize = vocabulary.size();
        double[] score = new double[numClasses];
        for(int i=0;i<score.length;i++){
            score[i] = Math.log(classCounts[i] * 1.0 / trainingDocs.length);
        }
        String[] tokens = doc.split(" ");
        for(int i=0;i<numClasses;i++){
            for(String token: tokens){
                if(condProb[i].containsKey(token))
                    score[i] += Math.log(condProb[i].get(token));
                else
                    score[i] += Math.log(1.0/(classTokenCounts[i]+vSize));
            }
        }
        double maxScore = score[0];
        for(int i=0;i<score.length;i++){
            if(score[i]>maxScore)
                label = i;
        }

        return label;
    }



    /**
     *  Classify a set of testing documents and report the accuracy
     * @param testDataFolder fold that contains the testing documents
     * @return classification accuracy
     */
    public double classifyAll(String testDataFolder) throws IOException {

        long startTime = System.currentTimeMillis();
        ArrayList<Integer> testclassLabel = new ArrayList<>();
        ArrayList<String> testreviewList = new ArrayList<>();
        ArrayList<String> testbookIdList = new ArrayList<>();
        ArrayList<Integer> testratingList = new ArrayList<>();

        Reader in = new FileReader(testDataFolder);
        int x =0;
        for(CSVRecord rec : CSVFormat.EXCEL.parse(in)){

            if(x > 0){
                // After skipping the header
                StringBuilder sb = new StringBuilder();
                sb.append(rec.get(0));  // append review
                String cleanWord = sb.toString().toLowerCase();

                // Text Cleaning
                cleanWord = textCleaning.removePunctuations(cleanWord);
                cleanWord = textCleaning.removeStopWords(cleanWord, "stopwords.txt");
                cleanWord = textCleaning.porterStem(cleanWord);
                testreviewList.add(cleanWord);
                testclassLabel.add(Integer.parseInt(rec.get(4)));   // add sentiment (class label)
                testbookIdList.add(rec.get(1)); // add book id
                testratingList.add(Integer.parseInt(rec.get(3))); // add rating
            }
            x++;
        }

        int length = testreviewList.size();
        testingDocs = new String[length];
        testingLabels = new int[length];
        predictTestingLabel = new int[length];
        for (int i = 0; i < length; i++) {
            testingDocs[i] = testreviewList.get(i);
            testingLabels[i] = testclassLabel.get(i);
            predictTestingLabel[i] = classfiy(testreviewList.get(i));
            if ( predictTestingLabel[i] == testingLabels[i]){
                correctCount++;
            }
        }

        System.out.println("Correctly classified " + correctCount + " out of " + length);
        System.out.println("Accuracy: "+ (double)(correctCount * 1.0 / length * 1.0)*100);

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken to test the model: "+ (Math.round((endTime - startTime) * 100.0) / 100.0) / 60000 +" min");

        return correctCount/length;
    }


    public static void main(String[] args) throws IOException {
//        String trainpath = "training_Amazon_Book_Reviews.csv";
        String trainpath = "train.csv";
        BookReview bookReview = new BookReview(trainpath);
        System.out.println();
//        String testpath = "testing_Amazon_Book_Reviews.csv";
        String testpath = "test.csv";
        bookReview.classifyAll(testpath);

    }


}
/*
Vocabulary Size = 2321
Time taken to build the model: 0.008116666666666666 min

Correctly classified 11 out of 13
Accuracy: 84.61538461538461
Time taken to test the model: 6.0E-4 min

*
* */