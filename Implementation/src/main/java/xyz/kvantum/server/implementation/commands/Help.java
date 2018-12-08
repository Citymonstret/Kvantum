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
package xyz.kvantum.server.implementation.commands;

import com.intellectualsites.commands.Command;
import com.intellectualsites.commands.CommandInstance;
import com.intellectualsites.commands.CommandManager;
import com.intellectualsites.commands.pagination.PaginatedCommand;
import com.intellectualsites.commands.pagination.PaginationFactory;
import com.intellectualsites.commands.parser.impl.IntegerParser;
import xyz.kvantum.server.api.config.Message;

public class Help extends PaginatedCommand<Command> {

    public Help(final CommandManager parent) {
        super(Command.class, parent::getCommands, 3, "help", "/help [page]",
            "Show a list of commands", "", new String[] {"h"}, Object.class);
        withArgument("page", new IntegerParser(), "The page");
    }

    @Override public boolean handleTooBigPage(CommandInstance commandInstance, int i, int i1) {
        commandInstance.getCaller()
            .message("The entered page number is too large (" + (i + 1) + ">" + i1 + ")");
        return true;
    }

    @Override public boolean onCommand(PaginatedCommandInstance<Command> paginatedCommandInstance) {
        final PaginationFactory.Page<Command> commandPage = paginatedCommandInstance.getPage();
        Message.CMD_HELP_HEADER
            .log(commandPage.getPageNum() + 1, this.getPaginationFactory().getPages().size());
        for (final Command command : commandPage.getItems()) {
            Message.CMD_HELP_ITEM
                .log(command.getCommand(), command.getUsage(), command.getDescription());
        }
        if (commandPage.getPageNum() + 1 < this.getPaginationFactory().getPages().size()) {
            Message.CMD_HELP_FOOTER.log(commandPage.getPageNum() + 2);
        }
        return true;
    }
}
