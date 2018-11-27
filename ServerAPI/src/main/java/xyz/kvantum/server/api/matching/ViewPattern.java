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
package xyz.kvantum.server.api.matching;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import xyz.kvantum.server.api.config.CoreConfig;
import xyz.kvantum.server.api.core.ServerImplementation;
import xyz.kvantum.server.api.logging.Logger;
import xyz.kvantum.server.api.util.Assert;

/**
 * <p> ViewPattern will be referred to as filter </p> <p> Filters are used to determine which view gets to serve the
 * incoming requests. Each filter is made up of different parts, and there are four types of parts: <ul> <li>Separator -
 * / - Used like a path separator</li> <li>Static - Example: user - A static string</li> <li>Required Variable -
 * Example: &lt;username&gt;</li> <li>Optional Variable - Example: [page]</li> </ul> </p> <p> <b>Examples</b> </p> <p>
 * <b>user/&lt;username&gt;</b> - Serves user/Citymonstret, but not user/ or user/Citymonstret/other </p> <p>
 * <b>news/[page]</b> - Serves news, news/1, news/foo, but not news/foo/bar </p> <p>
 * <b>user/&lt;username&gt;/posts/[page]</b> - Serves user/Citymonstret/posts and user/Citymonstrst/posts/10 </p>
 */
@SuppressWarnings("unused") public class ViewPattern
{
	private static final Timer TIMER_MATCH = ServerImplementation.getImplementation().getMetrics().getRegistry()
			.timer( MetricRegistry.name( ViewPattern.class, "match" ) );

	private static final Pattern PATTERN_VARIABLE_REQUIRED = Pattern.compile( "<([a-zA-Z0-9]*)>" );
	private static final Pattern PATTERN_VARIABLE_OPTIONAL = Pattern.compile( "\\[([a-zA-Z0-9]*)(=([a-zA-Z0-9]*))?]" );
	private static final Pattern PATTERN_VARIABLE_STATIC = Pattern.compile( "([a-zA-Z0-9]*)" );
	private static final String  PATTERN_PATTERN = "[\\/]*%s[\\/]*";

	private final List<Part> parts = new ArrayList<>();
	private final String raw;
	private final Pattern pattern;
	private final Map<String, Variable> variableMap = new HashMap<>();

	/**
	 * Generate a list of parts from the provided string
	 *
	 * @param in String to compile
	 */
	public ViewPattern(final String in)
	{
		Assert.notNull( in );

		this.raw = in;

		final boolean empty = in.isEmpty();

		String string = raw.toLowerCase( Locale.ENGLISH );
		if ( !empty && string.charAt( 0 ) == '/' )
		{
			string = string.substring( 1 );
		}
		if ( !empty && string.charAt( string.length() - 1 ) == '/' )
		{
			string = string.substring( 0, string.length() - 1 );
		}

		final List<Integer> delimiterTypes = new ArrayList<>();
		for ( final Character c : string.toCharArray() )
		{
			if ( c.equals( '.' ) )
			{
				delimiterTypes.add( 0 );
			} else if ( c.equals( '/' ) )
			{
				delimiterTypes.add( 1 );
			}
		}
		final Iterator<Integer> delimiterIterator = delimiterTypes.iterator();

		final StringTokenizer stringTokenizer = new StringTokenizer( string, "\\/." );
		while ( stringTokenizer.hasMoreTokens() )
		{
			final String token = stringTokenizer.nextToken();
			if ( token.isEmpty() )
			{
				continue;
			}
			Matcher matcher;
			if ( ( matcher = PATTERN_VARIABLE_REQUIRED.matcher( token ) ).matches() )
			{
				this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_REQUIRED ) );
			} else if ( ( matcher = PATTERN_VARIABLE_OPTIONAL.matcher( token ) ).matches() )
			{
				if ( matcher.group( 3 ) == null || matcher.group( 3 ).isEmpty() )
				{
					this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_OPTIONAL ) );
				} else
				{
					this.parts.add( new Variable( matcher.group( 1 ), Variable.TYPE_OPTIONAL, matcher.group( 3 ) ) );
				}
			} else if ( ( matcher = PATTERN_VARIABLE_STATIC.matcher( token ) ).matches() )
			{
				this.parts.add( new Static( matcher.group( 1 ) ) );
			}
			if ( stringTokenizer.hasMoreTokens() && delimiterIterator.hasNext() )
			{
				final int delimiterType = delimiterIterator.next();
				if ( delimiterType == 0 )
				{
					this.parts.add( new Dot() );
				} else if ( delimiterType == 1 )
				{
					this.parts.add( new Split() );
				}
			}
		}

		final StringBuilder pattern = new StringBuilder();
		for ( int index = 0; index < parts.size(); index++ )
		{
			final Part part = parts.get( index );
			final Part nextPart;
			if ( index + 1 == parts.size() )
			{
				nextPart = null;
			} else
			{
				nextPart = parts.get( index + 1 );
			}
			final boolean optional = nextPart != null && nextPart instanceof Variable &&
					( ( Variable ) nextPart ).getType() == Variable.TYPE_OPTIONAL;
			pattern.append( part.toRegexBlock( optional ) );
			if ( part instanceof Variable )
			{
				final Variable variable = (Variable) part;
				variableMap.put( variable.name, variable );
			}
		}
		this.pattern = Pattern.compile( String.format( PATTERN_PATTERN, pattern.toString() ) );

		if ( CoreConfig.debug )
		{
			Logger.debug( "Transformed pattern into regex: {0} -> {1}", raw, this.pattern.pattern() );
		}

	}

	/**
	 * Test if a string matches the pattern
	 *
	 * @param in String to test for
	 * @return A map containing the variables extracted from the string. If there was no match, the map will be null
	 */
	@Nullable public Map<String, String> matches(final String in)
	{
		Assert.notNull( in );

		Timer.Context timer = TIMER_MATCH.time();

		String url;
		int index;
		if ( ( index = in.indexOf( '?' )) != -1 )
		{
			url = in.substring( 0, index );
		} else
		{
			url = in;
		}

		// Replace all but last occurrence of . with §
		final int count = StringUtils.countMatches( url, "." );

		if ( count >= 2 )
		{
			final int lastIndex = url.lastIndexOf( "." );
			final String sub = url.substring( 0, lastIndex );
			url = sub.replaceAll( "\\.", "§" ) + url.substring( lastIndex );
		}

		if ( parts.isEmpty() )
		{
			if ( url.isEmpty() )
			{
				timer.close();
				return new HashMap<>();
			} else
			{
				timer.close();
				return null; // Nullable
			}
		}

		final Matcher matcher = pattern.matcher( url );
		if ( !matcher.matches() )
		{
			timer.close();
			return null; // Nullable
		}

		final Map<String, String> variables = new HashMap<>();
		for ( final Map.Entry<String, Variable> entry : this.variableMap.entrySet() )
		{
			String value = matcher.group( entry.getKey() );
			if ( value == null )
			{
				value = "";
			}
			if ( value.isEmpty() ) // The value is missing
			{
				if ( entry.getValue().getType() == Variable.TYPE_REQUIRED ) // Value is missing
				{
					timer.close();
					return null; // Nullable
				}
				if ( entry.getValue().hasDefaultValue() )
				{
					value = entry.getValue().getDefaultValue();
				} else
				{
					continue;
				}
			}
			variables.put( entry.getKey(), value );
		}

		timer.close();
		return variables;
	}

	@Override public String toString()
	{
		return this.raw;
	}

	private abstract static class Part
	{

		@Override public abstract String toString();

		public abstract String toRegexBlock(boolean nextOptional);

	}

	private static class Static extends Part
	{

		private final String string;

		private Static(final String string)
		{
			this.string = string;
		}

		@Override public String toString()
		{
			return string;
		}

		@Override public String toRegexBlock(final boolean nextOptional)
		{
			return String.format( "(%s)", this.string );
		}
	}

	private static final class Dot extends Part
	{

		@Override public String toString()
		{
			return ".";
		}

		@Override public String toRegexBlock(final boolean nextOptional)
		{
			if ( nextOptional )
			{
				return "[.§]*";
			} else
			{
				return "[§.]{1}";
			}
		}
	}

	private static final class Split extends Part
	{

		@Override public String toString()
		{
			return "/";
		}

		@Override public String toRegexBlock(final boolean nextOptional)
		{
			if ( nextOptional )
			{
				return "[\\/]*";
			} else
			{
				return "[\\/]{1}";
			}
		}
	}

	@AllArgsConstructor private static class Variable extends Part
	{

		private static int TYPE_REQUIRED = 0, TYPE_OPTIONAL = 1;
		@Getter private final String name;
		@Getter private final int type;
		@Getter private final String defaultValue;

		Variable(String name, int type)
		{
			this( name, type, null );
		}

		boolean hasDefaultValue()
		{
			return this.getDefaultValue() != null;
		}

		@Override public String toString()
		{
			return this.name + ( type == TYPE_REQUIRED ? "" :  "?" );
		}

		@Override public String toRegexBlock(final boolean nextOptional)
		{
			return String.format( "(?<%s>[A-Za-z0-9_-]%s)", this.name, type == TYPE_REQUIRED ? "+" : "*" );
		}
	}

}
