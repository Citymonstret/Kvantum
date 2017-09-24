/**
 * IntellectualServer is a web server, written entirely in the Java language.
 * Copyright (C) 2015 IntellectualSites
 * <p>
 * This program is free software; you can redistribute it andor modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.plotsquared.iserver.api.validation;

import com.plotsquared.iserver.api.views.RequestHandler;

import java.util.*;

public class ValidationManager
{

    private final RequestHandler parent;
    private final Map<RequestValidation.ValidationStage, List<RequestValidation>> validators;
    private boolean empty = true;

    public ValidationManager(final RequestHandler parent)
    {
        this.parent = parent;
        this.validators = new HashMap<>();
        for ( final RequestValidation.ValidationStage stage : RequestValidation.ValidationStage.values() )
        {
            validators.put( stage, new ArrayList<>() );
        }
    }

    public boolean isEmpty()
    {
        return this.empty;
    }

    public List<RequestValidation> getValidators(final RequestValidation.ValidationStage stage)
    {
        return Collections.unmodifiableList( validators.get( stage ) );
    }

    public void addValidator(final RequestValidation validator)
    {
        this.empty = false;
        this.validators.get( validator.getStage() ).add( validator );
    }

}
