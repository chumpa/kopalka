package io.rsug.kopalka;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    static void main(String[] args) throws Exception {
        String iv = Main.class.getPackage().getImplementationVersion();
        System.out.println("Копалка v" + iv);
        if (args == null || args.length == 0) {
            System.out.println("Нет аргументов - выход");
            System.exit(0);
        }
        Kop kop = new Kop();
        for (String a : args) {
            Path x = Paths.get(a);
            if (Files.isDirectory(x)) {
                System.out.printf("Обработка директории `%s`:\n", x);
                kop.dir(x);
            } else if (Files.isRegularFile(x)) {
                System.out.printf("Обработка файла `%s`:\n", x);
                kop.file(x);
            } else if (Files.exists(x)) {
                System.err.printf("Нет обработчика для `%s`\n", x);
            }
        }
        kop.wipe();
    }
}
