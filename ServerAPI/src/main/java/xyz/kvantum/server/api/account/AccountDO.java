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
package xyz.kvantum.server.api.account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNegative;
import net.sf.oval.constraint.NotNull;
import xyz.kvantum.server.api.account.roles.AccountRole;
import xyz.kvantum.server.api.pojo.KvantumPojo;
import xyz.kvantum.server.api.pojo.KvantumPojoFactory;

@Getter @RequiredArgsConstructor @EqualsAndHashCode @ToString @SuppressWarnings({ "unused",
		"WeakerAccess" }) public final class AccountDO
{

	@Getter private static final KvantumPojoFactory<AccountDO> kvantumPojoFactory = KvantumPojoFactory
			.forClass( AccountDO.class );
	private static final Validator validator = new Validator();

	@NotNegative private final int id;

	@NotEmpty @NotNull @NonNull private final String username;

	@NonNull @NotNull private final Collection<AccountRole> accountRoles;

	public AccountDO(@NotNull final IAccount account)
	{
		this.id = account.getId();
		this.username = account.getUsername();
		this.accountRoles = new ArrayList<>( account.getAccountRoles() );
	}

	public List<ConstraintViolation> validate()
	{
		return validator.validate( this );
	}

	public KvantumPojo<AccountDO> toPojo()
	{
		return kvantumPojoFactory.of( this );
	}
}
