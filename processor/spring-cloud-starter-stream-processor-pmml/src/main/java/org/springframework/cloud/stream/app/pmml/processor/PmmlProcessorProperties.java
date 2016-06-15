/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.pmml.processor;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.expression.Expression;

/**
 * Holds configuration properties for the Pmml Processor module.
 *
 * @author Eric Bottard
 * @author Gary Russell
 */
@ConfigurationProperties("pmml")
public class PmmlProcessorProperties {

	/**
	 * The location of the PMML model file.
	 */
	private Resource modelLocation;

	/**
	 * If the model file contains multiple models, the name of the one to use.
	 */
	private String modelName;

	/**
	 * If the model file contains multiple models, the name of the one to use, as a SpEL expression.
	 */
	private Expression modelNameExpression;

	/**
	 * How to compute model active fields from input message properties as modelField->SpEL.
	 */
	private Map<String, Expression> inputs = new HashMap<>();

	/**
	 * How to emit evaluation results in the output message as msgProperty->SpEL.
	 */
	private Map<String, Expression> outputs = new HashMap<>();

	@NotNull
	public Resource getModelLocation() {
		return modelLocation;
	}

	public void setModelLocation(Resource modelLocation) {
		this.modelLocation = modelLocation;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public Expression getModelNameExpression() {
		return modelNameExpression;
	}

	public void setModelNameExpression(Expression modelNameExpression) {
		this.modelNameExpression = modelNameExpression;
	}

	public Map<String, Expression> getInputs() {
		return inputs;
	}

	public void setInputs(Map<String, Expression> inputs) {
		this.inputs = inputs;
	}

	public Map<String, Expression> getOutputs() {
		return outputs;
	}

	public void setOutputs(Map<String, Expression> outputs) {
		this.outputs = outputs;
	}

	@AssertTrue(message = "At most one of 'modelName' and 'modelNameExpression' is allowed")
	public boolean isAtMostOneModelName() {
		return modelName == null || modelNameExpression == null;
	}

}
