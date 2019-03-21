/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.stream.app.test.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * @author Gary Russell
 *
 */
public class HamcrestMatchers {

	public static Matcher<ObjectError> fieldErrorWith(String argument0, String defaultMessage) {
		return new FieldValidationMatcher(argument0, defaultMessage);
	}

	public static Matcher<ObjectError> fieldErrorWithNonEmptyArgument(String argument0) {
		return new FieldValidationMatcher(argument0, "may not be empty");
	}

	private static final class FieldValidationMatcher extends DiagnosingMatcher<ObjectError> {

		private final String argument0;

		private final String defaultMessage;


		public FieldValidationMatcher(String argument0, String defaultMessage) {
			this.argument0 = argument0;
			this.defaultMessage = defaultMessage;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("a FieldError with a single argument value '").appendText(this.argument0)
						.appendText("' and default message '").appendText(this.defaultMessage).appendText("'");
		}

		@Override
		protected boolean matches(Object item, Description mismatchDescription) {
			if (item instanceof FieldError) {
				FieldError fieldError = (FieldError) item;
				boolean matches = fieldError.getArguments() != null
						&& fieldError.getArguments().length == 1
						&& fieldError.getArguments()[0].toString().contains(this.argument0)
						&& fieldError.getDefaultMessage().equals(this.defaultMessage);
				if (!matches) {
					mismatchDescription.appendText(item.toString());
				}
				return matches;
			}
			else {
				mismatchDescription.appendText(item.toString());
				return false;
			}
		}

	}

}
