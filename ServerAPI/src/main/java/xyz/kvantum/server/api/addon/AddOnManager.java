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
package xyz.kvantum.server.api.addon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import xyz.kvantum.server.api.util.AutoCloseable;
import xyz.kvantum.server.api.util.CollectionUtil;

/**
 * Manages (loads, unloads, enables and disables) {@link AddOn addons}
 */
@SuppressWarnings("unused") public final class AddOnManager extends AutoCloseable
{

	@Getter private final File addOnFolder;
	private final Collection<String> disabledAddons;
	private Map<String, Class> globalClassMap = new ConcurrentHashMap<>();
	private Map<String, AddOnClassLoader> classLoaders = new ConcurrentHashMap<>();

	public AddOnManager(@NonNull final File addOnFolder, final Collection<String> disabledAddons)
	{
		this.addOnFolder = addOnFolder;
		this.disabledAddons = new HashSet<>( disabledAddons );
	}

	/**
	 * Load all addons in the specified addon folder
	 *
	 * @throws AddOnManagerException If the loading of the addons fails for any reason
	 */
	@SuppressWarnings("WeakerAccess") public void load() throws AddOnManagerException
	{
		if ( !addOnFolder.exists() )
		{
			if ( !addOnFolder.mkdir() )
			{
				throw new AddOnManagerException( "Couldn't create AddOn folder" );
			}
		}
		final File[] files = addOnFolder.listFiles( (file, name) -> {
			final String fileName = name.toLowerCase( Locale.ENGLISH );
			return fileName.endsWith( ".jar" ) || fileName.endsWith( ".zip" );
		} );
		if ( files == null )
		{
			return;
		}
		final List<File> fileList = Arrays.asList( files );
		//
		// This makes sure that libraries are loaded before addons
		//
		fileList.stream().filter( this::isLibrary ).forEach( this::loadAddon );
		fileList.stream().filter( file -> !this.isLibrary( file ) ).forEach( this::loadAddon );
	}

	@Nullable private Properties getAddOnProperties(@NonNull final File file) throws AddOnManagerException
	{
		final JarFile jar;
		try
		{
			jar = new JarFile( file );
		} catch ( final IOException e )
		{
			throw new AddOnManagerException( "Failed to create jar object from " + file.getName(), e );
		}
		final JarEntry desc = jar.getJarEntry( "addon.properties" );
		if ( desc == null )
		{
			return null; // Nullable
		}
		final Properties properties = new Properties();
		try ( final InputStream stream = jar.getInputStream( desc ) )
		{
			properties.load( stream );
		} catch ( final Exception e )
		{
			throw new AddOnManagerException( "Failed to load \"addon.properties\" in " + file.getName() );
		}
		return properties;
	}

	void setClass(@NonNull final String name, @NonNull Class<?> clazz)
	{
		this.globalClassMap.put( name, clazz );
	}

	@Nullable Class<?> findClass(@NonNull final String name)
	{
		if ( this.globalClassMap.containsKey( name ) )
		{
			return this.globalClassMap.get( name );
		}
		Class<?> clazz;
		for ( final AddOnClassLoader loader : this.classLoaders.values() )
		{
			try
			{
				if ( ( clazz = loader.findClass( name, false ) ) != null )
				{
					this.globalClassMap.put( name, clazz );
					return clazz;
				}
			} catch ( final Exception ignored )
			{
			}
		}
		return null; // Nullable
	}

	/**
	 * Get an immutable copy of the underlying library list
	 *
	 * @return All loaded libraries
	 */
	@SuppressWarnings("WeakerAccess") public Collection<String> getLibraries()
	{
		return Collections.unmodifiableList(
				this.classLoaders.values().stream().filter( loader -> loader.getAddOn() == null )
						.map( AddOnClassLoader::getName ).collect( Collectors.toList() ) );
	}

	/**
	 * Get an immutable copy of the underlying addon list
	 *
	 * @return All loaded addons
	 */
	@SuppressWarnings("WeakerAccess") public Collection<AddOn> getAddOns()
	{
		return Collections.unmodifiableList(
				this.classLoaders.values().stream().filter( loader -> loader.getAddOn() != null )
						.map( AddOnClassLoader::getAddOn ).collect( Collectors.toList() ) );
	}

	/**
	 * Get the instance of an addon, if it exists
	 *
	 * @param clazz Addon class
	 * @return Instance, if it can be found
	 */
	@SuppressWarnings("WeakerAccess") public <T extends AddOn> Optional<T> getAddOnInstance(
			@NonNull final Class<T> clazz)
	{
		return this.classLoaders.values().stream().filter( loader -> loader.getAddOn() != null ).
				map( AddOnClassLoader::getAddOn ).filter( addOn -> addOn.getClass().equals( clazz ) ).map( clazz::cast )
				.findAny();
	}

	@SuppressWarnings("WeakerAccess") public Optional<AddOn> getAddOnInstance(@NonNull final String addOnName)
	{
		if ( this.classLoaders.containsKey( addOnName ) )
		{
			return Optional.of( this.classLoaders.get( addOnName ).getAddOn() );
		}
		return Optional.empty();
	}

	/**
	 * Unload an addon
	 *
	 * @param clazz Addon class
	 */
	@SuppressWarnings("WeakerAccess") public <T extends AddOn> void unloadAddon(@NonNull final Class<T> clazz)
	{
		getAddOnInstance( clazz ).ifPresent( this::unloadAddon );
	}

	/**
	 * Unload an addon
	 *
	 * @param addOn Addon instance
	 * @throws AddOnManagerException If anything happens during the unloading of the addon
	 */
	@SuppressWarnings("WeakerAccess") public <T extends AddOn> void unloadAddon(@NonNull final T addOn)
			throws AddOnManagerException
	{
		if ( addOn.isEnabled() )
		{
			addOn.disable();
		}
		@NonNull final AddOnClassLoader classLoader = addOn.getClassLoader();
		classLoader.setDisabling( true );
		classLoader.removeClasses();
		final String name = addOn.getName();
		if ( !this.classLoaders.containsKey( name ) )
		{
			throw new AddOnManagerException( "Cannot find class loader by name \"" + name + "\". Panicking!" );
		}
		this.classLoaders.remove( name );
	}

	/**
	 * Will disable, unload, load and enable the addon
	 *
	 * @param addOn Addon
	 * @return New AddOn instance
	 * @throws AddOnManagerException If anything goes wrong
	 */
	@SuppressWarnings("WeakerAccess") public <T extends AddOn> AddOn reloadAddon(@NonNull final T addOn)
			throws AddOnManagerException
	{
		if ( addOn.isEnabled() )
		{
			addOn.disable();
		}
		final File file = addOn.getClassLoader().getFile();
		this.unloadAddon( addOn );
		final AddOnClassLoader classLoader = this.loadAddon( file );
		if ( classLoader != null && classLoader.getAddOn() != null )
		{
			classLoader.getAddOn().enable();
		} else
		{
			throw new AddOnManagerException( "Failed to enable addon..." );
		}
		return classLoader.getAddOn();
	}

	private boolean isLibrary(@NonNull final File file)
	{
		Properties properties = null;
		try
		{
			properties = getAddOnProperties( file );
		} catch ( final Exception e )
		{
			e.printStackTrace();
		}
		return properties == null;
	}

	/**
	 * Load an addon from a jar file
	 *
	 * @param file AddOn jar file
	 * @throws AddOnManagerException If anything goes wrong during the loading
	 */
	@Nullable @SuppressWarnings("WeakerAccess") public AddOnClassLoader loadAddon(@NonNull final File file)
			throws AddOnManagerException
	{
		final Properties properties;
		try
		{
			properties = getAddOnProperties( file );
		} catch ( final Exception e )
		{
			e.printStackTrace();
			return null; // Nullable
		}
		if ( properties == null )
		{
			return this.loadLibrary( file );
		} else
		{
			if ( !properties.containsKey( "main" ) )
			{
				new AddOnManagerException( "\"addon.properties\" for " + file.getName() + " has no \"main\" key" )
						.printStackTrace();
				return null; // Nullable
			}
			if ( !properties.containsKey( "name" ) )
			{
				new AddOnManagerException( "\"addon.properties\" for " + file.getName() + " has no \"name\" key" )
						.printStackTrace();
				return null; // Nullable
			}
			@NonNull final String addOnName = properties.get( "name" ).toString();
			if ( this.classLoaders.containsKey( addOnName ) )
			{
				throw new AddOnManagerException( "AddOn of name \"" + addOnName + "\" has already been loaded..." );
			}
			if ( CollectionUtil.containsIgnoreCase( this.disabledAddons, addOnName ) )
			{
				return null; // Nullable
			}
			@NonNull final String main = properties.get( "main" ).toString();
			if ( this.globalClassMap.containsKey( main ) )
			{
				throw new AddOnManagerException( "AddOn main class has already been loaded: " + main );
			}
			final AddOnClassLoader loader;
			try
			{
				loader = new AddOnClassLoader( this, file, main, addOnName );
			} catch ( final Exception e )
			{
				new AddOnManagerException( "Failed to load " + file.getName(), e ).printStackTrace();
				return null; // Nullable
			}
			this.classLoaders.put( addOnName, loader );
			return loader;
		}
	}

	@Nullable private AddOnClassLoader loadLibrary(@NonNull final File file)
	{
		final AddOnClassLoader loader;
		try
		{
			loader = new AddOnClassLoader( this, file, file.toPath().getFileName().toString() );
		} catch ( final Exception e )
		{
			new AddOnManagerException( "Failed to load " + file.getName(), e ).printStackTrace();
			return null; // Nullable
		}
		this.classLoaders.put( loader.getName(), loader );
		return loader;
	}

	/**
	 * Enable all addons. Does NOT load any addons.
	 */
	@SuppressWarnings("WeakerAccess") public void enableAddOns()
	{
		this.classLoaders.values().stream().filter( loader -> loader.getAddOn() != null )
				.map( AddOnClassLoader::getAddOn ).filter( addOn -> !addOn.isEnabled() ).forEach( AddOn::enable );
	}

	/**
	 * Disable all addons. Does NOT unload them.
	 */
	@SuppressWarnings("WeakerAccess") public void disableAddons()
	{
		this.classLoaders.values().stream().filter( loader -> loader.getAddOn() != null )
				.map( AddOnClassLoader::getAddOn ).filter( AddOn::isEnabled ).forEach( AddOn::disable );
	}

	void removeAll(final Map<String, Class<?>> clear)
	{
		clear.keySet().forEach( key -> this.globalClassMap.remove( key ) );
		for ( final Class<?> clazz : clear.values() )
		{
			if ( this.globalClassMap.containsValue( clazz ) )
			{
				throw new AddOnManagerException( "Clazz did not get removed..." );
			}
		}
	}

	@Override public void handleClose()
	{
		this.disableAddons();
	}

	private static final class AddOnManagerException extends RuntimeException
	{

		private AddOnManagerException(@NonNull final String error)
		{
			super( error );
		}

		private AddOnManagerException(@NonNull final String error, @NonNull final Throwable cause)
		{
			super( error, cause );
		}

	}
}
