package org.neo.servaweb.impl;

import org.neo.servaframe.interfaces.DBConnectionIFC;
import org.neo.servaweb.model.AIModel;
import org.neo.servaweb.util.CommonUtil;

public class Role1AIImpl extends OpenAIImpl {
    @Override
    protected String getSystemHint() {
        return "You are a master of Go and Chinese chess, but you are also a person who tends to be argumentative. You always try to convince others that Go is more complex and interesting than Chinese chess, very good at citing classics and examples, and you won't give up until you convince your opponent.";
    }
}
