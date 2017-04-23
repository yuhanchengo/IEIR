package data_processing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class word2vec {
	public static HashMap<Object, ArrayList<Double>> wordVecDict = new HashMap<Object, ArrayList<Double>>();
	word2vec(){
		createHashMap();
	}
	public static void createHashMap(){
		try{
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
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public double[] docToVec(String document){
		double[] docVec = new double[600];
		String[] inputToken = document.split(" ");
		Boolean first = true;
		for(int i=0; i<inputToken.length; i++){
			ArrayList<Double> wordVec = wordVecDict.get(inputToken[i]);
			if(wordVec == null && inputToken[i].length()==4){
				ArrayList<Double> segwordVec1 = wordVecDict.get(inputToken[i].substring(0, 2));
				ArrayList<Double> segwordVec2 = wordVecDict.get(inputToken[i].substring(2, 4));
				if(segwordVec1 != null && first == true){
					for(int j=0; j<segwordVec1.size(); j++){
						docVec[j] = segwordVec1.get(j);
						docVec[j+300] = segwordVec1.get(j);
					}
					first = false;
				}else if(segwordVec1 != null && first == false){
					for(int j=0; j<segwordVec1.size(); j++){
						if(segwordVec1.get(j)<docVec[j]){
							docVec[j] = segwordVec1.get(j);
						}else{
							docVec[j+300] = segwordVec1.get(j);
						}
					}
				}
				if(segwordVec2 != null && first == true){
					for(int j=0; j<segwordVec2.size(); j++){
						docVec[j] = segwordVec2.get(j);
						docVec[j+300] = segwordVec2.get(j);
					}
					first = false;
				}else if(segwordVec2 != null && first == false){
					for(int j=0; j<segwordVec2.size(); j++){
						if(segwordVec2.get(j)<docVec[j]){
							docVec[j] = segwordVec2.get(j);
						}else{
							docVec[j+300] = segwordVec2.get(j);
						}
					}
				}
				
			}else if(wordVec != null && first == true){
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
			}
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
//		System.out.println("dotProduct : " + dotProduct);
//		System.out.println("queryNorm : " + queryNorm);
//		System.out.println("docNorm : " + docNorm);
		if(queryNorm == 0){
			queryNorm = 1;
		}else if(docNorm == 0){
			docNorm = 1;
		}
		cosSimilarity = dotProduct/ (Math.sqrt(queryNorm)*Math.sqrt(docNorm));
		System.out.println(cosSimilarity);
		return df.format(cosSimilarity);
	}
	public void predictSVMRank(){
		try{
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("cmd /c start C:\\Users\\joy\\Desktop\\svm_rank_windows\\predict.bat");
		int processComplete = pr.waitFor();
		System.out.println(processComplete);
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException exp){
			exp.printStackTrace();
		}
	}
}
