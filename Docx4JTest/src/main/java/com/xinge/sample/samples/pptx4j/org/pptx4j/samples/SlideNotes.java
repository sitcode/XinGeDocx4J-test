/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

package com.xinge.sample.samples.pptx4j.org.pptx4j.samples;


import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;
import org.docx4j.XmlUtils;
import org.pptx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesMasterPart;
import org.docx4j.openpackaging.parts.PresentationML.NotesSlidePart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.relationships.Relationship;
import org.pptx4j.pml.CTNotesMasterIdList;
import org.pptx4j.pml.CTNotesMasterIdListEntry;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.NotesMaster;
import org.pptx4j.pml.Shape;



/**
 * @author jharrop
 *
 */
public class SlideNotes  {
	
	protected static Logger log = LoggerFactory.getLogger(SlideNotes.class);
		
	public static void main(String[] args) throws Exception {

		// Where will we save our new .ppxt?
		String outputfilepath = System.getProperty("user.dir") + "/sample-docs/OUT_SlideNotes.pptx";
		
		// Create skeletal package, including a MainPresentationPart and a SlideLayoutPart
		PresentationMLPackage presentationMLPackage = PresentationMLPackage.createPackage(); 
		
		// Need references to these parts to create a slide
		// Please note that these parts *already exist* - they are
		// created by createPackage() above.  See that method
		// for instruction on how to create and add a part.
		MainPresentationPart pp = (MainPresentationPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/presentation.xml"));		
		SlideLayoutPart layoutPart = (SlideLayoutPart)presentationMLPackage.getParts().getParts().get(
				new PartName("/ppt/slideLayouts/slideLayout1.xml"));
		
		// OK, now we can create a slide
		SlidePart slidePart = new SlidePart(new PartName("/ppt/slides/slide1.xml"));
		slidePart.setContents( SlidePart.createSld() );		
		pp.addSlide(0, slidePart);
		
		// Slide layout part
		slidePart.addTargetPart(layoutPart);
				
		// Create and add shape
		Shape sample = ((Shape)XmlUtils.unmarshalString(SAMPLE_SHAPE, Context.jcPML) );
		slidePart.getJaxbElement().getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame().add(sample);
		
		// Now add notes slide.
		// 1. Notes master
		NotesMasterPart nmp = new NotesMasterPart();
		NotesMaster notesmaster = (NotesMaster)XmlUtils.unmarshalString(notesMasterXml, Context.jcPML);
		nmp.setJaxbElement(notesmaster);
		// .. connect it to /ppt/presentation.xml
		Relationship ppRelNmp = pp.addTargetPart(nmp);
		/*
		 *  <p:notesMasterIdLst>
                <p:notesMasterId r:id="rId3"/>
            </p:notesMasterIdLst>
		 */
		pp.getJaxbElement().setNotesMasterIdLst(createNotesMasterIdListPlusEntry(ppRelNmp.getId()));
		
		// .. NotesMasterPart typically has a rel to a theme 
		// .. can we get away without it? 
		// Nope .. read this in from a file
		ThemePart themePart = new ThemePart(new PartName("/ppt/theme/theme2.xml"));
			// TODO: read it from a string instead
		themePart.unmarshal(
				FileUtils.openInputStream(new File(System.getProperty("user.dir") + "/theme2.xml"))
			);		
		nmp.addTargetPart(themePart);
		
		// 2. Notes slide
		NotesSlidePart nsp = new NotesSlidePart();
		Notes notes = (Notes)XmlUtils.unmarshalString(notesXML, Context.jcPML);
		nsp.setJaxbElement(notes);
		// .. connect it to the slide
		slidePart.addTargetPart(nsp);
		// .. it also has a rel to the slide
		nsp.addTargetPart(slidePart);
		// .. and the slide master
		nsp.addTargetPart(nmp);
		
		
		
		// All done: save it
		presentationMLPackage.save(new File(outputfilepath));

		System.out.println("\n\n done .. saved " + outputfilepath);
		
	}	
	
	private static CTNotesMasterIdList createNotesMasterIdListPlusEntry(String relId) {

		org.pptx4j.pml.ObjectFactory pmlObjectFactory = new org.pptx4j.pml.ObjectFactory();

		CTNotesMasterIdList notesmasteridlist = pmlObjectFactory.createCTNotesMasterIdList(); 
		    // Create object for notesMasterId
		    CTNotesMasterIdListEntry notesmasteridlistentry = pmlObjectFactory.createCTNotesMasterIdListEntry(); 
		    notesmasteridlist.setNotesMasterId(notesmasteridlistentry); 
		        notesmasteridlistentry.setId( relId); 

		return notesmasteridlist;
		}
	
	
	private static String SAMPLE_SHAPE = 			
		"<p:sp   xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
		+ "<p:nvSpPr>"
		+ "<p:cNvPr id=\"4\" name=\"Title 3\" />"
		+ "<p:cNvSpPr>"
			+ "<a:spLocks noGrp=\"1\" />"
		+ "</p:cNvSpPr>"
		+ "<p:nvPr>"
			+ "<p:ph type=\"title\" />"
		+ "</p:nvPr>"
	+ "</p:nvSpPr>"
	+ "<p:spPr />"
	+ "<p:txBody>"
		+ "<a:bodyPr />"
		+ "<a:lstStyle />"
		+ "<a:p>"
			+ "<a:r>"
				+ "<a:rPr lang=\"en-US\" smtClean=\"0\" />"
				+ "<a:t>Hello World</a:t>"
			+ "</a:r>"
			+ "<a:endParaRPr lang=\"en-US\" />"
		+ "</a:p>"
	+ "</p:txBody>"
+ "</p:sp>";
	
	private static String notesXML = "<p:notes xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
            + "<p:cSld>"
                + "<p:spTree>"
                    + "<p:nvGrpSpPr>"
                        + "<p:cNvPr id=\"1\" name=\"\"/>"
                        + "<p:cNvGrpSpPr/>"
                        + "<p:nvPr/>"
                    +"</p:nvGrpSpPr>"
                    + "<p:grpSpPr>"
                        + "<a:xfrm>"
                            + "<a:off x=\"0\" y=\"0\"/>"
                            + "<a:ext cx=\"0\" cy=\"0\"/>"
                            + "<a:chOff x=\"0\" y=\"0\"/>"
                            + "<a:chExt cx=\"0\" cy=\"0\"/>"
                        +"</a:xfrm>"
                    +"</p:grpSpPr>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"2\" name=\"Slide Image Placeholder 1\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noChangeAspect=\"1\" noGrp=\"1\" noRot=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph type=\"sldImg\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr/>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"3\" name=\"Notes Placeholder 2\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"1\" type=\"body\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr/>"
                        + "<p:txBody>"
                            + "<a:bodyPr/>"
                            + "<a:lstStyle/>"
                            + "<a:p>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-AU\" smtClean=\"0\"/>"
                                    + "<a:t>My first note.</a:t>"
                                +"</a:r>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"4\" name=\"Slide Number Placeholder 3\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"10\" sz=\"quarter\" type=\"sldNum\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr/>"
                        + "<p:txBody>"
                            + "<a:bodyPr/>"
                            + "<a:lstStyle/>"
                            + "<a:p>"
                                + "<a:fld id=\"{4E862D5C-1B55-4909-8988-3A2E4E74C6F7}\" type=\"slidenum\">"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>1</a:t>"
                                +"</a:fld>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                +"</p:spTree>"
                + "<p:extLst>"
                    + "<p:ext uri=\"{BB962C8B-B14F-4D97-AF65-F5344CB8AC3E}\">"
                        + "<p14:creationId val=\"2690966451\"/>"
                    +"</p:ext>"
                +"</p:extLst>"
            +"</p:cSld>"
            + "<p:clrMapOvr>"
                + "<a:masterClrMapping/>"
            +"</p:clrMapOvr>"
        +"</p:notes>";
	
	private static String notesMasterXml = "<p:notesMaster xmlns:p14=\"http://schemas.microsoft.com/office/powerpoint/2010/main\" xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
            + "<p:cSld>"
                + "<p:bg>"
                    + "<p:bgRef idx=\"1001\">"
                        + "<a:schemeClr val=\"bg1\"/>"
                    +"</p:bgRef>"
                +"</p:bg>"
                + "<p:spTree>"
                    + "<p:nvGrpSpPr>"
                        + "<p:cNvPr id=\"1\" name=\"\"/>"
                        + "<p:cNvGrpSpPr/>"
                        + "<p:nvPr/>"
                    +"</p:nvGrpSpPr>"
                    + "<p:grpSpPr>"
                        + "<a:xfrm>"
                            + "<a:off x=\"0\" y=\"0\"/>"
                            + "<a:ext cx=\"0\" cy=\"0\"/>"
                            + "<a:chOff x=\"0\" y=\"0\"/>"
                            + "<a:chExt cx=\"0\" cy=\"0\"/>"
                        +"</a:xfrm>"
                    +"</p:grpSpPr>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"2\" name=\"Header Placeholder 1\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph sz=\"quarter\" type=\"hdr\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"0\" y=\"0\"/>"
                                + "<a:ext cx=\"2971800\" cy=\"457200\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle>"
                                + "<a:lvl1pPr algn=\"l\">"
                                    + "<a:defRPr sz=\"1200\"/>"
                                +"</a:lvl1pPr>"
                            +"</a:lstStyle>"
                            + "<a:p>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"3\" name=\"Date Placeholder 2\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"1\" type=\"dt\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"3884613\" y=\"0\"/>"
                                + "<a:ext cx=\"2971800\" cy=\"457200\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle>"
                                + "<a:lvl1pPr algn=\"r\">"
                                    + "<a:defRPr sz=\"1200\"/>"
                                +"</a:lvl1pPr>"
                            +"</a:lstStyle>"
                            + "<a:p>"
                                + "<a:fld id=\"{F5F08C1F-E91F-43BA-A849-5EBBE62C914B}\" type=\"datetimeFigureOut\">"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>12/8/2013</a:t>"
                                +"</a:fld>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"4\" name=\"Slide Image Placeholder 3\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noChangeAspect=\"1\" noGrp=\"1\" noRot=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"2\" type=\"sldImg\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"1143000\" y=\"685800\"/>"
                                + "<a:ext cx=\"4572000\" cy=\"3429000\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                            + "<a:noFill/>"
                            + "<a:ln w=\"12700\">"
                                + "<a:solidFill>"
                                    + "<a:prstClr val=\"black\"/>"
                                +"</a:solidFill>"
                            +"</a:ln>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr anchor=\"ctr\" bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle/>"
                            + "<a:p>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"5\" name=\"Notes Placeholder 4\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"3\" sz=\"quarter\" type=\"body\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"685800\" y=\"4343400\"/>"
                                + "<a:ext cx=\"5486400\" cy=\"4114800\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle/>"
                            + "<a:p>"
                                + "<a:pPr lvl=\"0\"/>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>Click to edit Master text styles</a:t>"
                                +"</a:r>"
                            +"</a:p>"
                            + "<a:p>"
                                + "<a:pPr lvl=\"1\"/>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>Second level</a:t>"
                                +"</a:r>"
                            +"</a:p>"
                            + "<a:p>"
                                + "<a:pPr lvl=\"2\"/>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>Third level</a:t>"
                                +"</a:r>"
                            +"</a:p>"
                            + "<a:p>"
                                + "<a:pPr lvl=\"3\"/>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>Fourth level</a:t>"
                                +"</a:r>"
                            +"</a:p>"
                            + "<a:p>"
                                + "<a:pPr lvl=\"4\"/>"
                                + "<a:r>"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>Fifth level</a:t>"
                                +"</a:r>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"6\" name=\"Footer Placeholder 5\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"4\" sz=\"quarter\" type=\"ftr\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"0\" y=\"8685213\"/>"
                                + "<a:ext cx=\"2971800\" cy=\"457200\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr anchor=\"b\" bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle>"
                                + "<a:lvl1pPr algn=\"l\">"
                                    + "<a:defRPr sz=\"1200\"/>"
                                +"</a:lvl1pPr>"
                            +"</a:lstStyle>"
                            + "<a:p>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                    + "<p:sp>"
                        + "<p:nvSpPr>"
                            + "<p:cNvPr id=\"7\" name=\"Slide Number Placeholder 6\"/>"
                            + "<p:cNvSpPr>"
                                + "<a:spLocks noGrp=\"1\"/>"
                            +"</p:cNvSpPr>"
                            + "<p:nvPr>"
                                + "<p:ph idx=\"5\" sz=\"quarter\" type=\"sldNum\"/>"
                            +"</p:nvPr>"
                        +"</p:nvSpPr>"
                        + "<p:spPr>"
                            + "<a:xfrm>"
                                + "<a:off x=\"3884613\" y=\"8685213\"/>"
                                + "<a:ext cx=\"2971800\" cy=\"457200\"/>"
                            +"</a:xfrm>"
                            + "<a:prstGeom prst=\"rect\">"
                                + "<a:avLst/>"
                            +"</a:prstGeom>"
                        +"</p:spPr>"
                        + "<p:txBody>"
                            + "<a:bodyPr anchor=\"b\" bIns=\"45720\" lIns=\"91440\" rIns=\"91440\" rtlCol=\"0\" tIns=\"45720\" vert=\"horz\"/>"
                            + "<a:lstStyle>"
                                + "<a:lvl1pPr algn=\"r\">"
                                    + "<a:defRPr sz=\"1200\"/>"
                                +"</a:lvl1pPr>"
                            +"</a:lstStyle>"
                            + "<a:p>"
                                + "<a:fld id=\"{4E862D5C-1B55-4909-8988-3A2E4E74C6F7}\" type=\"slidenum\">"
                                    + "<a:rPr lang=\"en-US\" smtClean=\"0\"/>"
                                    + "<a:t>‹#›</a:t>"
                                +"</a:fld>"
                                + "<a:endParaRPr lang=\"en-US\"/>"
                            +"</a:p>"
                        +"</p:txBody>"
                    +"</p:sp>"
                +"</p:spTree>"
                + "<p:extLst>"
                    + "<p:ext uri=\"{BB962C8B-B14F-4D97-AF65-F5344CB8AC3E}\">"
                        + "<p14:creationId val=\"2674124109\"/>"
                    +"</p:ext>"
                +"</p:extLst>"
            +"</p:cSld>"
            + "<p:clrMap accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" bg1=\"lt1\" bg2=\"lt2\" folHlink=\"folHlink\" hlink=\"hlink\" tx1=\"dk1\" tx2=\"dk2\"/>"
            + "<p:notesStyle>"
                + "<a:lvl1pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"0\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl1pPr>"
                + "<a:lvl2pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"457200\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl2pPr>"
                + "<a:lvl3pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"914400\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl3pPr>"
                + "<a:lvl4pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"1371600\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl4pPr>"
                + "<a:lvl5pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"1828800\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl5pPr>"
                + "<a:lvl6pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"2286000\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl6pPr>"
                + "<a:lvl7pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"2743200\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl7pPr>"
                + "<a:lvl8pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"3200400\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl8pPr>"
                + "<a:lvl9pPr algn=\"l\" defTabSz=\"914400\" eaLnBrk=\"1\" hangingPunct=\"1\" latinLnBrk=\"0\" marL=\"3657600\" rtl=\"0\">"
                    + "<a:defRPr kern=\"1200\" sz=\"1200\">"
                        + "<a:solidFill>"
                            + "<a:schemeClr val=\"tx1\"/>"
                        +"</a:solidFill>"
                        + "<a:latin typeface=\"+mn-lt\"/>"
                        + "<a:ea typeface=\"+mn-ea\"/>"
                        + "<a:cs typeface=\"+mn-cs\"/>"
                    +"</a:defRPr>"
                +"</a:lvl9pPr>"
            +"</p:notesStyle>"
        +"</p:notesMaster>";
	

	
}
