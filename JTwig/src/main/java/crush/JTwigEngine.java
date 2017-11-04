/*
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2017 IntellectualSites
 *
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package crush;

import com.github.intellectualsites.iserver.api.config.CoreConfig;
import com.github.intellectualsites.iserver.api.config.Message;
import com.github.intellectualsites.iserver.api.core.ServerImplementation;

public class JTwigEngine
{

    private static JTwigEngine instance;

    private JTwigEngine()
    {
    }

    public static JTwigEngine getInstance()
    {
        if ( instance == null )
        {
            instance = new JTwigEngine();
        }
        return instance;
    }

    public void load()
    {
        Message.TEMPLATING_ENGINE_STATUS.log( "JTwigEngine",
                CoreConfig.Templates.status( CoreConfig.TemplatingEngine.JTWIG ) );

        if ( !CoreConfig.Templates.status( CoreConfig.TemplatingEngine.JTWIG ) )
        {
            return;
        }

        ServerImplementation.getImplementation().getProcedure().addProcedure( "syntax", new SyntaxHandler() );
    }
}
