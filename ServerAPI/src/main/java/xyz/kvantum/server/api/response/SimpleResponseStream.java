package xyz.kvantum.server.api.response;

import lombok.Getter;
import lombok.NonNull;

/**
 * Response stream with a single input write
 */
public final class SimpleResponseStream extends ResponseStream implements KnownLengthStream
{

	@Getter
	private byte[] bytes;
	private int read = 0;

	public SimpleResponseStream(final byte[] bytes)
	{
		this.bytes = bytes;
	}

	@Override public byte[] read(int amount)
	{
		int toRead = Math.min( this.getOffer(), amount );
		final byte[] bytes = new byte[ toRead ];
		System.arraycopy( this.bytes, read, bytes, 0, toRead );
		this.read += bytes.length;
		if ( this.bytes.length <= this.read )
		{
			this.finish();
		}
		return bytes;
	}

	@Override public int getOffer()
	{
		return this.bytes.length - read;
	}

	@Override public int getLength()
	{
		return this.getBytes().length;
	}

	@Override public byte[] getAll()
	{
		return this.getBytes();
	}

	@Override public void replaceBytes(@NonNull final byte[] bytes)
	{
		this.bytes = bytes;
		this.read = 0;
	}

}
