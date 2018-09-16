package translate.tools;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Translator {

    private String translationSystemAddress;
    private int translationSystemPortNumber;

    private Socket socket;
    private PrintWriter outputToTranslate;
    private BufferedReader inputWithTranslation;

    public Translator(String translationSystemAddress, int translationSystemPortNumber) {
       this.translationSystemAddress = translationSystemAddress;
       this.translationSystemPortNumber = translationSystemPortNumber;
       initializeConnection();
    }

    private void initializeConnection() {
        try {
            socket = new Socket(translationSystemAddress, translationSystemPortNumber);
            socket.setKeepAlive(true);
            outputToTranslate = new PrintWriter(socket.getOutputStream(), true);
            inputWithTranslation = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> translate(List<String> documentLines) {
        List<String> translation = new ArrayList<>();
        List<String> documentSentences = convertLinesToSentences(documentLines);

        boolean retryTranslation = false;
        do {
            try {
                translation = tryToTranslate(documentSentences);
                retryTranslation = false;
            } catch(Exception e) {
                initializeConnection();
                retryTranslation = true;
            }

        } while(retryTranslation);

        return translation;
    }

    private List<String> convertLinesToSentences(List<String> documentLines) {
        List<String> resultTokenizedText = new ArrayList<>();

        List<String> documentLinesClean = documentLines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> line.replace("\n", "").replace("\r", ""))
                .collect(Collectors.toList());

        for (String nextLine : documentLinesClean) {
            List<String> sentences = SentenceSplitter.process(nextLine);
            List<String> sentences_clean = sentences.stream().map(String::trim).filter(line -> !line.isEmpty()).collect(Collectors.toList());
            resultTokenizedText.addAll(sentences_clean);
        }

        return resultTokenizedText;
    }

    private List<String> tryToTranslate(List<String> input) throws IOException {
        List<String> translatedSentences = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            String nextPolishSentence = input.get(i);
            outputToTranslate.println(nextPolishSentence);
            String translation = inputWithTranslation.readLine();
            if (i == 0 && translation == null) {
                throw new IOException("Connection timeout.");
            }
            translatedSentences.add(translation.replace("\n", "").replace("\r", ""));
        }

        return translatedSentences;
    }
}
