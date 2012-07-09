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


import java.io.*;
import java.util.Stack;

import evyframework.container.factory.support.ParserException;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class ParserInput {

	protected ScriptTokenizer scriptTokenizer = null;
	protected Stack<ParserMark> marks = new Stack<ParserMark>();

	public ParserInput(Reader reader) {
		this.scriptTokenizer = new ScriptTokenizer(new ScriptTokenizerInputBuffer(reader));
	}

	public ParserInput(String input) {
		this.scriptTokenizer = new ScriptTokenizer(new ScriptTokenizerInputBuffer(new StringReader(input)));
	}

	public void factoryStart() {
		this.scriptTokenizer.factoryStart();
	}

	public boolean isNextElseBacktrack(Token expectedToken) {
		mark();
		Token nextToken = nextToken();
		boolean matches = expectedToken.equals(nextToken);
		if (matches)
			clearMark();
		else
			backtrack();
		return matches;
	}

	public void assertNextToken(Token token) {
		Token nextToken = nextToken();
		if (nextToken == null || !nextToken.equals(token)) {
			throw new ParserException("ParserInput", "ASSERT_NEXT_TOKEN", "Error (" + this.scriptTokenizer.getLineNo()
					+ ", " + this.scriptTokenizer.getCharNo() + "): Expected token " + token + " but found "
					+ nextToken);
		}
	}

	public Token lookAhead() {
		mark();
		Token nextToken = nextToken();
		backtrack();
		return nextToken;
	}

	public Token markAndNextToken() {
		mark();
		return nextToken();
	}

	public Token nextToken() {
		Token nextToken = this.scriptTokenizer.nextToken();
		if (nextToken == null)
			return null;
		return nextToken;
	}

	public void assertNoMarks() {
		if (this.marks.size() > 0) {
			throw new ParserException("ParserInput", "ASSERT_NO_MARKS",
					"There should have been no marks at the current parsing point");
		}
	}

	public int mark() {
		ParserMark mark = this.scriptTokenizer.mark();
		this.marks.push(mark);
		return this.marks.size();
	}

	public int backtrack() {
		ParserMark mark = this.marks.pop();
		this.scriptTokenizer.backtrackTo(mark);
		return this.marks.size() + 1;
	}

	public void clearMark() {
		this.marks.pop();
	}

	public boolean hasMark() {
		return this.marks.size() > 0;
	}

}