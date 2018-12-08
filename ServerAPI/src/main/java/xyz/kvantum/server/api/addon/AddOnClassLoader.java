/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 Alexander Söderberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.kvantum.server.api.addon;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class AddOnClassLoader extends URLClassLoader {

    private final AddOnManager addOnManager;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    @Getter private final AddOn addOn;
    @Getter private final File file;
    @Getter private final String name;

    @Getter @Setter(AccessLevel.PROTECTED) private boolean disabling;

    AddOnClassLoader(@Nonnull @NonNull final AddOnManager addOnManager, @Nonnull @NonNull final File file,
        @NonNull final String name) throws AddOnLoaderException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, addOnManager.getClass().getClassLoader());

        this.addOnManager = addOnManager;
        this.file = file;
        this.name = name;
        this.addOn = null; // This is a library

        if (!file.toPath().getFileName().toString().equalsIgnoreCase(name)) {
            throw new AddOnLoaderException("File name does not match addon name...");
        }
    }

    AddOnClassLoader(@Nonnull @NonNull final AddOnManager addOnManager, @Nonnull @NonNull final File file,
        @NonNull final String mainFile, @NonNull final String name)
        throws AddOnLoaderException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, addOnManager.getClass().getClassLoader());

        this.addOnManager = addOnManager;
        this.file = file;

        Class<?> mainClass;
        try {
            mainClass = Class.forName(mainFile, true, this);
            this.classes.put(mainClass.getName(), mainClass);
        } catch (final ClassNotFoundException e) {
            throw new AddOnLoaderException("Could not find main class for addOn " + name);
        }
        Class<? extends AddOn> addOnMain;
        try {
            addOnMain = mainClass.asSubclass(AddOn.class);
        } catch (final Exception e) {
            throw new AddOnLoaderException(mainFile + " does not implement AddOn");
        }
        try {
            this.addOn = addOnMain.getConstructor().newInstance(); // addOnMain.newInstance();
            this.addOn.setClassLoader(this);
            this.addOn.setName(name);
            this.name = name;
        } catch (final Exception e) {
            throw new AddOnLoaderException("Failed to load main class for " + name, e);
        }
    }

    @Override protected Class<?> findClass(final String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(final String name, final boolean global) throws ClassNotFoundException {
        if (this.isDisabling()) {
            throw new ClassNotFoundException("This class loader is disabling...");
        }
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            Class<?> clazz = null;
            if (global) {
                clazz = addOnManager.findClass(name);
            }
            if (clazz == null) {
                clazz = super.findClass(name);
                if (clazz != null) {
                    addOnManager.setClass(name, clazz);
                }
            }
            return clazz;
        }
    }

    void removeClasses() {
        if (!this.isDisabling()) {
            throw new IllegalStateException(
                "Cannot remove class when the loader isn't disabling...");
        }
        this.addOnManager.removeAll(this.classes);
        this.classes.clear();
    }

    private static class AddOnLoaderException extends RuntimeException {

        private AddOnLoaderException(String error) {
            super(error);
        }

        private AddOnLoaderException(String error, Throwable cause) {
            super(error, cause);
        }

    }

}
