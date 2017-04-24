/*
 * Copyright 2006 Jacek Olszak
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
package com.googlecode.egg.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.egg.core.EggContext;
import com.googlecode.egg.core.config.Configuration;
import com.googlecode.egg.exceptions.EggRuntimeException;
import com.googlecode.egg.exceptions.InvalidReturnHTMLException;
import com.googlecode.egg.util.ElementSearcher;

/**
 * 
 * @author Jacek Olszak
 * 
 */
public class Form extends UniqueSubmitableElement implements Submitable {
	private static final long serialVersionUID = -1876684648489014038L;

	private Input submitClicked;

	public Form() {
		this("temporaryName");
	}

	public Form(String name) {
		super(name);
		createHiddenFieldWithUid();
		createHiddenFieldWithFormUid();
		setMethod(FormMethod.GET);
		setAttribute("action", "?");
	}

	private void createHiddenFieldWithFormUid() {
		Input input = new Input(Configuration.FORM_UID_PARAMETER);
		input.setType(InputType.HIDDEN);
		input.setValue(getName());
		add(input);
	}

	private void createHiddenFieldWithUid() {
		Input input = new Input(Configuration.UID_PARAMETER);
		input.setType(InputType.HIDDEN);
		input.setValue(EggContext.getCurrentHtmlUid());
		add(input);
	}

	/**
	 * Returns form field with given name
	 * 
	 * @param name
	 *            If name is null IllegalArgumentException is thrown
	 * @return Returns FormField instance or null if field with this name does
	 *         not exist
	 */
	public FormField getField(String name) throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException("UID cannot be null");
		}
		List<FormField> fields = getFields();
		for (FormField field : fields) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns form fields with given name
	 * 
	 * @param name
	 *            If name is null IllegalArgumentException is thrown
	 * @return Returns FormField instance or null if field with this name does
	 *         not exist
	 */
	public List<FormField> getFields(String name)
			throws IllegalArgumentException {
		List<FormField> ret = new ArrayList<FormField>();

		if (name == null) {
			throw new IllegalArgumentException("UID cannot be null");
		}
		List<FormField> fields = getFields();
		for (FormField field : fields) {
			if (field.getName().equals(name)) {
				ret.add(field);
			}
		}
		return ret;
	}

	public List<FormField> getFields() {
		List<Element> elements = ElementSearcher.lookForElementsOfGivenType(
				FormField.class, this);
		List<FormField> fields = new ArrayList<FormField>();
		for (Element element : elements) {
			fields.add((FormField) element);
		}
		return fields;
	}

	@Override
	public void add(Element element) throws IllegalArgumentException {
		if (element instanceof FormField) {
			FormField field = (FormField) element;
			// if name is a "uid" then replace existing input
			if (field.getName().equals(Configuration.UID_PARAMETER)) {
				int i = super.getElements().indexOf(
						getField(Configuration.UID_PARAMETER));
				if (i != -1) {
					super.getElements().set(i, element);
					return;
				}
			}
			if (field.getName().equals(Configuration.FORM_UID_PARAMETER)) {
				int i = super.getElements().indexOf(
						getField(Configuration.FORM_UID_PARAMETER));
				if (i != -1) {
					super.getElements().set(i, element);
					return;
				}
			}
		}
		super.add(element);
	}

	public void setMethod(FormMethod method) {
		setAttribute("method", method.toString());
	}

	public FormMethod getMethod() {
		String attribute = getAttribute("method");
		return FormMethod.getFormMethodFromString(attribute);
	}

	@Override
	public String getTagName() {
		return "form";
	}

	public Input getSubmitClicked() {
		return submitClicked;
	}

	public void setSubmitClicked(Input submitOrImage) {
		if (submitOrImage == null) {
			throw new EggRuntimeException(
					"Submit or image which has been clicked cannot be null");
		}
		this.submitClicked = submitOrImage;
	}

	public Set<Input> getSubmitsAndImages() {
		// used for validation
		Map<String, Set<String>> submitValues = new HashMap<String, Set<String>>();

		Set<Input> submitsAndImages = new HashSet<Input>();
		List<Element> elements = ElementSearcher.lookForElementsOfGivenType(
				Input.class, this);
		for (Element element : elements) {
			Input input = (Input) element;
			if (input.getType().equals(InputType.SUBMIT)
					|| input.getType().equals(InputType.IMAGE)) {

				Set<String> values = submitValues.get(input.getName());
				if (values == null) {
					values = new HashSet<String>();
					submitValues.put(input.getName(), values);
				}
				String value = input.getValue();
				if (values.contains(value)) {
					throw new EggRuntimeException(
							"Two or more submit/image buttons found with the same name and value. Buttons with same names cannot have same values.");
				} else {
					values.add(value);
				}

				submitsAndImages.add(input);
			}
		}
		return submitsAndImages;
	}

	@Override
	public Html onSubmit() throws InvalidReturnHTMLException {
		if (getSubmitClicked() != null) {
			getSubmitClicked().onClick();
		}
		return super.onSubmit();
	}

}