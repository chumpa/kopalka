package io.rsug.kopalka;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Tree {
    String name;
    long size = 0;
    boolean isZip = false;
    boolean isJavaClass = false;
    final LinkedHashMap<String, Tree> children = new LinkedHashMap<>();
}

/**
 * точка входа всегда путь на диске -- файл или директория
 */
public class Kop {
    public static final Path tmpDir = Paths.get("tmp");
    public static final long BARRIER = 1024*1024;
    Kop() throws IOException {
        if (!Files.isDirectory(tmpDir)) Files.createDirectories(tmpDir);
    }

    static SimpleFileVisitor<Path> fileVisitorDeleteAll = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(Objects.requireNonNull(file));
            Objects.requireNonNull(attrs);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(Objects.requireNonNull(dir));
            return FileVisitResult.CONTINUE;
        }
    };

    ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            System.out.println("Имя класса: " + name);
            System.out.println("Суперкласс: " + superName);
            System.out.println("Версия класса (major): " + version);
            System.out.println("Модификаторы: " + access);
            // version 69 = Java 25 (0x45)
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc,
                                       String signature, Object value) {
            System.out.println("Поле: " + name + " (" + desc + ")");
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc,
                                         String signature, String[] exceptions) {
            System.out.println("Метод: " + name + desc);
            return null;
        }
    };

    public Tree file(Path regularFile) throws IOException {
        byte[] magic = new byte[512];
        List<Path> deleteFiles = new LinkedList<>();
//        List<Path> deleteDirs = new LinkedList<>();
        Tree tree = new Tree();
        tree.name = regularFile.getName(regularFile.getNameCount() - 1).toString();
        tree.size = Files.size(regularFile);
        int first = Math.toIntExact(Math.min(magic.length, tree.size));
        IOUtils.read(Files.newInputStream(regularFile), magic, 0, first);
        tree.isZip = isZip(magic, first);
        tree.isJavaClass = isJavaClass(magic, first);
        if (tree.isJavaClass) {
            ClassReader reader = new ClassReader(Files.newInputStream(regularFile));
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        } else if (tree.isZip) {
            boolean useFile = tree.size > BARRIER;
            // создали временную папку
            Path tmpDirUnzipped = Files.createTempDirectory(tmpDir, tree.name + "__");
            // распаковываем архив, запоминаем имена файлов к обходу
            LinkedHashMap<String, Path> subparse = new LinkedHashMap<>();
            ZipInputStream zis = new ZipInputStream(Files.newInputStream(regularFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                Path that = tmpDirUnzipped.resolve(name);
                if (!ze.isDirectory()) {
                    Path parentPath = that.getParent();
                    if (!Files.exists(parentPath)) {
                        Files.createDirectories(parentPath);
                    }
                    OutputStream fos = Files.newOutputStream(that);
                    IOUtils.copy(zis, fos);
                    fos.close();
                    subparse.put(name, that);
                    deleteFiles.add(that);
                }
                ze = zis.getNextEntry();
            }
            zis.close();
            // начинаем обходить
            for (Map.Entry<String, Path> sp : subparse.entrySet()) {
                String key = sp.getKey();
                Path regFile = sp.getValue();
                Tree child = file(regFile);
                tree.children.put(key, child);
                deleteFiles.add(regFile);
            }
            for (Path p : deleteFiles) {
                Files.deleteIfExists(p);
            }
            Files.walkFileTree(tmpDirUnzipped, fileVisitorDeleteAll);
        }
        return tree;
    }

    public Tree dir(Path directory) throws IOException {
        Tree tree = new Tree();
        tree.name = directory.getFileName().toString();
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
        for (Path p : directoryStream) {
            System.out.printf("Starting to handle %s\n", p);
            Tree sub = file(p);
            tree.children.put(p.getFileName().toString(), sub);
        }
        return tree;
    }

    public static boolean isZip(byte[] magic, int length) {
        Objects.requireNonNull(magic);
        if (length > 20 && magic[0] == (byte) 0x50 && magic[1] == (byte) 0x4B) {
            int zeroes = 0;
            for (int i = 2; i < length; i++) {
                if (magic[i] == 0) zeroes++;
            }
            return zeroes > 2;
        }
        return false;
    }

    public static boolean isJavaClass(byte[] magic, int length) {
        Objects.requireNonNull(magic);
        if (length > 200 && magic[0] == (byte) 0xCA && magic[1] == (byte) 0xFE && magic[2] == (byte) 0xBA && magic[3] == (byte) 0xBE) {
            int zeroes = 0;
            for (int i = 2; i < length; i++) {
                if (magic[i] == 0) zeroes++;
            }
            return zeroes > 10;
        }
        return false;
    }

    void wipe() throws IOException {
        Files.walkFileTree(tmpDir, fileVisitorDeleteAll);
        Files.createDirectories(tmpDir);
    }
}
