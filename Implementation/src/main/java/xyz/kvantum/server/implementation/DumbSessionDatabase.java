/*
 *
 *    Copyright (C) 2017 IntellectualSites
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
package xyz.kvantum.server.implementation;

import xyz.kvantum.server.api.session.ISession;
import xyz.kvantum.server.api.session.ISessionDatabase;
import xyz.kvantum.server.api.session.SessionLoad;

public final class DumbSessionDatabase implements ISessionDatabase
{

    @Override
    public void setup() throws Exception
    {
    }

    @Override
    public SessionLoad getSessionLoad(String sessionID)
    {
        return null;
    }

    @Override
    public void storeSession(ISession session)
    {
    }

    @Override
    public void updateSession(String session)
    {
    }

    @Override
    public void deleteSession(String session)
    {
    }
}
