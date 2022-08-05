package com.example.tracing.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileReaderUtil {

    public static final String INPUT_DELIMITER = "\\s*,\\s*";

    public static Scanner getInitializedScanner(String inputGraphFilePath) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(inputGraphFilePath));
        scanner.useDelimiter(INPUT_DELIMITER);
        return scanner;
    }

}
