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
package xyz.kvantum.server.implementation.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.EqualsAndHashCode;
import xyz.kvantum.files.Path;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.util.AutoCloseable;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class for dealing with common MySQL operations
 */
@SuppressWarnings({"unused", "WeakerAccess"}) @EqualsAndHashCode(of = "name", callSuper = false)
public class MySQLManager extends AutoCloseable {

    private final String name;
    private final HikariDataSource dataSource;

    public MySQLManager(final String name) {
        this.name = name;

        final Properties properties = new Properties();
        final Path path = ServerImplementation.getImplementation().getFileSystem().getPath("config")
            .getPath("mysql-" + name + ".properties");
        if (!path.exists()) {
            path.create();
        }
        try (final FileReader reader = path.getReader()) {
            properties.load(reader);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        properties.putIfAbsent("jdbcUrl", "jdbc:mysql://localhost:3306/database");
        properties.putIfAbsent("username", "username");
        properties.putIfAbsent("password", "password");
        try (final FileWriter writer = path.getWriter()) {
            properties.store(writer, null);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final HikariConfig hikariConfig = new HikariConfig(properties);
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void executeUpdate(final String sql) throws SQLException {
        try (final Connection connection = this.getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        }
    }

    @Override protected void handleClose() {
        if (this.dataSource != null && this.dataSource.isRunning()) {
            this.dataSource.close();
        }
    }
}
