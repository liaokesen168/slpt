package com.example.objectexchangecallbacktest;

import java.io.IOException;
import java.io.InputStream;
import com.ingenic.iwds.os.SafeParcel;
import com.ingenic.iwds.os.SafeParcelable;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.utils.IwdsAssert;

public class SafeParcelableUtils {
    private final static String TAG = "SafeParcelableUtils";

    private static byte[] expand(int pos, int length, byte[] buffer) {
        byte[] newBuffer = null;
        int newSize = pos + length;

        if (buffer != null && newSize <= buffer.length) {
            return buffer;
        }

        newBuffer = new byte[newSize];
        if (buffer != null)
            System.arraycopy(buffer, 0, newBuffer, 0, pos);

        return newBuffer;
    }

    private static int writeInt(byte[] buffer, int offset, int value) {
        buffer[offset++] = (byte) ((value & 0xff000000) >> 24);
        buffer[offset++] = (byte) ((value & 0x00ff0000) >> 16);
        buffer[offset++] = (byte) ((value & 0x0000ff00) >> 8);
        buffer[offset++] = (byte) ((value & 0x000000ff) >> 0);
        return offset;
    }

    public static <T extends SafeParcelable> byte[] encodeParcelable(
            SafeParcelable parcelable, SafeParcelable.Creator<T> creator) {
        byte[] buffer = null;
        int pos = 0;

        SafeParcel parcel = SafeParcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] rawBytes = parcel.marshall();
        parcel.recycle();

        buffer = expand(pos, 4, buffer);
        pos = writeInt(buffer, pos, rawBytes.length);

        buffer = expand(pos, rawBytes.length, buffer);
        System.arraycopy(rawBytes, 0, buffer, pos, rawBytes.length);
        pos += rawBytes.length;

        return buffer;
    }

    private static int readInt(byte[] bytes) {
        int value = 0;
        value = (int) (((bytes[0] << 24) & 0xff000000)
                | ((bytes[1] << 16) & 0x00ff0000)
                | ((bytes[2] << 8) & 0x0000ff00) | ((bytes[3] << 0) & 0x000000ff));
        return value;
    }

    private static int decodeInt(Connection connection) throws IOException {
        byte[] buffer = new byte[4];
        int pos = 0;
        int maxSize = buffer.length;
        InputStream is = connection.getInputStream();

        while (maxSize > 0) {
            int readBytes = is.read(buffer, pos, maxSize);
            pos += readBytes;
            maxSize -= readBytes;
        }

        return readInt(buffer);
    }

    private static <T extends SafeParcelable> Object readParcelable(byte[] buffer,
            SafeParcelable.Creator<T> creator) {

        IwdsAssert.dieIf(TAG, creator == null, "creator == null");

        SafeParcel p = SafeParcel.obtain();
        p.unmarshall(buffer, 0, buffer.length);
        p.setDataPosition(0);
        T obj = creator.createFromParcel(p);
        p.recycle();

        return obj;
    }

    public static <T extends SafeParcelable> Object decodeParcelable(
            Connection connection, SafeParcelable.Creator<T> creator)
            throws IOException {

        int length = decodeInt(connection);
        byte[] buffer = new byte[length];

        int pos = 0;
        InputStream is = connection.getInputStream();

        while (length > 0) {
            int readBytes = is.read(buffer, pos, length);
            pos += readBytes;
            length -= readBytes;
        }

        return readParcelable(buffer, creator);
    }
}
