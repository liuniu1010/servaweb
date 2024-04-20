package org.neo.servaweb.factory;

import org.neo.servaframe.interfaces.DBConnectionIFC;

import org.neo.servaweb.ifc.SuperAIIFC;
import org.neo.servaweb.ifc.OpenAIIFC;
import org.neo.servaweb.ifc.GoogleAIIFC;
import org.neo.servaweb.impl.OpenAIImpl;
import org.neo.servaweb.impl.GoogleAIImpl;
import org.neo.servaweb.util.CommonUtil;

public class AIFactory {
    public static SuperAIIFC getSuperAIInstance(DBConnectionIFC dbConnection) {
        String aiInstance = CommonUtil.getConfigValue(dbConnection, "AIInstance");
        if(aiInstance.equals("OpenAIImpl")) {
            return OpenAIImpl.getInstance(dbConnection);
        }
        else if(aiInstance.equals("GoogleAIImpl")) {
            return GoogleAIImpl.getInstance(dbConnection);
        }
        return null;
    }

    public static OpenAIIFC getOpenAIInstance(DBConnectionIFC dbConnection) {
        return OpenAIImpl.getInstance(dbConnection);
    }

    public static GoogleAIIFC getGoogleAIInstance(DBConnectionIFC dbConnection) {
        return GoogleAIImpl.getInstance(dbConnection);
    }
}
