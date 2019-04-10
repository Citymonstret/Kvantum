/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2019 Alexander Söderberg
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
package xyz.kvantum.example;

import xyz.kvantum.server.api.AccountService;
import xyz.kvantum.server.api.account.AccountMatcherFactory;
import xyz.kvantum.server.api.account.IAccount;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.service.KvantumSearchService;
import xyz.kvantum.server.api.service.RSQLSearchService;
import xyz.kvantum.server.api.util.ParameterScope;
import xyz.kvantum.server.implementation.Account;

class ExampleSearch {

    ExampleSearch() {
        Logger.info("");
        Logger.info("INITIALIZING EXAMPLE: UserSearch");

        // GET /search?username=admin
        // RESULT:
        // {
        //      "status": "success"
        //      "query": {
        //          "id": -1,
        //          "username": "admin"
        //      }
        //      "result": [
        //        {
        //          "id": 1,
        //          "username: "admin"
        //        }
        //      ]
        // }
        KvantumSearchService.<Account, IAccount>builder().filter("search")
            .queryObjectType(Account.class).resultProvider(AccountService.getInstance().getGlobalAccountManager())
            .matcher(new AccountMatcherFactory<>()).parameterScope(ParameterScope.GET).build()
            .createService();

        // GET /rsqlsearch?query=id=lt=30
        // RESULT:
        // {
        //      "status": "success"
        //      "query": "id=lt=30",
        //      "result": [
        //        {
        //          "id": 1,
        //          "username: "admin"
        //        }
        //      ]
        // }
        RSQLSearchService.<IAccount>builder().filter("rsqlsearch")
            .parameterScope(ParameterScope.GET).resultProvider(AccountService.getInstance().getGlobalAccountManager())
            .queryKey("query").build().createService();

        Logger.info("ACCESS THE EXAMPLE AT: /search?username=<username>&id=<id>");
        Logger.info("AND: /rsqlsearch?query=<query>");
        Logger.info("EXAMPLE: /search?username=admin");
        Logger.info("EXAMPLE: /rsqlsearch?query=id=lt=30");
        Logger.info("");
    }

}
