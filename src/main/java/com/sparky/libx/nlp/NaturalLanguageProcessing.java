package com.sparky.libx.nlp;

import com.sparky.libx.math.Vector3D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Advanced Natural Language Processing Framework for Minecraft Plugins
 * Provides capabilities for text analysis, sentiment analysis, language modeling, and NLP algorithms
 * 
 * @author Андрій Будильников
 */
public class NaturalLanguageProcessing {
    
    /**
     * Represents a document for text processing
     */
    public static class Document {
        private final String text;
        private final List<String> tokens;
        private final List<String> sentences;
        private final Map<String, Integer> termFrequencies;
        private final String language;
        
        public Document(String text) {
            this(text, "en");
        }
        
        public Document(String text, String language) {
            this.text = text;
            this.language = language;
            this.tokens = tokenize(text);
            this.sentences = splitSentences(text);
            this.termFrequencies = calculateTermFrequencies(tokens);
        }
        
        public String getText() {
            return text;
        }
        
        public List<String> getTokens() {
            return new ArrayList<>(tokens);
        }
        
        public List<String> getSentences() {
            return new ArrayList<>(sentences);
        }
        
        public Map<String, Integer> getTermFrequencies() {
            return new HashMap<>(termFrequencies);
        }
        
        public String getLanguage() {
            return language;
        }
        
        public int getWordCount() {
            return tokens.size();
        }
        
        public int getSentenceCount() {
            return sentences.size();
        }
        
        public double getAverageWordsPerSentence() {
            return sentences.isEmpty() ? 0 : (double) tokens.size() / sentences.size();
        }
        
        /**
         * Tokenizes text into words
         */
        private List<String> tokenize(String text) {
            // Simple tokenization - in practice, you would use more sophisticated methods
            String[] words = text.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
            return Arrays.stream(words).filter(word -> !word.isEmpty()).collect(Collectors.toList());
        }
        
        /**
         * Splits text into sentences
         */
        private List<String> splitSentences(String text) {
            // Simple sentence splitting - in practice, you would use more sophisticated methods
            String[] sentences = text.split("[.!?]+");
            return Arrays.stream(sentences).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        }
        
        /**
         * Calculates term frequencies
         */
        private Map<String, Integer> calculateTermFrequencies(List<String> tokens) {
            Map<String, Integer> frequencies = new HashMap<>();
            for (String token : tokens) {
                frequencies.put(token, frequencies.getOrDefault(token, 0) + 1);
            }
            return frequencies;
        }
    }
    
    /**
     * Represents a corpus of documents for analysis
     */
    public static class Corpus {
        private final List<Document> documents;
        private final Map<String, Integer> documentFrequencies;
        private final int totalDocuments;
        
        public Corpus(List<Document> documents) {
            this.documents = new ArrayList<>(documents);
            this.totalDocuments = documents.size();
            this.documentFrequencies = calculateDocumentFrequencies();
        }
        
        public List<Document> getDocuments() {
            return new ArrayList<>(documents);
        }
        
        public int getTotalDocuments() {
            return totalDocuments;
        }
        
        public Map<String, Integer> getDocumentFrequencies() {
            return new HashMap<>(documentFrequencies);
        }
        
        /**
         * Calculates document frequencies for all terms
         */
        private Map<String, Integer> calculateDocumentFrequencies() {
            Map<String, Integer> docFreq = new HashMap<>();
            for (Document doc : documents) {
                Set<String> uniqueTerms = new HashSet<>(doc.getTermFrequencies().keySet());
                for (String term : uniqueTerms) {
                    docFreq.put(term, docFreq.getOrDefault(term, 0) + 1);
                }
            }
            return docFreq;
        }
        
        /**
         * Calculates TF-IDF for a term in a document
         */
        public double calculateTfIdf(String term, Document document) {
            // Term Frequency
            int tf = document.getTermFrequencies().getOrDefault(term, 0);
            double logTf = tf > 0 ? 1 + Math.log(tf) : 0;
            
            // Inverse Document Frequency
            int df = documentFrequencies.getOrDefault(term, 0);
            double idf = Math.log((double) totalDocuments / (df + 1));
            
            return logTf * idf;
        }
        
        /**
         * Gets the most frequent terms in the corpus
         */
        public List<Map.Entry<String, Integer>> getMostFrequentTerms(int limit) {
            Map<String, Integer> termCounts = new HashMap<>();
            
            for (Document doc : documents) {
                for (Map.Entry<String, Integer> entry : doc.getTermFrequencies().entrySet()) {
                    termCounts.put(entry.getKey(), termCounts.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
            
            return termCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Represents a text classifier
     */
    public abstract static class TextClassifier {
        /**
         * Trains the classifier on a corpus
         */
        public abstract void train(Corpus corpus, Map<Document, String> labels);
        
        /**
         * Classifies a document
         */
        public abstract String classify(Document document);
        
        /**
         * Gets classification probabilities for a document
         */
        public abstract Map<String, Double> getClassProbabilities(Document document);
    }
    
    /**
     * Represents a Naive Bayes text classifier
     */
    public static class NaiveBayesClassifier extends TextClassifier {
        private final Map<String, Map<String, Double>> classWordProbabilities;
        private final Map<String, Double> classPriors;
        private final Set<String> vocabulary;
        private boolean isTrained;
        
        public NaiveBayesClassifier() {
            this.classWordProbabilities = new ConcurrentHashMap<>();
            this.classPriors = new ConcurrentHashMap<>();
            this.vocabulary = new HashSet<>();
            this.isTrained = false;
        }
        
        @Override
        public void train(Corpus corpus, Map<Document, String> labels) {
            // Clear previous training
            classWordProbabilities.clear();
            classPriors.clear();
            vocabulary.clear();
            
            // Collect all documents by class
            Map<String, List<Document>> documentsByClass = new HashMap<>();
            for (Map.Entry<Document, String> entry : labels.entrySet()) {
                String className = entry.getValue();
                Document document = entry.getKey();
                documentsByClass.computeIfAbsent(className, k -> new ArrayList<>()).add(document);
            }
            
            // Calculate class priors
            int totalDocuments = corpus.getTotalDocuments();
            for (Map.Entry<String, List<Document>> entry : documentsByClass.entrySet()) {
                String className = entry.getKey();
                List<Document> classDocuments = entry.getValue();
                classPriors.put(className, (double) classDocuments.size() / totalDocuments);
            }
            
            // Calculate word probabilities for each class
            for (Map.Entry<String, List<Document>> entry : documentsByClass.entrySet()) {
                String className = entry.getKey();
                List<Document> classDocuments = entry.getValue();
                
                // Count words in this class
                Map<String, Integer> wordCounts = new HashMap<>();
                int totalWords = 0;
                
                for (Document document : classDocuments) {
                    for (Map.Entry<String, Integer> termEntry : document.getTermFrequencies().entrySet()) {
                        String word = termEntry.getKey();
                        int count = termEntry.getValue();
                        wordCounts.put(word, wordCounts.getOrDefault(word, 0) + count);
                        totalWords += count;
                        vocabulary.add(word);
                    }
                }
                
                // Calculate probabilities with Laplace smoothing
                Map<String, Double> wordProbabilities = new HashMap<>();
                int vocabSize = vocabulary.size();
                
                for (String word : vocabulary) {
                    int wordCount = wordCounts.getOrDefault(word, 0);
                    double probability = (double) (wordCount + 1) / (totalWords + vocabSize);
                    wordProbabilities.put(word, probability);
                }
                
                classWordProbabilities.put(className, wordProbabilities);
            }
            
            isTrained = true;
        }
        
        @Override
        public String classify(Document document) {
            if (!isTrained) {
                throw new IllegalStateException("Classifier must be trained before classification");
            }
            
            Map<String, Double> probabilities = getClassProbabilities(document);
            
            return probabilities.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
        }
        
        @Override
        public Map<String, Double> getClassProbabilities(Document document) {
            if (!isTrained) {
                throw new IllegalStateException("Classifier must be trained before classification");
            }
            
            Map<String, Double> probabilities = new HashMap<>();
            
            for (String className : classPriors.keySet()) {
                // Start with log prior probability
                double logProbability = Math.log(classPriors.get(className));
                
                // Add log probabilities for each word
                Map<String, Double> wordProbabilities = classWordProbabilities.get(className);
                for (String token : document.getTokens()) {
                    if (vocabulary.contains(token)) {
                        double wordProb = wordProbabilities.getOrDefault(token, 
                            1.0 / (wordProbabilities.size() + vocabulary.size()));
                        logProbability += Math.log(wordProb);
                    }
                }
                
                probabilities.put(className, logProbability);
            }
            
            // Convert log probabilities to probabilities
            double maxLogProb = probabilities.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            double sumExp = probabilities.values().stream()
                    .mapToDouble(prob -> Math.exp(prob - maxLogProb))
                    .sum();
            
            for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
                double prob = Math.exp(entry.getValue() - maxLogProb) / sumExp;
                entry.setValue(prob);
            }
            
            return probabilities;
        }
    }
    
    /**
     * Represents a sentiment analyzer
     */
    public static class SentimentAnalyzer {
        private final Map<String, Double> sentimentLexicon;
        private final Set<String> negationWords;
        
        public SentimentAnalyzer() {
            this.sentimentLexicon = createDefaultLexicon();
            this.negationWords = createNegationWords();
        }
        
        /**
         * Analyzes the sentiment of a document
         */
        public Sentiment analyze(Document document) {
            List<String> tokens = document.getTokens();
            double totalScore = 0;
            int wordCount = 0;
            boolean negation = false;
            
            for (String token : tokens) {
                // Check for negation words
                if (negationWords.contains(token.toLowerCase())) {
                    negation = !negation;
                    continue;
                }
                
                // Get sentiment score for word
                Double score = sentimentLexicon.get(token.toLowerCase());
                if (score != null) {
                    // Apply negation if active
                    if (negation) {
                        score = -score;
                    }
                    totalScore += score;
                    wordCount++;
                }
            }
            
            // Calculate average sentiment
            double averageScore = wordCount > 0 ? totalScore / wordCount : 0;
            
            // Determine sentiment category
            Sentiment.Category category;
            if (averageScore > 0.1) {
                category = Sentiment.Category.POSITIVE;
            } else if (averageScore < -0.1) {
                category = Sentiment.Category.NEGATIVE;
            } else {
                category = Sentiment.Category.NEUTRAL;
            }
            
            return new Sentiment(averageScore, category);
        }
        
        /**
         * Creates a default sentiment lexicon
         */
        private Map<String, Double> createDefaultLexicon() {
            Map<String, Double> lexicon = new HashMap<>();
            
            // Positive words
            lexicon.put("good", 0.5);
            lexicon.put("great", 0.8);
            lexicon.put("excellent", 0.9);
            lexicon.put("amazing", 0.9);
            lexicon.put("wonderful", 0.8);
            lexicon.put("fantastic", 0.9);
            lexicon.put("awesome", 0.8);
            lexicon.put("brilliant", 0.8);
            lexicon.put("outstanding", 0.9);
            lexicon.put("superb", 0.8);
            lexicon.put("perfect", 1.0);
            lexicon.put("love", 0.9);
            lexicon.put("like", 0.5);
            lexicon.put("happy", 0.7);
            lexicon.put("pleased", 0.6);
            lexicon.put("satisfied", 0.6);
            lexicon.put("delighted", 0.8);
            lexicon.put("thrilled", 0.9);
            lexicon.put("excited", 0.7);
            lexicon.put("enthusiastic", 0.7);
            
            // Negative words
            lexicon.put("bad", -0.5);
            lexicon.put("terrible", -0.9);
            lexicon.put("awful", -0.8);
            lexicon.put("horrible", -0.9);
            lexicon.put("disgusting", -0.9);
            lexicon.put("hate", -0.9);
            lexicon.put("dislike", -0.5);
            lexicon.put("sad", -0.7);
            lexicon.put("angry", -0.8);
            lexicon.put("frustrated", -0.6);
            lexicon.put("disappointed", -0.7);
            lexicon.put("annoyed", -0.6);
            lexicon.put("upset", -0.7);
            lexicon.put("worried", -0.5);
            lexicon.put("stressed", -0.6);
            lexicon.put("depressed", -0.8);
            lexicon.put("miserable", -0.8);
            lexicon.put("pathetic", -0.7);
            lexicon.put("useless", -0.7);
            lexicon.put("worthless", -0.8);
            
            return lexicon;
        }
        
        /**
         * Creates a set of negation words
         */
        private Set<String> createNegationWords() {
            return new HashSet<>(Arrays.asList(
                "not", "no", "never", "nothing", "nowhere", "nobody", "none",
                "neither", "nor", "cannot", "cant", "dont", "doesnt", "didnt",
                "wont", "wouldnt", "shouldnt", "couldnt", "isnt", "arent", "wasnt", "werent"
            ));
        }
    }
    
    /**
     * Represents a sentiment analysis result
     */
    public static class Sentiment {
        private final double score;
        private final Category category;
        
        public enum Category {
            POSITIVE, NEGATIVE, NEUTRAL
        }
        
        public Sentiment(double score, Category category) {
            this.score = score;
            this.category = category;
        }
        
        public double getScore() {
            return score;
        }
        
        public Category getCategory() {
            return category;
        }
        
        @Override
        public String toString() {
            return "Sentiment{score=" + score + ", category=" + category + "}";
        }
    }
    
    /**
     * Represents a named entity recognizer
     */
    public static class NamedEntityRecognizer {
        private final Set<String> personNames;
        private final Set<String> locations;
        private final Set<String> organizations;
        
        public NamedEntityRecognizer() {
            this.personNames = createPersonNames();
            this.locations = createLocations();
            this.organizations = createOrganizations();
        }
        
        /**
         * Recognizes named entities in a document
         */
        public List<NamedEntity> recognize(Document document) {
            List<NamedEntity> entities = new ArrayList<>();
            List<String> tokens = document.getTokens();
            
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                
                // Check for person names
                if (personNames.contains(token)) {
                    entities.add(new NamedEntity(token, NamedEntity.Type.PERSON, i));
                }
                // Check for locations
                else if (locations.contains(token)) {
                    entities.add(new NamedEntity(token, NamedEntity.Type.LOCATION, i));
                }
                // Check for organizations
                else if (organizations.contains(token)) {
                    entities.add(new NamedEntity(token, NamedEntity.Type.ORGANIZATION, i));
                }
            }
            
            return entities;
        }
        
        /**
         * Creates a set of common person names
         */
        private Set<String> createPersonNames() {
            return new HashSet<>(Arrays.asList(
                "john", "mary", "james", "patricia", "robert", "jennifer", "michael", "linda",
                "william", "elizabeth", "david", "barbara", "richard", "susan", "joseph", "jessica",
                "thomas", "sarah", "charles", "karen", "christopher", "nancy", "daniel", "lisa",
                "matthew", "betty", "anthony", "helen", "mark", "sandra", "donald", "donna",
                "steven", "carol", "paul", "ruth", "andrew", "sharon", "joshua", "michelle"
            ));
        }
        
        /**
         * Creates a set of common locations
         */
        private Set<String> createLocations() {
            return new HashSet<>(Arrays.asList(
                "new york", "los angeles", "chicago", "houston", "phoenix", "philadelphia",
                "san antonio", "san diego", "dallas", "san jose", "austin", "jacksonville",
                "fort worth", "columbus", "charlotte", "san francisco", "indianapolis",
                "seattle", "denver", "washington", "boston", "el paso", "nashville", "detroit",
                "oklahoma city", "portland", "las vegas", "memphis", "louisville", "baltimore",
                "milwaukee", "albuquerque", "tucson", "fresno", "sacramento", "long beach",
                "kansas city", "mesa", "virginia beach", "atlanta", "colorado springs",
                "raleigh", "omaha", "miami", "tulsa", "oakland", "minneapolis", "cleveland",
                "wichita", "arlington", "new orleans", "bakersfield", "tampa", "honolulu"
            ));
        }
        
        /**
         * Creates a set of common organizations
         */
        private Set<String> createOrganizations() {
            return new HashSet<>(Arrays.asList(
                "google", "apple", "microsoft", "amazon", "facebook", "netflix", "twitter",
                "ibm", "oracle", "sap", "salesforce", "adobe", "intel", "nvidia", "amd",
                "tesla", "boeing", "lockheed", "general electric", "disney", "netflix",
                "coca cola", "mcdonalds", "starbucks", "walmart", "target", "costco",
                "ford", "general motors", "toyota", "honda", "nike", "adidas", "puma",
                "microsoft", "accenture", "deloitte", "pwc", "kpmg", "ernst young",
                "goldman sachs", "jpmorgan", "bank of america", "citibank", "wells fargo",
                "united nations", "world bank", "international monetary fund",
                "red cross", "doctors without borders", "amnesty international"
            ));
        }
    }
    
    /**
     * Represents a named entity
     */
    public static class NamedEntity {
        private final String text;
        private final Type type;
        private final int position;
        
        public enum Type {
            PERSON, LOCATION, ORGANIZATION, DATE, TIME, MONEY, PERCENT
        }
        
        public NamedEntity(String text, Type type, int position) {
            this.text = text;
            this.type = type;
            this.position = position;
        }
        
        public String getText() {
            return text;
        }
        
        public Type getType() {
            return type;
        }
        
        public int getPosition() {
            return position;
        }
        
        @Override
        public String toString() {
            return "NamedEntity{text='" + text + "', type=" + type + ", position=" + position + "}";
        }
    }
    
    /**
     * Represents a text summarizer
     */
    public static class TextSummarizer {
        private final int maxSentences;
        
        public TextSummarizer() {
            this(3);
        }
        
        public TextSummarizer(int maxSentences) {
            this.maxSentences = maxSentences;
        }
        
        /**
         * Summarizes a document using extractive summarization
         */
        public String summarize(Document document) {
            List<String> sentences = document.getSentences();
            
            if (sentences.size() <= maxSentences) {
                return document.getText();
            }
            
            // Calculate sentence scores based on term frequencies
            Map<String, Integer> termFrequencies = new HashMap<>();
            for (Document doc : new Corpus(Arrays.asList(document)).getDocuments()) {
                for (Map.Entry<String, Integer> entry : doc.getTermFrequencies().entrySet()) {
                    termFrequencies.put(entry.getKey(), termFrequencies.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
            
            // Score each sentence
            Map<String, Double> sentenceScores = new HashMap<>();
            for (String sentence : sentences) {
                Document sentenceDoc = new Document(sentence);
                double score = 0;
                
                for (String token : sentenceDoc.getTokens()) {
                    score += termFrequencies.getOrDefault(token, 0);
                }
                
                // Normalize by sentence length
                if (!sentenceDoc.getTokens().isEmpty()) {
                    score /= sentenceDoc.getTokens().size();
                }
                
                sentenceScores.put(sentence, score);
            }
            
            // Select top sentences
            return sentenceScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(maxSentences)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(" "));
        }
    }
    
    /**
     * Represents a language detector
     */
    public static class LanguageDetector {
        private final Map<String, Map<Character, Double>> languageModels;
        
        public LanguageDetector() {
            this.languageModels = createLanguageModels();
        }
        
        /**
         * Detects the language of a document
         */
        public String detectLanguage(Document document) {
            String text = document.getText().toLowerCase();
            
            // Calculate character frequencies
            Map<Character, Integer> charCounts = new HashMap<>();
            int totalChars = 0;
            
            for (char c : text.toCharArray()) {
                if (Character.isLetter(c)) {
                    charCounts.put(c, charCounts.getOrDefault(c, 0) + 1);
                    totalChars++;
                }
            }
            
            // Convert to probabilities
            Map<Character, Double> charProbs = new HashMap<>();
            for (Map.Entry<Character, Integer> entry : charCounts.entrySet()) {
                charProbs.put(entry.getKey(), (double) entry.getValue() / totalChars);
            }
            
            // Calculate similarity to each language model
            Map<String, Double> similarities = new HashMap<>();
            for (Map.Entry<String, Map<Character, Double>> modelEntry : languageModels.entrySet()) {
                String language = modelEntry.getKey();
                Map<Character, Double> model = modelEntry.getValue();
                
                double similarity = 0;
                for (Map.Entry<Character, Double> charEntry : charProbs.entrySet()) {
                    char c = charEntry.getKey();
                    double prob = charEntry.getValue();
                    double modelProb = model.getOrDefault(c, 0.0);
                    similarity += prob * Math.log(modelProb + 1e-10); // Add small epsilon to avoid log(0)
                }
                
                similarities.put(language, similarity);
            }
            
            // Return language with highest similarity
            return similarities.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("unknown");
        }
        
        /**
         * Creates language models for common languages
         */
        private Map<String, Map<Character, Double>> createLanguageModels() {
            Map<String, Map<Character, Double>> models = new HashMap<>();
            
            // English model (simplified)
            Map<Character, Double> englishModel = new HashMap<>();
            String englishChars = "etaoinshrdlcumwfgypbvkjxqz";
            double[] englishFreqs = {12.7, 9.1, 8.2, 7.5, 7.0, 6.7, 6.3, 6.1, 6.0, 4.3,
                                   4.0, 2.8, 2.8, 2.4, 2.4, 2.2, 2.0, 2.0, 1.9, 1.5,
                                   1.0, 0.8, 0.2, 0.2, 0.1, 0.1};
            
            for (int i = 0; i < englishChars.length(); i++) {
                englishModel.put(englishChars.charAt(i), englishFreqs[i] / 100.0);
            }
            models.put("en", englishModel);
            
            // Spanish model (simplified)
            Map<Character, Double> spanishModel = new HashMap<>();
            String spanishChars = "eaosrnidlctumpbgvyqhfzjxwk";
            double[] spanishFreqs = {12.5, 10.8, 7.5, 6.8, 6.5, 6.2, 5.5, 5.0, 4.8, 4.5,
                                   4.2, 3.8, 3.5, 3.2, 3.0, 2.8, 2.5, 2.2, 2.0, 1.8,
                                   1.5, 1.2, 0.8, 0.5, 0.3, 0.1};
            
            for (int i = 0; i < spanishChars.length(); i++) {
                spanishModel.put(spanishChars.charAt(i), spanishFreqs[i] / 100.0);
            }
            models.put("es", spanishModel);
            
            return models;
        }
    }
    
    /**
     * Represents a text similarity calculator
     */
    public static class TextSimilarity {
        /**
         * Calculates cosine similarity between two documents
         */
        public static double cosineSimilarity(Document doc1, Document doc2) {
            // Get all unique terms
            Set<String> allTerms = new HashSet<>();
            allTerms.addAll(doc1.getTermFrequencies().keySet());
            allTerms.addAll(doc2.getTermFrequencies().keySet());
            
            // Calculate TF vectors
            double[] vector1 = new double[allTerms.size()];
            double[] vector2 = new double[allTerms.size()];
            
            int index = 0;
            for (String term : allTerms) {
                vector1[index] = doc1.getTermFrequencies().getOrDefault(term, 0);
                vector2[index] = doc2.getTermFrequencies().getOrDefault(term, 0);
                index++;
            }
            
            // Calculate cosine similarity
            double dotProduct = 0;
            double norm1 = 0;
            double norm2 = 0;
            
            for (int i = 0; i < vector1.length; i++) {
                dotProduct += vector1[i] * vector2[i];
                norm1 += vector1[i] * vector1[i];
                norm2 += vector2[i] * vector2[i];
            }
            
            if (norm1 == 0 || norm2 == 0) {
                return 0;
            }
            
            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        }
        
        /**
         * Calculates Jaccard similarity between two documents
         */
        public static double jaccardSimilarity(Document doc1, Document doc2) {
            Set<String> terms1 = new HashSet<>(doc1.getTermFrequencies().keySet());
            Set<String> terms2 = new HashSet<>(doc2.getTermFrequencies().keySet());
            
            // Calculate intersection and union
            Set<String> intersection = new HashSet<>(terms1);
            intersection.retainAll(terms2);
            
            Set<String> union = new HashSet<>(terms1);
            union.addAll(terms2);
            
            if (union.isEmpty()) {
                return 0;
            }
            
            return (double) intersection.size() / union.size();
        }
        
        /**
         * Calculates edit distance (Levenshtein distance) between two strings
         */
        public static int editDistance(String s1, String s2) {
            int[][] dp = new int[s1.length() + 1][s2.length() + 1];
            
            // Initialize base cases
            for (int i = 0; i <= s1.length(); i++) {
                dp[i][0] = i;
            }
            for (int j = 0; j <= s2.length(); j++) {
                dp[0][j] = j;
            }
            
            // Fill DP table
            for (int i = 1; i <= s1.length(); i++) {
                for (int j = 1; j <= s2.length(); j++) {
                    if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                    }
                }
            }
            
            return dp[s1.length()][s2.length()];
        }
    }
    
    /**
     * Represents a keyword extractor
     */
    public static class KeywordExtractor {
        private final int maxKeywords;
        
        public KeywordExtractor() {
            this(10);
        }
        
        public KeywordExtractor(int maxKeywords) {
            this.maxKeywords = maxKeywords;
        }
        
        /**
         * Extracts keywords from a document using TF-IDF
         */
        public List<String> extractKeywords(Document document, Corpus corpus) {
            // Calculate TF-IDF scores for all terms
            Map<String, Double> tfidfScores = new HashMap<>();
            Map<String, Integer> termFrequencies = document.getTermFrequencies();
            
            for (String term : termFrequencies.keySet()) {
                double tfidf = corpus.calculateTfIdf(term, document);
                tfidfScores.put(term, tfidf);
            }
            
            // Sort by TF-IDF score and return top keywords
            return tfidfScores.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(maxKeywords)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Represents a text generator
     */
    public static class TextGenerator {
        private final Map<String, List<String>> wordTransitions;
        private final Random random;
        
        public TextGenerator() {
            this.wordTransitions = new HashMap<>();
            this.random = new Random();
        }
        
        /**
         * Trains the generator on a corpus
         */
        public void train(Corpus corpus) {
            wordTransitions.clear();
            
            for (Document document : corpus.getDocuments()) {
                List<String> tokens = document.getTokens();
                
                for (int i = 0; i < tokens.size() - 1; i++) {
                    String currentWord = tokens.get(i);
                    String nextWord = tokens.get(i + 1);
                    
                    wordTransitions.computeIfAbsent(currentWord, k -> new ArrayList<>()).add(nextWord);
                }
            }
        }
        
        /**
         * Generates text of specified length
         */
        public String generateText(int wordCount) {
            if (wordTransitions.isEmpty()) {
                throw new IllegalStateException("Generator must be trained before generating text");
            }
            
            // Select random starting word
            List<String> startWords = new ArrayList<>(wordTransitions.keySet());
            String currentWord = startWords.get(random.nextInt(startWords.size()));
            
            StringBuilder text = new StringBuilder(currentWord);
            
            // Generate subsequent words
            for (int i = 1; i < wordCount; i++) {
                List<String> nextWords = wordTransitions.get(currentWord);
                if (nextWords == null || nextWords.isEmpty()) {
                    // If no transitions, pick random word
                    List<String> allWords = new ArrayList<>(wordTransitions.keySet());
                    currentWord = allWords.get(random.nextInt(allWords.size()));
                } else {
                    // Pick random next word
                    currentWord = nextWords.get(random.nextInt(nextWords.size()));
                }
                
                text.append(" ").append(currentWord);
            }
            
            return text.toString();
        }
    }
    
    /**
     * Represents a spell checker
     */
    public static class SpellChecker {
        private final Set<String> dictionary;
        private final Map<String, List<String>> editsCache;
        
        public SpellChecker() {
            this.dictionary = createDefaultDictionary();
            this.editsCache = new HashMap<>();
        }
        
        /**
         * Checks if a word is spelled correctly
         */
        public boolean isCorrect(String word) {
            return dictionary.contains(word.toLowerCase());
        }
        
        /**
         * Suggests corrections for a misspelled word
         */
        public List<String> suggestCorrections(String word) {
            if (isCorrect(word)) {
                return Arrays.asList(word);
            }
            
            // Get all possible edits
            Set<String> candidates = new HashSet<>();
            
            // Try edits with distance 1
            candidates.addAll(getEdits(word));
            
            // Filter by dictionary
            List<String> suggestions = candidates.stream()
                    .filter(dictionary::contains)
                    .collect(Collectors.toList());
            
            // If no suggestions with distance 1, try distance 2
            if (suggestions.isEmpty()) {
                Set<String> secondLevelCandidates = new HashSet<>();
                for (String candidate : candidates) {
                    secondLevelCandidates.addAll(getEdits(candidate));
                }
                
                suggestions = secondLevelCandidates.stream()
                        .filter(dictionary::contains)
                        .collect(Collectors.toList());
            }
            
            return suggestions.isEmpty() ? Arrays.asList(word) : suggestions;
        }
        
        /**
         * Gets all possible edits (insertions, deletions, substitutions, transpositions)
         */
        private Set<String> getEdits(String word) {
            if (editsCache.containsKey(word)) {
                return new HashSet<>(editsCache.get(word));
            }
            
            Set<String> edits = new HashSet<>();
            String lowerWord = word.toLowerCase();
            
            // Deletions
            for (int i = 0; i < lowerWord.length(); i++) {
                edits.add(lowerWord.substring(0, i) + lowerWord.substring(i + 1));
            }
            
            // Transpositions
            for (int i = 0; i < lowerWord.length() - 1; i++) {
                edits.add(lowerWord.substring(0, i) + lowerWord.charAt(i + 1) + 
                         lowerWord.charAt(i) + lowerWord.substring(i + 2));
            }
            
            // Replacements
            for (int i = 0; i < lowerWord.length(); i++) {
                for (char c = 'a'; c <= 'z'; c++) {
                    edits.add(lowerWord.substring(0, i) + c + lowerWord.substring(i + 1));
                }
            }
            
            // Insertions
            for (int i = 0; i <= lowerWord.length(); i++) {
                for (char c = 'a'; c <= 'z'; c++) {
                    edits.add(lowerWord.substring(0, i) + c + lowerWord.substring(i));
                }
            }
            
            editsCache.put(word, new ArrayList<>(edits));
            return edits;
        }
        
        /**
         * Creates a default dictionary
         */
        private Set<String> createDefaultDictionary() {
            return new HashSet<>(Arrays.asList(
                "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
                "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
                "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
                "or", "an", "will", "my", "one", "all", "would", "there", "their",
                "what", "so", "up", "out", "if", "about", "who", "get", "which", "go",
                "me", "when", "make", "can", "like", "time", "no", "just", "him", "know",
                "take", "people", "into", "year", "your", "good", "some", "could", "them",
                "see", "other", "than", "then", "now", "look", "only", "come", "its", "over",
                "think", "also", "back", "after", "use", "two", "how", "our", "work", "first",
                "well", "way", "even", "new", "want", "because", "any", "these", "give", "day",
                "most", "us", "is", "was", "are", "has", "had", "were", "been", "being",
                "have", "do", "does", "did", "can", "could", "shall", "should", "will", "would",
                "may", "might", "must", "ought", "need", "dare", "used"
            ));
        }
    }
}