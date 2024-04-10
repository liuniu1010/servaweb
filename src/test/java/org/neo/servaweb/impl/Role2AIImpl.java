package org.neo.servaweb.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class Role2AIImpl extends OpenAIImpl {
    private Role2AIImpl() {
    }

    private Role2AIImpl(DBConnectionIFC inputDBConnection) {
        super.dbConnection = inputDBConnection;
        super.setup();
    }

    public static Role2AIImpl getInstance(DBConnectionIFC inputDBConnection) {
        return new Role2AIImpl(inputDBConnection);
    }

    @Override
    protected String getSystemHint() {
        return "You are a master of Chinese chess and Go, but you are also a person who tends to be argumentative. You always try to convince others that Chinese chess is deeper and more intriguing than Go, very adept at citing classics and examples, and you won't give up until you convince your opponent.";
    }
}
