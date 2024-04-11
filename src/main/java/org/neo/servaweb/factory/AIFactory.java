package org.neo.servaweb.factory;

import org.neo.servaframe.interfaces.DBConnectionIFC;

import org.neo.servaweb.ifc.SuperAIIFC;
import org.neo.servaweb.ifc.OpenAIIFC;
import org.neo.servaweb.ifc.GoogleAIIFC;
import org.neo.servaweb.impl.OpenAIImpl;
import org.neo.servaweb.impl.GoogleAIImpl;

public class AIFactory {
    public static SuperAIIFC getSuperAIInstance(DBConnectionIFC dbConnection) {
        // return OpenAIImpl.getInstance(dbConnection);
        return GoogleAIImpl.getInstance(dbConnection);
    }

    public static OpenAIIFC getOpenAIInstance(DBConnectionIFC dbConnection) {
        return OpenAIImpl.getInstance(dbConnection);
    }

    public static GoogleAIIFC getGoogleAIInstance(DBConnectionIFC dbConnection) {
        return GoogleAIImpl.getInstance(dbConnection);
    }
}
