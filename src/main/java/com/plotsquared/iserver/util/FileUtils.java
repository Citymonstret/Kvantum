//////////////////////////////////////////////////////////////////////////////////////////////////////////////
//     IntellectualServer is a web server, written entirely in the Java language.                            /
//     Copyright (C) 2015 IntellectualSites                                                                  /
//                                                                                                           /
//     This program is free software; you can redistribute it and/or modify                                  /
//     it under the terms of the GNU General Public License as published by                                  /
//     the Free Software Foundation; either version 2 of the License, or                                     /
//     (at your option) any later version.                                                                   /
//                                                                                                           /
//     This program is distributed in the hope that it will be useful,                                       /
//     but WITHOUT ANY WARRANTY; without even the implied warranty of                                        /
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                         /
//     GNU General Public License for more details.                                                          /
//                                                                                                           /
//     You should have received a copy of the GNU General Public License along                               /
//     with this program; if not, write to the Free Software Foundation, Inc.,                               /
//     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.                                           /
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.plotsquared.iserver.util;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * File Utilitiy Methods
 *
 * @author Citymonstret
 */
public class FileUtils {

    /**
     * Add files to a zip file
     *
     * @param zipFile Zip File
     * @param files   Files to add to the zip
     * @param delete  If the original files should be deleted
     * @throws Exception If anything goes wrong
     */
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
        for (File file : files) {
            InputStream in = new FileInputStream(file);
            zos.putNextEntry(new ZipEntry(file.getName()));

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

    /**
     * Get file contents as a string
     *
     * @param file   File to read
     * @param buffer File buffer
     * @return String
     */
    public static String getDocument(final File file, int buffer) {
        return getDocument(file, buffer, false);
    }

    public static byte[] getBytes(final File file, final int buffer) {
        byte[] bytes = new byte[0];
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file), buffer);
            bytes = IOUtils.toByteArray(stream);
            stream.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static String getDocument(final File file, int buffer, boolean create) {
        StringBuilder document = new StringBuilder();
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (create) {
                    file.createNewFile();
                    return "";
                }
            }

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
