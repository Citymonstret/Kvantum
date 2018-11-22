package xyz.kvantum.server.api.response;

import lombok.NonNull;

public interface KnownLengthStream
{

	int getLength();

	byte[] getAll();

	void replaceBytes(@NonNull final byte[] bytes);

}
