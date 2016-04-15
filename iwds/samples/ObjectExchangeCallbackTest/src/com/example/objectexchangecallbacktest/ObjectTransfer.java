package com.example.objectexchangecallbacktest;

import java.io.IOException;
import java.io.OutputStream;

import com.ingenic.iwds.common.exception.SerializeException;
import com.ingenic.iwds.datatransactor.DataTransactor.ObjectExchangeCallback;
import com.ingenic.iwds.uniconnect.Connection;
import com.ingenic.iwds.utils.IwdsAssert;

public class ObjectTransfer implements ObjectExchangeCallback {

    @Override
    public void send(Connection connection, Object object)
            throws SerializeException, IOException {
        IwdsAssert.dieIf(this, connection == null, "connection == null");

        if (object == null)
            throw new SerializeException("object is null");

        OutputStream os = connection.getOutputStream();

        byte[] buffer = SafeParcelableUtils.encodeParcelable(
                (SafeParcelableClass) object, SafeParcelableClass.CREATOR);

        os.write(buffer);
    }

    @Override
    public Object recv(Connection connection) throws SerializeException,
            IOException {

        IwdsAssert.dieIf(this, connection == null, "connection == null");

        return SafeParcelableUtils.decodeParcelable(connection,
                SafeParcelableClass.CREATOR);
    }
}
