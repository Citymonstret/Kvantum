package com.intellectualsites.web.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created 2015-04-23 for IntellectualServer
 *
 * @author Citymonstret
 */
public class FileUtils {

    public static void addToZip(File zipFile, File[] files, boolean delete) throws Exception {
        Assert.notNull(zipFile, files);

        if (!zipFile.exists()) {
            zipFile.createNewFile();
        }

        File temporary = File.createTempFile(zipFile.getName(), "");
        temporary.delete();

        if (!zipFile.renameTo(temporary)) {
            throw new RuntimeException("Couldn't rename " + zipFile + " to " + zipFile);
        }

        byte[] buffer = new byte[1024 * 16]; // 16mb

        ZipInputStream zis = new ZipInputStream(new FileInputStream(temporary));
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

        ZipEntry e = zis.getNextEntry();
        while (e != null) {
            String n = e.getName();

            boolean no = true;
            for (File f : files) {
                if (f.getName().equals(n)) {
                    no = false;
                    break;
                }
            }

            if (no) {
                zos.putNextEntry(new ZipEntry(n));
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            e = zis.getNextEntry();
        }
        zis.close();
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);
            zos.putNextEntry(new ZipEntry(files[i].getName()));

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            zos.closeEntry();
            in.close();
        }
        zos.close();
        temporary.delete();

        if (delete) {
            for (File f : files) {
                f.delete();
            }
        }
    }


    /**
     * Copy a file from one location to another
     *
     * @param in   Ingoing File
     * @param out  Outgoing File
     * @param size Byte Buffer Size (in bytes)
     */
    public static void copyFile(final InputStream in, final OutputStream out,
                                final int size) {
        Assert.notNull(in, out, size);
        try {
            final byte[] buffer = new byte[size];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getDocument(final File file, int buffer) {
        StringBuilder document = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file), buffer);
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            reader.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return document.toString();
    }

    /**
     * Get the size of a file or directory
     *
     * @param file File
     * @return Size of file
     */
    public static long getSize(final File file) {
        long size = 0;
        if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (files == null)
                return size;
            for (final File f : files)
                if (f.isFile())
                    size += f.length();
                else
                    size += getSize(file);
        } else if (file.isFile())
            size += file.length();
        return size;
    }
}
