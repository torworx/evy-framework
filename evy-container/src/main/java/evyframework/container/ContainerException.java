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

package evyframework.container;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class ContainerException extends RuntimeException {

	public static final long serialVersionUID = -1;

	protected List<InfoItem> infoItems = new ArrayList<InfoItem>();

	public static class InfoItem {
		public String errorContext = null;
		public String errorCode = null;
		public String errorText = null;

		public InfoItem(String contextCode, String errorCode, String errorText) {

			this.errorContext = contextCode;
			this.errorCode = errorCode;
			this.errorText = errorText;
		}
	}

	public ContainerException(String errorContext, String errorCode, String errorMessage) {
		super(errorMessage);
		addInfo(errorContext, errorCode, errorMessage);
	}

	public ContainerException(String errorContext, String errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, cause);
		addInfo(errorContext, errorCode, errorMessage);
	}

	public ContainerException addInfo(String errorContext, String errorCode, String errorText) {
		this.infoItems.add(new InfoItem(errorContext, errorCode, errorText));
		return this;
	}

	public List<InfoItem> getInfoItems() {
		return infoItems;
	}

	public String getCode() {
		StringBuilder builder = new StringBuilder();

		for (int i = this.infoItems.size() - 1; i >= 0; i--) {
			InfoItem info = this.infoItems.get(i);
			builder.append('[');
			builder.append(info.errorContext);
			builder.append(':');
			builder.append(info.errorCode);
			builder.append(']');
		}

		return builder.toString();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Error Code  : " + getCode());
		builder.append('\n');

		// append additional context information.
		for (int i = this.infoItems.size() - 1; i >= 0; i--) {
			InfoItem info = this.infoItems.get(i);
			builder.append("Context Info: ");
			builder.append('[');
			builder.append(info.errorContext);
			builder.append(':');
			builder.append(info.errorCode);
			builder.append(']');
			builder.append(" : ");
			builder.append(info.errorText);
			if (i > 0)
				builder.append('\n');
		}

		// append root causes and text from this exception first.
		if (getMessage() != null) {
			builder.append('\n');
			if (getCause() == null) {
				builder.append(getMessage());
			} else if (!getMessage().equals(getCause().toString())) {
				builder.append(getMessage());
			}
		}
		appendException(builder, getCause());

		return builder.toString();
	}

	private void appendException(StringBuilder builder, Throwable throwable) {
		if (throwable == null)
			return;
		appendException(builder, throwable.getCause());
		builder.append(throwable.toString());
		builder.append('\n');
	}
}
