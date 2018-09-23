### Improving chatbot with Word-Embedding and SVM-Rank

#### Building word-Embedding model in R
```R
library(jiebaR)
library(readr)
library(wordVectors)

cutter = worker("hmm", stop_word = "stop_words.utf8", hmm="hmm_model.utf8", bylines = T)
# corpus.txt是ntcir提供的post, comment的文字內容
text = read_file("corpus.txt")
article_words = segment(text, cutter)
article_word_vectors = sapply(article_words, paste, collapse = " ")
writeLines(article_word_vectors, "corpus_vec.txt")
model_SKIP = train_word2vec("corpus_vec.txt", output="skip_gram.bin", threads = 3, vectors=300, window = 7, force = T, min_count = 7)
# 此csv檔為每個詞所對應的vector的table
write.csv(model_SKIP,"skip_gram.csv")
```
#### Store word-embedding model trained in R to HashMap in Java
```java
public static HashMap<Object, ArrayList<Double>> wordVecDict = new HashMap<Object, ArrayList<Double>>();
public static void createHashMap(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("modelSC.csv"));
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
```

#### Convert wordVector to docVector
- #### 概念：document vector 就是取出每個維度最小及最大的元素來分別組成兩個同樣為300維的vector
![](figure/a2_1.png)
![](figure/a2_2.png)
![](figure/a2_3.png)
![](figure/a2_4.png)

```Java
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

```

#### Calculate cosine Similarity between Documents

```java
public String cosineSimilarity(double[] query, double[] doc){
		DecimalFormat df = new DecimalFormat("#.########");
		double dotProduct = 0,queryNorm =0, docNorm =0, cosSimilarity=0;
		for(int i=0; i<query.length; i++){
			queryNorm += Math.pow(query[i],2);
			docNorm += Math.pow(doc[i], 2);
			dotProduct += query[i]*doc[i];
		}
		if(queryNorm == 0){
			queryNorm = 1;
		}else if(docNorm == 0){
			docNorm = 1;
		}
		cosSimilarity = dotProduct/ (Math.sqrt(queryNorm)*Math.sqrt(docNorm));
		System.out.println(cosSimilarity);
		return df.format(cosSimilarity);
	}

```

#### Call Batch file to execute SVMRank prediction
```java
public void predictSVMRank(){
		try{
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("cmd /c start predict.bat");
		int processComplete = pr.waitFor();
		System.out.println(processComplete);
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException exp){
			exp.printStackTrace();
		}
	}

```

### SVMRank model
Training data: 11520 (767 queries)

#### Features for SVM-Rank (Total of 1201 features)
- Query DocVector(dim: 600)
- Comment DocVector(dim: 600)
- Cosine Similarity value of query and comment

#### Basis of reranking
1. SVM Score : The score of the document calculated by Solr through tuning the weight of different fields in the Solr config file.
2. Cosine Similarity : The cosine similarity of the query and the retrieved comment.
3. SVMRank Score : The score given by the SVMRank model.
4. AVG Score : The average score of the three scores above.

#### Result
Performance based on the above 4 reranking mechanisms:
Cosine Similarity > SVMRank > Solr > AVG Score
