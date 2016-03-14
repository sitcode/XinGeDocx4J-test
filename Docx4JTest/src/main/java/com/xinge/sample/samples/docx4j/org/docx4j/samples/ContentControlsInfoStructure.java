/*
 *  Copyright 2012, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */

package com.xinge.sample.samples.docx4j.org.docx4j.samples;


import java.util.HashMap;
import java.util.List;

import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.OpenDoPEHandler;
import org.docx4j.model.sdt.QueryString;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.opendope.ConditionsPart;
import org.docx4j.openpackaging.parts.opendope.XPathsPart;
import org.docx4j.samples.AbstractSample;
import org.docx4j.utils.SingleTraversalUtilVisitorCallback;
import org.docx4j.utils.TraversalUtilVisitor;
import org.docx4j.wml.CTDataBinding;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Tag;
import org.opendope.conditions.Condition;
import org.opendope.conditions.Xpathref;


/**
 * Show the content controls in the main document part
 * indenting as nested.  
 * 
 * This is helpful for understanding complex documents.
 * 
 * If there are OpenDoPE tags, display info about these.
 * 
 * @author jharrop
 *
 */
public class ContentControlsInfoStructure extends AbstractSample
{
	
	private static org.opendope.conditions.Conditions conditions;
	private static org.opendope.xpaths.Xpaths xPaths;
	
	public static void main(String[] args) throws Exception {
		

		
		try {
			getInputFilePath(args);
		} catch (IllegalArgumentException e) {
			inputfilepath = System.getProperty("user.dir") + "/sample-docs/word/databinding/invoice.docx";

		}
		
			
		WordprocessingMLPackage wordMLPackage  = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		if (wordMLPackage.getMainDocumentPart().getXPathsPart() == null) {
			throw new Docx4JException("OpenDoPE XPaths part missing");
		} else {
			xPaths = wordMLPackage.getMainDocumentPart().getXPathsPart()
					.getJaxbElement();
//			System.out.println(XmlUtils.marshaltoString(xPaths, true, true));
		}
		if (wordMLPackage.getMainDocumentPart().getConditionsPart() != null) {
			conditions = wordMLPackage.getMainDocumentPart()
					.getConditionsPart().getJaxbElement();
//			System.out.println(XmlUtils.marshaltoString(conditions, true, true));
		}
		
		
		TraversalUtilContentControlVisitor visitor = new TraversalUtilContentControlVisitor();		
		IndentingVisitorCallback contentControlCallback = new IndentingVisitorCallback(visitor);
		
		visitor.callback = contentControlCallback; 
		
		contentControlCallback.walkJAXBElements(
				wordMLPackage.getMainDocumentPart().getJaxbElement() );
		
	}
	
	
	public static class TraversalUtilContentControlVisitor extends TraversalUtilVisitor<SdtElement> {
		
		IndentingVisitorCallback callback; // so we can get indentation
		
		@Override
		public void apply(SdtElement element, Object parent, List<Object> siblings) {
			
			StringBuffer sb = new StringBuffer(); 

			sb.append("\n"+"\n");
			
			SdtPr sdtPr = element.getSdtPr();
			if (sdtPr==null) {
				sb.append("\n"+callback.indent + element.getClass().getSimpleName() + "  [no sdtPr!]" + " (having parent " + parent.getClass().getSimpleName() + ")");
				System.out.println(sb.toString());
			} else {				
				sb.append("\n"+callback.indent + element.getClass().getSimpleName()  + " (having parent " + parent.getClass().getSimpleName() + ")");
				
				CTDataBinding binding = (CTDataBinding) XmlUtils.unwrap(sdtPr
						.getDataBinding());
				if (binding!=null) {
					sb.append("\n"+callback.indent + "  binding: " + binding.getXpath() );					
				}
				
				Tag tag = sdtPr.getTag();
				if (tag == null) {
					//System.out.println(sb.toString());					
					return;
				}
				
				sb.append("\n"+callback.indent + "  " + tag.getVal() );					
				
				HashMap<String, String> map = QueryString.parseQueryString(
						tag.getVal(), true);
				
				String conditionId = map.get(OpenDoPEHandler.BINDING_ROLE_CONDITIONAL);
				String repeatId = map.get(OpenDoPEHandler.BINDING_ROLE_REPEAT);
				String xp = map.get(OpenDoPEHandler.BINDING_ROLE_XPATH);
				
				if (conditionId!=null ) {
					Condition c = ConditionsPart.getConditionById(conditions,
							conditionId);
					if (c == null) {
						sb.append("\n"+callback.indent + "  " + "Missing condition " + conditionId);
					}
					
					if (c.getParticle() instanceof Xpathref) {
						Xpathref xpathRef = (Xpathref)c.getParticle();
						if (xpathRef == null) {
							sb.append("\n"+callback.indent + "  " + "Condition " + c.getId() + " references a missing xpath!");
						}
						
						org.opendope.xpaths.Xpaths.Xpath xpath = XPathsPart.getXPathById(xPaths, xpathRef.getId());
						if (xpath==null) {
							sb.append("\n"+callback.indent + "  " + "XPath specified in condition '" + c.getId() + "' is missing!");
						} else {
							sb.append("\n"+callback.indent + "  " +  xpath.getId() + ": " + xpath.getDataBinding().getXpath() );
						}
					} else {
						//sb.append("\n"+"Complex condition: " + XmlUtils.marshaltoString(c, true, true) );
					}
					System.out.println(sb.toString());					
					
				} else if (repeatId!=null ) {
					
					org.opendope.xpaths.Xpaths.Xpath xpath = XPathsPart.getXPathById(xPaths, repeatId);
					
					if (xpath==null) {
						sb.append("\n"+callback.indent + "  " + "XPath specified in repeat '" + repeatId + "' is missing!");
					} else {
						sb.append("\n"+callback.indent + "  " +  xpath.getId() + ": " + xpath.getDataBinding().getXpath() );
					}
					System.out.println(sb.toString());					
					
				} else if (xp!=null) {
					
					org.opendope.xpaths.Xpaths.Xpath xpath = XPathsPart.getXPathById(xPaths, xp);
					
					if (xpath==null) {
						sb.append("\n"+callback.indent + "  " + "XPath specified with id '" + xp + "' is missing!");
					} else {
						sb.append("\n"+callback.indent + "  " +  xpath.getId() + ": " + xpath.getDataBinding().getXpath() );
					}
					// System.out.println(sb.toString());					
					
				}
				
			}
			
			
	
		}
	
	}
	
	public static class IndentingVisitorCallback extends SingleTraversalUtilVisitorCallback {

		public IndentingVisitorCallback(TraversalUtilVisitor visitor) {
			super(visitor);			
		}
		
		String indent = "";
		
		@Override
		public void walkJAXBElements(Object parent) {
			
			List children = getChildren(parent);
			if (children != null) {
				String oldIndent = indent;
				indent += "  ";
				for (Object o : children) {
					// if its wrapped in javax.xml.bind.JAXBElement, get its
					// value; this is ok, provided the results of the Callback
					// won't be marshalled
					o = XmlUtils.unwrap(o);
					this.apply(o, parent, children);
					if (this.shouldTraverse(o)) {
						walkJAXBElements(o);
					}
				}
				indent = oldIndent;
			}
		}
		
	}
	
	
}
