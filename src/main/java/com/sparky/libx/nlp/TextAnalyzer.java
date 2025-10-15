package com.sparky.libx.nlp;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Advanced Natural Language Processing Utilities for Minecraft Plugins
 * Provides capabilities for text analysis, sentiment analysis, and language generation
 */
public class TextAnalyzer {
    
    /**
     * Represents a word with its frequency and position information
     */
    public static class Word {
        private final String text;
        private final int frequency;
        private final List<Integer> positions;
        
        public Word(String text, int frequency, List<Integer> positions) {
            this.text = text.toLowerCase();
            this.frequency = frequency;
            this.positions = new ArrayList<>(positions);
        }
        
        public String getText() {
            return text;
        }
        
        public int getFrequency() {
            return frequency;
        }
        
        public List<Integer> getPositions() {
            return new ArrayList<>(positions);
        }
        
        @Override
        public String toString() {
            return String.format("Word{text='%s', frequency=%d, positions=%s}", text, frequency, positions);
        }
    }
    
    /**
     * Represents a document with various text analysis metrics
     */
    public static class Document {
        private final String text;
        private final List<String> sentences;
        private final List<String> words;
        private final Map<String, Integer> wordFrequency;
        private final Map<String, List<Integer>> wordPositions;
        
        public Document(String text) {
            this.text = text;
            this.sentences = tokenizeSentences(text);
            this.words = tokenizeWords(text);
            this.wordFrequency = calculateWordFrequency(this.words);
            this.wordPositions = calculateWordPositions(this.words);
        }
        
        public String getText() {
            return text;
        }
        
        public List<String> getSentences() {
            return new ArrayList<>(sentences);
        }
        
        public List<String> getWords() {
            return new ArrayList<>(words);
        }
        
        public Map<String, Integer> getWordFrequency() {
            return new HashMap<>(wordFrequency);
        }
        
        public int getWordCount() {
            return words.size();
        }
        
        public int getSentenceCount() {
            return sentences.size();
        }
        
        public double getAverageWordsPerSentence() {
            return sentences.isEmpty() ? 0 : (double) words.size() / sentences.size();
        }
        
        public Set<String> getUniqueWords() {
            return new HashSet<>(wordFrequency.keySet());
        }
        
        public List<Word> getWordsWithFrequency() {
            return wordFrequency.entrySet().stream()
                .map(entry -> new Word(entry.getKey(), entry.getValue(), wordPositions.get(entry.getKey())))
                .collect(Collectors.toList());
        }
        
        public Word getMostFrequentWord() {
            return wordFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> new Word(entry.getKey(), entry.getValue(), wordPositions.get(entry.getKey())))
                .orElse(null);
        }
    }
    
    /**
     * Represents a term with TF-IDF score
     */
    public static class TFIDF {
        private final String term;
        private final double tf;
        private final double idf;
        private final double tfidf;
        
        public TFIDF(String term, double tf, double idf, double tfidf) {
            this.term = term;
            this.tf = tf;
            this.idf = idf;
            this.tfidf = tfidf;
        }
        
        public String getTerm() {
            return term;
        }
        
        public double getTf() {
            return tf;
        }
        
        public double getIdf() {
            return idf;
        }
        
        public double getTfidf() {
            return tfidf;
        }
        
        @Override
        public String toString() {
            return String.format("TFIDF{term='%s', tf=%.4f, idf=%.4f, tfidf=%.4f}", term, tf, idf, tfidf);
        }
    }
    
    // Common English stop words
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "will", "with", "i", "you", "we", "they", "this",
        "these", "those", "but", "or", "not", "have", "had", "do", "does",
        "did", "can", "could", "should", "would", "may", "might", "must",
        "shall", "am", "been", "being", "my", "your", "his", "her", "its",
        "our", "their", "me", "him", "us", "them", "what", "which", "who",
        "when", "where", "why", "how", "all", "any", "both", "each", "few",
        "more", "most", "other", "some", "such", "no", "nor", "only", "own",
        "same", "so", "than", "too", "very", "just", "now"
    ));
    
    // Regular expressions for text processing
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?]+");
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b\\w+\\b");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}&&[^']]+");
    
    /**
     * Tokenize text into sentences
     */
    public static List<String> tokenizeSentences(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        String[] sentences = SENTENCE_PATTERN.split(text);
        List<String> result = new ArrayList<>();
        
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        
        return result;
    }
    
    /**
     * Tokenize text into words
     */
    public static List<String> tokenizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> words = new ArrayList<>();
        java.util.regex.Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase());
        
        while (matcher.find()) {
            String word = matcher.group();
            // Remove punctuation but keep apostrophes for contractions
            word = PUNCTUATION_PATTERN.matcher(word).replaceAll("");
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        
        return words;
    }
    
    /**
     * Calculate word frequency
     */
    public static Map<String, Integer> calculateWordFrequency(List<String> words) {
        Map<String, Integer> frequency = new HashMap<>();
        
        for (String word : words) {
            if (!STOP_WORDS.contains(word)) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }
        
        return frequency;
    }
    
    /**
     * Calculate word positions
     */
    public static Map<String, List<Integer>> calculateWordPositions(List<String> words) {
        Map<String, List<Integer>> positions = new HashMap<>();
        
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (!STOP_WORDS.contains(word)) {
                positions.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
            }
        }
        
        return positions;
    }
    
    /**
     * Calculate Term Frequency (TF)
     */
    public static double calculateTF(String term, Document document) {
        int termCount = document.getWordFrequency().getOrDefault(term.toLowerCase(), 0);
        return document.getWordCount() == 0 ? 0 : (double) termCount / document.getWordCount();
    }
    
    /**
     * Calculate Inverse Document Frequency (IDF)
     */
    public static double calculateIDF(String term, List<Document> documents) {
        if (documents.isEmpty()) {
            return 0;
        }
        
        long docsWithTerm = documents.stream()
            .map(Document::getWordFrequency)
            .mapToLong(freq -> freq.containsKey(term.toLowerCase()) ? 1 : 0)
            .sum();
        
        return Math.log((double) documents.size() / (docsWithTerm + 1));
    }
    
    /**
     * Calculate TF-IDF scores for a document
     */
    public static List<TFIDF> calculateTFIDF(Document document, List<Document> allDocuments) {
        List<TFIDF> tfidfScores = new ArrayList<>();
        
        for (String term : document.getWordFrequency().keySet()) {
            double tf = calculateTF(term, document);
            double idf = calculateIDF(term, allDocuments);
            double tfidf = tf * idf;
            
            tfidfScores.add(new TFIDF(term, tf, idf, tfidf));
        }
        
        // Sort by TF-IDF score descending
        tfidfScores.sort((a, b) -> Double.compare(b.getTfidf(), a.getTfidf()));
        
        return tfidfScores;
    }
    
    /**
     * Calculate cosine similarity between two documents
     */
    public static double calculateCosineSimilarity(Document doc1, Document doc2) {
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(doc1.getWordFrequency().keySet());
        allTerms.addAll(doc2.getWordFrequency().keySet());
        
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        
        for (String term : allTerms) {
            int freq1 = doc1.getWordFrequency().getOrDefault(term, 0);
            int freq2 = doc2.getWordFrequency().getOrDefault(term, 0);
            
            dotProduct += freq1 * freq2;
            magnitude1 += freq1 * freq1;
            magnitude2 += freq2 * freq2;
        }
        
        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(magnitude1) * Math.sqrt(magnitude2));
    }
    
    /**
     * Simple sentiment analysis based on word lists
     */
    public static class SentimentAnalyzer {
        private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "good", "great", "excellent", "amazing", "wonderful", "fantastic", "awesome",
            "brilliant", "outstanding", "superb", "magnificent", "marvelous", "terrific",
            "fabulous", "incredible", "perfect", "delightful", "pleasant", "enjoyable",
            "satisfactory", "favorable", "positive", "pleased", "happy", "glad", "joyful"
        ));
        
        private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "bad", "terrible", "awful", "horrible", "dreadful", "atrocious", "abysmal",
            "dismal", "poor", "mediocre", "inferior", "unsatisfactory", "unfavorable",
            "negative", "disappointing", "frustrating", "annoying", "irritating",
            "disgusting", "repulsive", "offensive", "unpleasant", "painful", "hurtful",
            "sad", "unhappy", "depressed", "miserable", "angry", "furious", "outraged"
        ));
        
        /**
         * Analyze sentiment of text
         */
        public static SentimentResult analyzeSentiment(String text) {
            Document doc = new Document(text);
            List<String> words = doc.getWords();
            
            int positiveCount = 0;
            int negativeCount = 0;
            
            for (String word : words) {
                String lowerWord = word.toLowerCase();
                if (POSITIVE_WORDS.contains(lowerWord)) {
                    positiveCount++;
                } else if (NEGATIVE_WORDS.contains(lowerWord)) {
                    negativeCount++;
                }
            }
            
            double total = positiveCount + negativeCount;
            double positiveScore = total > 0 ? (double) positiveCount / total : 0;
            double negativeScore = total > 0 ? (double) negativeCount / total : 0;
            
            Sentiment sentiment;
            if (positiveScore > negativeScore) {
                sentiment = Sentiment.POSITIVE;
            } else if (negativeScore > positiveScore) {
                sentiment = Sentiment.NEGATIVE;
            } else {
                sentiment = Sentiment.NEUTRAL;
            }
            
            return new SentimentResult(sentiment, positiveScore, negativeScore);
        }
        
        /**
         * Sentiment enumeration
         */
        public enum Sentiment {
            POSITIVE, NEGATIVE, NEUTRAL
        }
        
        /**
         * Sentiment analysis result
         */
        public static class SentimentResult {
            private final Sentiment sentiment;
            private final double positiveScore;
            private final double negativeScore;
            
            public SentimentResult(Sentiment sentiment, double positiveScore, double negativeScore) {
                this.sentiment = sentiment;
                this.positiveScore = positiveScore;
                this.negativeScore = negativeScore;
            }
            
            public Sentiment getSentiment() {
                return sentiment;
            }
            
            public double getPositiveScore() {
                return positiveScore;
            }
            
            public double getNegativeScore() {
                return negativeScore;
            }
            
            @Override
            public String toString() {
                return String.format("SentimentResult{sentiment=%s, positive=%.2f, negative=%.2f}", 
                    sentiment, positiveScore, negativeScore);
            }
        }
    }
    
    /**
     * Generate text using Markov chains
     */
    public static class MarkovChainGenerator {
        private final Map<String, List<String>> chain;
        private final int order;
        private final Random random;
        
        public MarkovChainGenerator(int order) {
            this.chain = new HashMap<>();
            this.order = order;
            this.random = new Random();
        }
        
        /**
         * Train the Markov chain with text
         */
        public void train(String text) {
            List<String> words = tokenizeWords(text);
            
            for (int i = 0; i <= words.size() - order; i++) {
                StringBuilder keyBuilder = new StringBuilder();
                for (int j = 0; j < order; j++) {
                    if (i + j < words.size()) {
                        if (keyBuilder.length() > 0) {
                            keyBuilder.append(" ");
                        }
                        keyBuilder.append(words.get(i + j));
                    }
                }
                
                String key = keyBuilder.toString();
                
                if (i + order < words.size()) {
                    String nextWord = words.get(i + order);
                    chain.computeIfAbsent(key, k -> new ArrayList<>()).add(nextWord);
                }
            }
        }
        
        /**
         * Generate text of specified length
         */
        public String generate(int wordCount) {
            if (chain.isEmpty()) {
                return "";
            }
            
            // Select random starting key
            List<String> keys = new ArrayList<>(chain.keySet());
            String currentKey = keys.get(random.nextInt(keys.size()));
            
            StringBuilder result = new StringBuilder(currentKey);
            String[] currentWords = currentKey.split(" ");
            
            for (int i = order; i < wordCount; i++) {
                List<String> nextWords = chain.get(currentKey);
                if (nextWords == null || nextWords.isEmpty()) {
                    break;
                }
                
                String nextWord = nextWords.get(random.nextInt(nextWords.size()));
                result.append(" ").append(nextWord);
                
                // Update current key
                StringBuilder newKeyBuilder = new StringBuilder();
                for (int j = 1; j < currentWords.length; j++) {
                    newKeyBuilder.append(currentWords[j]).append(" ");
                }
                newKeyBuilder.append(nextWord);
                currentKey = newKeyBuilder.toString();
                currentWords = currentKey.split(" ");
            }
            
            return result.toString();
        }
    }
    
    /**
     * Calculate text readability using Flesch-Kincaid Grade Level
     */
    public static double calculateReadability(Document document) {
        if (document.getWordCount() == 0 || document.getSentenceCount() == 0) {
            return 0;
        }
        
        // Count syllables (simplified)
        int totalSyllables = 0;
        for (String word : document.getWords()) {
            totalSyllables += countSyllables(word);
        }
        
        double avgWordsPerSentence = document.getAverageWordsPerSentence();
        double avgSyllablesPerWord = (double) totalSyllables / document.getWordCount();
        
        // Flesch-Kincaid Grade Level formula
        return 0.39 * avgWordsPerSentence + 11.8 * avgSyllablesPerWord - 15.59;
    }
    
    /**
     * Count syllables in a word (simplified algorithm)
     */
    private static int countSyllables(String word) {
        if (word.isEmpty()) {
            return 0;
        }
        
        word = word.toLowerCase();
        int count = 0;
        boolean prevVowel = false;
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            boolean isVowel = "aeiouy".indexOf(c) != -1;
            
            if (isVowel && !prevVowel) {
                count++;
            }
            
            prevVowel = isVowel;
        }
        
        // Handle silent 'e' at the end
        if (word.endsWith("e") && count > 1) {
            count--;
        }
        
        return Math.max(1, count);
    }
    
    /**
     * Extract keywords from document using TF-IDF
     */
    public static List<String> extractKeywords(Document document, List<Document> allDocuments, int maxKeywords) {
        List<TFIDF> tfidfScores = calculateTFIDF(document, allDocuments);
        
        return tfidfScores.stream()
            .limit(maxKeywords)
            .map(TFIDF::getTerm)
            .collect(Collectors.toList());
    }
    
    /**
     * Find similar documents using cosine similarity
     */
    public static List<DocumentSimilarity> findSimilarDocuments(Document target, List<Document> candidates) {
        List<DocumentSimilarity> similarities = new ArrayList<>();
        
        for (Document candidate : candidates) {
            if (candidate != target) {
                double similarity = calculateCosineSimilarity(target, candidate);
                similarities.add(new DocumentSimilarity(candidate, similarity));
            }
        }
        
        // Sort by similarity descending
        similarities.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));
        
        return similarities;
    }
    
    /**
     * Document similarity result
     */
    public static class DocumentSimilarity {
        private final Document document;
        private final double similarity;
        
        public DocumentSimilarity(Document document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }
        
        public Document getDocument() {
            return document;
        }
        
        public double getSimilarity() {
            return similarity;
        }
        
        @Override
        public String toString() {
            return String.format("DocumentSimilarity{similarity=%.4f}", similarity);
        }
    }
}