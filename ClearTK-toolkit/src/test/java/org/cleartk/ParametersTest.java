/** 
 * Copyright (c) 2009, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.util.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.util.io.Files;

/**
 * <br>
 * Copyright (c) 2009, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Philip Ogren
 */
public class ParametersTest {

	@Test
	public void testParameterDefinitions() throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException {
		List<String> badParameters = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src/main/java", new String[] { ".java" });

		for (File file : files) {
			String className = file.getPath();
			className = className.substring(14);
			className = className.substring(0, className.length() - 5);
			className = className.replace(File.separatorChar, '.');
			Class<?> cls = Class.forName(className);
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if (fieldName.indexOf("PARAM") != -1 && (field.getModifiers() & Modifier.PUBLIC) > 0) {
					String expectedValue = className + "." + field.getName();
					String actualValue = (String) field.get(cls);
					if (!expectedValue.equals(actualValue)) {
						badParameters.add("'" + actualValue + "' should be '" + expectedValue + "'");
					}
				}
			}
		}

		if (badParameters.size() > 0) {
			String message = String.format("%d descriptor parameters with bad names. ", badParameters.size());
			System.err.println(message);
			for (String badParameter : badParameters) {
				System.err.println(badParameter);
			}
			Assert.fail(message);
		}

	}

//	@Test
	public void testParameterComments() throws ClassNotFoundException, IllegalArgumentException,
			IllegalAccessException, IOException {
		List<String> badComments = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src", new String[] { ".java" });

		for (File file : files) {
			String fileContents = FileUtils.file2String(file);
			String className = file.getPath();
			className = className.substring(4);
			className = className.substring(0, className.length() - 5);
			className = className.replace(File.separatorChar, '.');

			Class<?> cls = Class.forName(className);
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				String fieldName = field.getName();
				if (fieldName.indexOf("PARAM") != -1 && (field.getModifiers() & Modifier.PUBLIC) > 0) {
					String actualValue = "\""+(String) field.get(cls)+"\"";
					int index = fileContents.indexOf(actualValue);
					if(index == -1) {
						throw new RuntimeException("field name definition is not found by searching the contents of .java file.  field = "+actualValue);
					}
					index = fileContents.indexOf(actualValue, index+actualValue.length());
					if(index == -1) {
						badComments.add(actualValue+" in "+file.getPath()+" is not commented adequately.   The value of the parameter should be provided in quotes along with the type (String, Boolean, Integer, Float), whether it is multi-valued, and whether it is mandatory along with a description of the parameter.");
					}
				}
			}
		}

		if (badComments.size() > 0) {
			String message = String.format("%d descriptor parameters with insufficient comments. ", badComments.size());
			System.err.println(message);
			for (String badParameter : badComments) {
				System.err.println(badParameter);
			}
			Assert.fail(message);
		}

	}

	
	@Test
	public void testDescriptorParameters() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			JDOMException, IOException {
		List<String> badParameters = new ArrayList<String>();
		List<String> badParameterSettings = new ArrayList<String>();
		Iterable<File> files = Files.getFiles("src", new String[] { ".xml" });

		Namespace ns = Namespace.getNamespace("http://uima.apache.org/resourceSpecifier");
		for (File file : files) {
			
			String path = file.getPath();
			if(path.equals("src/org/cleartk/syntax/treebank/opennlp/ChunkingParser.xml".replace('/', File.separatorChar))) {
				continue;
			}
			
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(file);
			Element root = doc.getRootElement();
			
			//skip file if it is an aggregate descriptor
			Element primitive = root.getChild("primitive", ns);
			if(primitive != null && !Boolean.parseBoolean(primitive.getText())) {
				continue;
			}

			Element analysisEngineMetaData = root.getChild("analysisEngineMetaData", ns);
			if(analysisEngineMetaData == null) {
				continue;
			}
			Element configurationParametersParent = analysisEngineMetaData.getChild("configurationParameters", ns);
			if (configurationParametersParent != null) {
				List<?> configurationParameters = configurationParametersParent.getChildren("configurationParameter", ns);
				for (Object configurationParameter : configurationParameters) {
					String parameterName = ((Element) configurationParameter).getChildText("name", ns);
					if (!validateParameterName(parameterName)) {
						badParameters.add("bad parameter name '" + parameterName + "' in " + file.getPath());
					}
				}
			}
			
			Element configurationParameterSettingsParent = analysisEngineMetaData.getChild("configurationParameterSettings", ns);
			if (configurationParameterSettingsParent != null) {
				List<?> nameValuePairs = configurationParameterSettingsParent.getChildren("nameValuePair", ns);
				for (Object nameValuePair : nameValuePairs) {
					String parameterName = ((Element) nameValuePair).getChildText("name", ns);
					if (!validateParameterName(parameterName)) {
						badParameterSettings.add("bad parameter setting '" + parameterName + "' in " + file.getPath());
					}
				}
			}
		}

		if (badParameters.size() > 0) {
			String message = String.format("%d descriptor parameters with bad names. ", badParameters.size());
			System.err.println(message);
			for (String badParameter : badParameters) {
				System.err.println(badParameter);
			}
		}

		if (badParameters.size() > 0) {
			String message = String.format("%d descriptor parameter settings with bad names. ", badParameterSettings.size());
			System.err.println(message);
			for (String badParameterSetting: badParameterSettings) {
				System.err.println(badParameterSetting);
			}
			
		}

		if(badParameters.size() > 0 || badParameterSettings.size() > 0) {
			Assert.fail("bad parameter names found in descriptors");
		}
			
	}


	private boolean validateParameterName(String parameterName) {
		if (parameterName.indexOf('.') == -1) {
			return false;
		}
		try {
			String className = parameterName.substring(0, parameterName.lastIndexOf('.'));
			String fieldName = parameterName.substring(parameterName.lastIndexOf('.') + 1);
			Class<?> cls = Class.forName(className);
			cls.getField(fieldName);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
}
