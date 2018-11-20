package xyz.kvantum.server.api.response;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Custom stream implementation used to write HTTP response bodies
 */
@RequiredArgsConstructor @SuppressWarnings({ "WeakerAccess", "unused" }) public class ResponseStream
{

	private static final byte[] EMPTY_RESPONSE = new byte[ 0 ];
	private static final long MAX_WAIT = 500L; // Wait time of 500ms

	@Getter private boolean finished = false;

	@Getter
	private int read = 0;
	private int offer = -1;
	private Consumer<Integer> offerAction, finalizedAction;

	private byte[] buffer;

	/**
	 * Get the last pushed offer (available data length)
	 *
	 * @return Length of available data
	 */
	public int getOffer()
	{
		return this.offer;
	}

	/**
	 * Mark the stream as finished, which means it cannot be written to anymore
	 */
	public void finish()
	{
		if ( this.finished )
		{
			throw new IllegalStateException( "Cannot finish the stream when it's already finished" );
		}
		this.finished = true;
	}

	/**
	 * Offer a given amount of bytes to the receiver
	 *
	 * @param amount Length of available data. Must be bigger than 0.
	 * @param acceptedAction Action to run when the client has accepted the offer
	 * @param finalizedAction Action to run when the client has read the offered data
	 */
	public void offer(final int amount, @NonNull final Consumer<Integer> acceptedAction, final Consumer<Integer> finalizedAction)
	{
		if ( amount <= 0 )
		{
			throw new IllegalArgumentException( "Amount must be bigger than 0" );
		}
		this.checkState();
		this.offer = amount;
		this.offerAction = acceptedAction;
		if ( finalizedAction != null )
		{
			this.finalizedAction = finalizedAction;
		}
	}

	/**
	 * Push data to the stream
	 *
	 * @param bytes Data. Length should not be larger than the offer.
	 */
	public void push(final byte[] bytes)
	{
		if ( bytes.length > this.offer )
		{
			throw new IllegalArgumentException( "Pushed data size cannot be larger than offer" );
		}
		this.buffer = bytes;
	}

	/**
	 * Read data from the stream, with a specified maximal length. Check return array size for actual length of read
	 * data.
	 *
	 * @param amount Amount of data that can be read.
	 * @return Read data.
	 */
	public byte[] read(int amount)
	{
		if ( amount <= 0 )
		{
			throw new IllegalArgumentException( "Amount must be bigger than 0" );
		}
		if ( this.finished )
		{
			return EMPTY_RESPONSE;
		}

		// Make sure that we don't try to read more than the offered data
		amount = Math.min( amount, this.offer );

		if ( this.offerAction != null )
		{
			this.offerAction.accept( amount );
		}
		// This makes sure that the offer has written to the stream, or else it'll block until MAX_TIME
		final long startTime = System.currentTimeMillis();
		while ( this.buffer == null )
		{
			if ( System.currentTimeMillis() - startTime > MAX_WAIT )
			{
				throw new IllegalStateException( "Time out" );
			}
		}

		read += this.buffer.length;

		this.offer = 0;
		this.offerAction = null;

		if ( this.finalizedAction != null )
		{
			this.finalizedAction.accept( read );
		}

		return this.buffer;
	}

	private void checkState()
	{
		if ( this.finished )
		{
			throw new IllegalStateException( "Cannot perform I/O actions on a closed response stream" );
		}
	}

}
