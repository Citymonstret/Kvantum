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

}
