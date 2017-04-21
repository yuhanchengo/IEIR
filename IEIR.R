library(jiebaR)
library(readr)
library(wordVectors)
setwd("/Users/joy/Desktop")

cutter = worker("hmm", stop_word = "stop_words.utf8", hmm="hmm_model.utf8", bylines = T)
text = read_file("Corpus.txt")
article_words = segment(text, cutter)
article_word_vectors = sapply(article_words, paste, collapse = " ")
writeLines(article_word_vectors, "Corpus_vec.txt")
#segment("uniquequery.txt", cutter)

model4_SKIP = train_word2vec("Corpus_vec.txt", output="skip_gram.bin", threads = 3, vectors=300, window = 7, force = T, min_count = 7)
