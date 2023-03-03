package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static long chars, words, sentences, syllables, polysyls;
    private static int ariAge, fkAge, smogAge, clAge;

    public static void main(String[] args) {
        String fileName = args[0];
        try (Scanner scanner = new Scanner(Files.newInputStream(Path.of(fileName)))) {

            List<String> input = new ArrayList<>();
            while (scanner.hasNextLine()) {
                input.add(scanner.nextLine());
            }

            final String TEXT = String.join("\n", input);

            processText(TEXT);

            StringBuilder sb = new StringBuilder();
            sb.append("The text is:\n");
            sb.append(TEXT).append("\n\n");
            sb.append("Words: ").append(words).append("\n");
            sb.append("Sentences: ").append(sentences).append("\n");
            sb.append("Characters: ").append(chars).append("\n");
            sb.append("Syllables: ").append(syllables).append("\n");
            sb.append("Polysyllables: ").append(polysyls).append("\n");
            sb.append("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");

            System.out.print(sb);

            Scanner stdin = new Scanner(System.in);
            if (!stdin.hasNextLine()) throw new RuntimeException("No score calculation selected");
            
            String toCompute = stdin.nextLine();

            System.out.println();

            switch (toCompute) {
                case "ARI" -> reportARI();
                case "FK" -> reportFK();
                case "SMOG" -> reportSMOG();
                case "CL" -> reportCL();
                // treat any other input as "all"
                default -> {
                    reportARI();
                    reportFK();
                    reportSMOG();
                    reportCL();
                    reportAvgAge();
                }
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void processText(String TEXT) {
        chars = 0;
        words = 0;
        sentences = 0;
        syllables = 0;
        polysyls = 0;

        String[] textWords = TEXT.split("\\s");

        for (String w : textWords) {
            int len = w.length();
            if (len > 0) {
                chars += len;
                words++;
                if (w.matches(".*[.?!]")) sentences++;
            }
            int sylInWord = countSyllables(w);
            syllables += sylInWord;
            if (sylInWord > 2) polysyls++;
        }
        // check for final punctuation after last word
        if (!TEXT.matches(".*[.?!]")) sentences++;
    }

    private static int countSyllables(String word) {
        final List<Character> vowels = List.of('a','e','i','o','u','y');
        int syls = 0;
        String letters = word.replaceAll("\\W", "").toLowerCase();
        int lettersLen = letters.length();
        for (int i = 0; i < lettersLen; i++) {
            if (vowels.contains(letters.charAt(i))) {
                // previous letter is also a vowel and was already counted
                if (i != 0 && vowels.contains(letters.charAt(i-1))) continue;
                // this is the last letter and an 'e' so ignore it
                if (i == lettersLen - 1 && 'e' == letters.charAt(i)) continue;
                syls++;
            }
        }
        // always report at least 1 syllable
        return syls == 0 ? 1 : syls;
    }

    private static void reportARI() {
        double ari = calculateARI();
        ariAge = convertScoreToAge(ari);
        printScore("Automated Readability Index", ari, ariAge);
    }

    private static void reportFK() {
        double fk = calculateFK();
        fkAge = convertScoreToAge(fk);
        printScore("Flesch–Kincaid readability tests", fk, fkAge);
    }

    private static void reportSMOG() {
        double smog = calculateSMOG();
        smogAge = convertScoreToAge(smog);
        printScore("Simple Measure of Gobbledygook", smog, smogAge);
    }

    private static void reportCL() {
        double cl = calculateCL();
        clAge = convertScoreToAge(cl);
        printScore("Coleman–Liau index", cl, clAge);
    }

    private static void printScore(String scoreName, double score, int scoreAge) {
        System.out.println(scoreName + ": " + score + " (about " + scoreAge + "-year-olds).");
    }
    
    private static void reportAvgAge() {
        double avgAge = (ariAge + fkAge + smogAge + clAge) / 4.0;
        System.out.print("\nThis text should be understood in average by " + avgAge + "-year-olds.\n");
    }

    private static double calculateARI() {
        if (words == 0 || sentences == 0) {
            throw new UnsupportedOperationException("Can't calculate ARI with 0 words or sentences");
        }
        return (4.71 * ((double) chars/words)) + (0.5 * ((double) words/sentences)) - 21.43;
    }

    private static double calculateFK() {
        if (words == 0 || sentences == 0) {
            throw new UnsupportedOperationException("Can't calculate FK with 0 words or sentences");
        }
        return (0.39 * ((double) words/sentences)) + (11.8 * ((double) syllables/words)) - 15.59;
    }

    private static double calculateSMOG() {
        if (sentences == 0) {
            throw new UnsupportedOperationException("Can't calculate SMOG with 0 sentences");
        }
        return (1.043 * Math.sqrt(polysyls * (30.0/sentences))) + 3.1291;
    }

    private static double calculateCL() {
        if (words == 0) {
            throw new UnsupportedOperationException("Can't calculate CL with 0 words");
        }
        return (5.88 * ((double) chars/words)) - (29.6 * ((double) sentences/words)) - 15.8;
    }

    /** Rounds the {@param score} to an integer and {@return the correlated age} */
    private static int convertScoreToAge(double score) {
        int roundedScore = (int)Math.ceil(score);
        return roundedScore == 14 ? 22 : roundedScore + 5;
    }


}
