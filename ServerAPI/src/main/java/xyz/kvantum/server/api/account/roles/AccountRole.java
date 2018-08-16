/*
 *    _  __                     _
 *    | |/ /__   __ __ _  _ __  | |_  _   _  _ __ ___
 *    | ' / \ \ / // _` || '_ \ | __|| | | || '_ ` _ \
 *    | . \  \ V /| (_| || | | || |_ | |_| || | | | | |
 *    |_|\_\  \_/  \__,_||_| |_| \__| \__,_||_| |_| |_|
 *
 *    Copyright (C) 2018 IntellectualSites
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
package xyz.kvantum.server.api.account.roles;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles are attached to accounts and may be used to check whether or not an user is permitted to perform certain
 * actions
 */
@EqualsAndHashCode(of = "roleIdentifier") @RequiredArgsConstructor(access = AccessLevel.PROTECTED) @SuppressWarnings({
		"unused", "WeakerAccess" }) public abstract class AccountRole
{

	@Getter private final String roleIdentifier;

	/**
	 * Check if the role is permitted to perform an action
	 *
	 * @param permissionKey Permission key
	 * @return boolean indicating whether or not the account has a certain permission
	 */
	public abstract boolean hasPermission(String permissionKey);

	/**
	 * Add a permission to the role
	 *
	 * @param permissionKey Permission key
	 * @return boolean indicating whether the permission was successfully added or not
	 */
	public abstract boolean addPermission(String permissionKey);

	/**
	 * Remove a permission from the role
	 *
	 * @param permissionKey Permission key
	 * @return boolean indicating whether the permission was successfully removed or not
	 */
	public abstract boolean removePermission(String permissionKey);

}
