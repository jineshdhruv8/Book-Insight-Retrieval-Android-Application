package AmazonBook;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Ranking {
	HashMap<String, ArrayList<ReviewDetails>> data;
	ArrayList<ReviewDetails> positivereviews = new ArrayList<ReviewDetails>();
	ArrayList<ReviewDetails> negativereviews = new ArrayList<ReviewDetails>();
//	static ArrayList<Date> dates = new ArrayList<Date>();
//	static ArrayList<String> ratings = new ArrayList<String>();

	static ArrayList<String> dates = new ArrayList<String>();
	DateFormat df = new SimpleDateFormat("MM/DD/YYYY");

	long maxm = 0;
	//long minm = 9999999;

	public void readFile(String filename) throws IOException, ParseException {
		data = new HashMap<String, ArrayList<ReviewDetails>>();

		Reader in = new FileReader(filename);
		int i = 0;
		for (CSVRecord rec : CSVFormat.EXCEL.parse(in)) {
//			if (i == 10) {
//				break;
//			}
			if (i != 0) {
				String bookId = rec.get(7);
				String reviewText = rec.get(2);
				int rating = Integer.parseInt(rec.get(4).substring(0, 1));
				String date = rec.get(8);
				ReviewDetails rd = new ReviewDetails(rating, date, reviewText);
				populateData(bookId, rd);
			}

			i++;
		}
		
		

	}

	public ArrayList<String> process(String bookid) throws ParseException {
		ArrayList<String> reviews = new ArrayList<String>();
		
		if (data.containsKey(bookid)) {
			ArrayList<ReviewDetails> details = data.get(bookid);

//			System.out.println("Total Reviews:" + details.size());
			for (ReviewDetails rd : details) {
				statistics(rd);
			}

			Collections.sort(positivereviews, new DetailsComparator());
			Collections.sort(negativereviews, new DetailsComparator());

			int npos = positivereviews.size();
			int nneg = negativereviews.size();
			
//			System.out.println("npos :" + npos);
//			System.out.println("nneg :" + nneg);

			int total = npos + nneg;
			
			
			int poslimit = (int)(Math.floor(((double)npos / (double)total) * 10))	;
			
			int i = 0;
			while(i <poslimit && !positivereviews.isEmpty() ) {
//				dates.add(positivereviews.get(0).date);
				dates.add(df.format(positivereviews.get(0).date));
				reviews.add(positivereviews.remove(0).reviewText);
//				ratings.add(Integer.toString(positivereviews.get(0).rating));
				i++;
			}

			int neglimit = 10 - poslimit;

			int j = 0;
			while(j <neglimit && !negativereviews.isEmpty() ) {
//				dates.add(negativereviews.get(0).date);
				dates.add(df.format(negativereviews.get(0).date));
				reviews.add(negativereviews.remove(0).reviewText);
//				ratings.add(Integer.toString(negativereviews.get(0).rating));
				j++;
			}

//			System.out.println(ratings);
			return reviews;

		} else {
			return null;
		}



	}

	public void statistics(ReviewDetails rd) throws ParseException {
		int datescore = 0;
		int ratingscore = 0;
		int textscore = 0;
		
		
		Date currentdate = new SimpleDateFormat("mm dd,yyyy").parse("07 17,2014");
		
		long months = (currentdate.getYear() - rd.date.getYear()) * 12 + (currentdate.getMonth() - rd.date.getMonth());
		
		//System.out.println("Month diff :" + months);
		if(months >= 0 && months < 33){
			datescore = 35;
		}else if(months >= 33 && months < 66){
			datescore = 28;
		}else if(months >= 66 && months < 99){
			datescore = 21;
		}else if(months >= 99 && months < 132){
			datescore = 7;
		}else
			datescore = 0;
		
		//System.out.println("Date Score :" + datescore);
		
		ratingscore = rd.rating * 3;
		
		//System.out.println("Rating Score :" + ratingscore);
		
		int length = rd.reviewText.length();
		
		//System.out.println("Review Length :" + length);
		if(length >= 4000){
			textscore = 25; 
		}else if(length < 4000 && length >= 3000){
			textscore = 20;
		}else if(length < 3000 && length >= 2000){
			textscore = 15;
		}else if(length < 2000 && length >= 1000){
			textscore = 10;
		}else
			textscore = 5;
		
		if(length == 0){
			textscore = 0;
		}
		
		//System.out.println("Text Score :" + textscore);
		
		rd.score = datescore + ratingscore + textscore;

		//System.out.println("Total Score " + rd.score);
		if (rd.rating > 3) {
			positivereviews.add(rd);
		} else {
			negativereviews.add(rd);
		}

	}

	public void populateData(String bookid, ReviewDetails details) {
		
		if (data.containsKey(bookid)) {
			ArrayList<ReviewDetails> temp1 = data.get(bookid);
			temp1.add(details);
			data.put(bookid, temp1);

		} else {
			ArrayList<ReviewDetails> temp = new ArrayList<ReviewDetails>();
			temp.add(details);
			data.put(bookid, temp);

		}

	}

	class DetailsComparator implements Comparator<ReviewDetails> {

		@Override
		public int compare(ReviewDetails o1, ReviewDetails o2) {

			return o1.score > o2.score ? -1 : (o1.score == o2.score ? 0 : 1);

		}

	}

	class ReviewDetails {
		int rating;
		Date date;
		String reviewText;
		double score;

		public ReviewDetails(int rating, String date, String reviewText) throws ParseException {
			this.rating = rating;
			this.date = (Date) new SimpleDateFormat("mm dd,yyyy").parse(date);
			this.reviewText = reviewText;
			this.score = 0;

		}

	}

	public static void main(String args[]) throws IOException, ParseException {
		Ranking r = new Ranking();
		r.readFile("AmazonReviewsAll50k.csv");
		ArrayList<String> reviews = r.process("28612566");
		//ArrayList<String> reviews = r.process("2007770");
		
		if (reviews == null) {
			System.out.println("Book not found !!!");
		} else {
			System.out.println("The top 10 reviews are :");
			for (int i = 0;i< reviews.size();i++) {
				
				System.out.println(i+1 + "\t" + dates.get(i) +  "\t" + reviews.get(i));
			}
		}
		
		
		ArrayList<String> bookids = r.getAllBookIds();
		
		System.out.println("All Books in system");
		for(String book : bookids){
			System.out.println(book);
		}
		
	}

	public ArrayList<String> getAllBookIds() {
		ArrayList<String> book = new ArrayList<String>();
		for(String key : data.keySet()){
			book.add(key);
		}
		
		return book;
		
	}

}
