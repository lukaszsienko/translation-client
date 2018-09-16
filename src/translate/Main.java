package translate;

import translate.tools.LanguageDetector;
import translate.tools.Translator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static LanguageDetector languageDetector;
    private static Translator translator;

    public static void main(String[] args) throws Exception {
        String pathToText = "";
        String translatorServerAddress = "";
        int translatorServerPortNum = 0;

        Path textToTranslatePath = Paths.get(pathToText);
        List<String> textLines = readDocument(textToTranslatePath);

        //// Language detection
        languageDetector = new LanguageDetector();
        String input = textLines.stream().collect(Collectors.joining("\n"));
        LanguageDetector.Language language = languageDetector.detectLanguage(input);

        System.out.println("Detected language: "+language.getLanguageCode()+"\n\n");

        ///// Translation
        translator = new Translator(translatorServerAddress, translatorServerPortNum);
        List<String> translationLines = translator.translate(textLines);
        String translation = translationLines.stream().collect(Collectors.joining("\n"));

        System.out.println(translation);
    }

    private static List<String> readDocument(Path filePath) {
        List<String> document = new ArrayList<>();
        try {
            document = new ArrayList<>(Files.readAllLines(filePath, Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

}
