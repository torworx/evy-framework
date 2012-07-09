/*
    Copyright 2007-2010 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



package evyframework.container.script;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class ParserError implements Comparable<Object> {

    int lineNo = 0;
    int charNo = 0;

    String errorText = null;

    public ParserError(int lineNo, int charNo, String errorText) {
        this.lineNo = lineNo;
        this.charNo = charNo;
        this.errorText = errorText;
    }

    public int getLineNo() {
        return lineNo;
    }

    public int getCharNo() {
        return charNo;
    }

    public String getErrorText() {
        return errorText;
    }

    public String toString() {
        return "(" + lineNo + ":" + charNo + ") " + errorText;
    }

    public int compareTo(Object o) {
        if(o == null ) throw new NullPointerException("Cannot compare a ParserError instance to null!");
        if(! (o instanceof ParserError)){
            throw new IllegalArgumentException("Cannot compare ParserError instances to " + o.getClass().getName());
        }

        ParserError other = (ParserError) o;
        if(this.lineNo > other.getLineNo()) return 1;
        if(this.lineNo < other.getLineNo()) return -1;
        if(this.charNo > other.getCharNo()) return 1;
        if(this.charNo < other.getCharNo()) return -1;

        return 0;
    }


}
