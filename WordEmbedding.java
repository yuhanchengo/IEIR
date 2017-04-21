package data_processing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class WordEmbedding {
	public static HashMap<Object, ArrayList<Double>> wordVecDict = new HashMap<Object, ArrayList<Double>>();
	public static Scanner scan = new Scanner(System.in);
	public static void main(String[] args){
		WordEmbedding we = new WordEmbedding();
		try {			
			BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\joy\\Desktop\\IEIR\\modelSC.csv"));
			for(int i =0; i<2; i++){
				br.readLine();
			}
			
			while(br.ready()) {
				
				ArrayList<Double> vector = new ArrayList<Double>();
				String[] s = br.readLine().split(",");
				for(int i=1; i<s.length; i++){
					vector.add(Double.parseDouble(s[i]));
				}
				wordVecDict.put(s[0].replaceAll("\"", ""),vector);
				System.out.println(wordVecDict.size());


			}
			br.close();
			BufferedReader trainComment = new BufferedReader(new FileReader("C:\\Users\\joy\\Desktop\\四下\\IEIR\\NTCIR\\NTCIR原始資料\\train\\trainCmnt.segment.2017-04-21_01_17_29.txt"));
			BufferedReader trainPost = new BufferedReader(new FileReader("C:\\Users\\joy\\Desktop\\四下\\IEIR\\NTCIR\\NTCIR原始資料\\train\\trainQuery.segment.2017-04-21_01_17_51.txt"));
			
			System.out.println("status : ");
			while(!scan.nextLine().equals("exit")){
				System.out.println("cosine Similarity is : " + we.cosineSimilarity(we.docToVec(trainPost.readLine()), we.docToVec(trainComment.readLine())));
				System.out.println("status : ");
			}
			trainPost.close();
			trainComment.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	public double[] docToVec(String document){
		double[] docVec = new double[600];
		String[] inputToken = document.split(" ");
		Boolean first = true;
		for(int i=0; i<inputToken.length; i++){
			ArrayList<Double> wordVec = wordVecDict.get(inputToken[i]);
			if(wordVec != null && first == true){
				for(int j=0; j<wordVec.size(); j++){
					docVec[j] = wordVec.get(j);
					docVec[j+300] = wordVec.get(j);
				}
				first = false;
			}else if(wordVec != null && first == false){
				for(int j=0; j<wordVec.size(); j++){
					if(wordVec.get(j)<docVec[j]){
						docVec[j] = wordVec.get(j);
					}else{
						docVec[j+300] = wordVec.get(j);
					}
				}
			}else
				continue;
		}
		return docVec;
	}
	public String cosineSimilarity(double[] query, double[] doc){
		DecimalFormat df = new DecimalFormat("#.########");
		double dotProduct = 0,queryNorm =0, docNorm =0, cosSimilarity=0;
		for(int i=0; i<query.length; i++){
			queryNorm += Math.pow(query[i],2);
			docNorm += Math.pow(doc[i], 2);
			dotProduct += query[i]*doc[i];
		}
		System.out.println("dotProduct : " + dotProduct);
		System.out.println("queryNorm : " + queryNorm);
		System.out.println("docNorm : " + docNorm);
		cosSimilarity = dotProduct/ (Math.sqrt(queryNorm)*Math.sqrt(docNorm));
		return df.format(cosSimilarity);
	}
	public void WriteHashMap() throws IOException{
		FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\joy\\Desktop\\IEIR\\myMap.ser");
		ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);

		objectOutputStream.writeObject(wordVecDict);
		objectOutputStream.close();
		System.out.println("hashmap file done");
	}
	public void ReadHashMap() throws IOException, ClassNotFoundException{
		FileInputStream fileInputStream  = new FileInputStream("C:\\Users\\joy\\Desktop\\IEIR\\myMap.ser");
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

		HashMap<String, ArrayList<Double>> myHashMap = (HashMap<String, ArrayList<Double>>) objectInputStream.readObject();
		objectInputStream.close();
		System.out.println(myHashMap.get("说"));
		System.out.println(myHashMap.get("一个"));

	}

}
