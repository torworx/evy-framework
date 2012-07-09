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

import evyframework.container.factory.support.ParserException;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class ScriptTokenizer {

	protected int nextChar = ' ';
	protected int lineNo = 1;
	protected int charNo = 1;
	protected int endOfStream = -1;

	protected ScriptTokenizerInputBuffer inputBuffer = null;

	public ScriptTokenizer(ScriptTokenizerInputBuffer inputBuffer) {
		this.inputBuffer = inputBuffer;
	}

	public int getLineNo() {
		return lineNo;
	}

	public int getCharNo() {
		return charNo;
	}

	public void factoryStart() {
		this.inputBuffer.factoryStart();
	}

	public ParserMark mark() {
		ParserMark mark = this.inputBuffer.mark();
		mark.lineNo = this.lineNo;
		mark.charNo = this.charNo;

		return mark;
	}

	public void backtrackTo(ParserMark mark) {
		this.lineNo = mark.lineNo;
		this.charNo = mark.charNo;
		this.inputBuffer.backtrackTo(mark);
	}

	public Token nextToken(char... allowedInToken) {
		Token token = getToken();
		if (this.inputBuffer.isEndOfInputReached())
			return null;

		while (true) {
			nextChar = read();

			// if the character is allowed in this token, add it to the token.
			// else do a normal parsing of the character.
			if (allowedInToken != null && allowedInToken.length > 0) {
				for (char allowedChar : allowedInToken) {
					if (allowedChar == nextChar) {
						append(token, (char) nextChar);
					}
				}
			} else {
				switch (nextChar) {

				case '\n':
					;
				case '\r':
					if (token.length() > 0) {
						unread(this.nextChar);
						return token;
					}
					readLineBreak();
					break;

				case '\t':
					;
				case ' ':
					if (token.length() > 0) {
						unread(this.nextChar);
						return token;
					}
					break;

				case '"':
					readQuote(token, '\"');
					break;

				case '\'':
					readQuote(token, '\'');
					break;

				case '/':
					int ahead = read();
					if (ahead == '*') {
						if (token.length() > 0) {
							unread(ahead);
							unread(this.nextChar);
							return token;
						}
						readBlockComment();
					} else if (ahead == '/'){
						readLineComment();
					} else {
						unread(ahead);
						append(token, (char) this.nextChar);
					}
					break;

				// significant delimiters
				case '{':
				case '}':
				case '(':
				case ')':
				case '[':
				case ']':
				case '<':
				case '>':
				case ',':
				case ';':
				case ':':
				case '.':
				case '=':
					if (token.length() > 0) {
						unread(this.nextChar);
						return token;
					}
					append(token, (char) this.nextChar);
					return token;

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if (token.length() == 0) {
						readNumberOrFactoryMode(token);
						return token;
					}

				default:
					if (nextChar != endOfStream) {
						append(token, (char) nextChar);
					} else {
						this.charNo--;
						if (token.length() > 0)
							return token;
						return null;
					}
				}
			}
		}
	}

	private void readNumberOrFactoryMode(Token token) {
		append(token, (char) this.nextChar);

		boolean hasDecimalSeparator = false;

		while (true) {
			int next = read();

			switch (next) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				append(token, (char) next);
				break;

			case '.':
				if (!hasDecimalSeparator) {
					hasDecimalSeparator = true;
					append(token, (char) next);
					break;
				} else {
					unread(next);
					return;
				}
			case 'F':
			case 'T':
				if (token.length() == 1 && token.charAt(0) == '1') { // if is thread singleton or flyweight.
					append(token, (char) next);
					return;
				}

			default:
				unread(next);
				return;

			}
		}
	}

	private Token getToken() {
		Token token = inputBuffer.token();
		return token;
	}

	private void unread(int charToUnread) {
		if (charToUnread != -1) {
			inputBuffer.unread((char) charToUnread);
			this.charNo--;
		}
	}

	private int read() {
		this.charNo++;
		return inputBuffer.read();
	}

	private void readLineBreak() {
		this.lineNo++;
		this.charNo = 1;
		int ahead = read();
		if (nextChar == '\n' && ahead == '\r') {
			// ignore, was double char line break
		} else if (nextChar == '\r' && ahead == '\n') {
			// ignore, was double char line break
		} else {
			unread(ahead);
		}
	}

	/**
	 * todo allow escape characters in quotes, like \" \t \n \r etc. todo create smart multi-line quote trim function -
	 * probably using special escape characters like \-
	 */
	private void readQuote(Token token, char quoteDelimiter) {
		append(token, quoteDelimiter);
		int next = read();
		while (next != endOfStream && next != quoteDelimiter) {
			append(token, (char) next);
			next = read();
		}
		if (next == endOfStream) {
			throw new ParserException("ScriptTokenizer", "NO_MATCHING_END_QUOTE",
					"No matching end quote found in input. End of stream reached");
		}
		append(token, quoteDelimiter);
	}
	
	private void readLineComment() {
		nextChar = read();
		while (nextChar != endOfStream && nextChar != '\r' && nextChar != '\n') {
			nextChar = read();
		}
		if (nextChar != endOfStream) {
			readLineBreak();
		}
	}

	private void readBlockComment() {
		nextChar = read();
		while (nextChar != endOfStream) {
			if (nextChar == '*') {
				nextChar = read();
				if (nextChar == '/' || nextChar == endOfStream)
					break;
			}
			nextChar = read();
		}
		if (nextChar == endOfStream)
			throw new ParserException("ScriptTokenizer", "NO_MATCHING_END_OF_COMMENT",
					"No matching end of comment (*/) found. End of stream reached");
	}

	private void append(Token token, char next) {
		if (token.length() == 0) {
			token.setFrom(this.inputBuffer.index - 1);
			token.setCharNoBefore(this.charNo - 1);
			token.setLineNo(this.lineNo);
		}
		token.append(next);
	}

}