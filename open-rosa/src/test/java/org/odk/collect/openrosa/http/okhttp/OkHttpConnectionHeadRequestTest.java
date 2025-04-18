package org.odk.collect.openrosa.http.okhttp;

import android.webkit.MimeTypeMap;

import org.odk.collect.openrosa.http.CollectThenSystemContentTypeMapper;
import org.odk.collect.openrosa.http.OpenRosaHeadRequestTest;
import org.odk.collect.openrosa.http.OpenRosaHttpInterface;

public class OkHttpConnectionHeadRequestTest extends OpenRosaHeadRequestTest {

    @Override
    protected OpenRosaHttpInterface buildSubject() {
        return new OkHttpConnection(
                null,
                new CollectThenSystemContentTypeMapper(MimeTypeMap.getSingleton()),
                USER_AGENT
        );
    }
}
