package io.rsug.kopalka;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class KopalkaTests {

    void rec(int deep, Tree parent) {
//        System.out.printf("%s\\%s\n", "-".repeat(deep), parent.name);
        for (Map.Entry<String, Tree> sub: parent.children.entrySet()) {
            System.out.printf("%s/%s\n", "-".repeat(deep+1), sub.getKey());
            rec(deep+1, sub.getValue());
        }
    }

    @Test
    public void classDirTest() throws Exception {
        Kop kop = new Kop();
        Path t = Paths.get("src/test/resources/class");
        Assertions.assertTrue(Files.isDirectory(t));
        Tree tree = kop.dir(t);
        System.out.println("classDirTest()\n" + tree.name);
        rec(0, tree);
        kop.wipe();
    }

    @Test
    public void scaTest() throws Exception {
        Kop kop = new Kop();
        Path t = Paths.get("src/test/resources/po75sp35/WDADOBE35_0-80000729.SCA");
        Assertions.assertTrue(Files.isRegularFile(t));
        Tree tree = kop.file(t);
        System.out.println("scaTest()\n" + tree.name);
        rec(0, tree);
        kop.wipe();
    }

    @Test
    public void dirSmallTest() throws Exception {
        Kop kop = new Kop();
        Path t = Paths.get("src/test/resources/po75sp35");
        Assertions.assertTrue(Files.isDirectory(t));
        Tree tree = kop.dir(t);
        System.out.println("dirSmallTest()\n" + tree.name);
        rec(0, tree);
        kop.wipe();
    }

    @Test
    public void dirTest() throws Exception {
        Kop kop = new Kop();
        Path t = Paths.get("D:\\distr\\SAP_PO\\PO75sp35");
        Assertions.assertTrue(Files.isDirectory(t));
        Tree tree = kop.dir(t);
        System.out.println("dirTest(BIG)\n" + tree.name);
    }
}
