import com.actelion.research.orbit.imageAnalysis.models.ImageAnnotation
import com.actelion.research.orbit.imageAnalysis.models.PolygonExt


import java.awt.Color
import java.awt.Point
import java.awt.Polygon
import java.awt.Shape
import java.util.ArrayList
import java.util.List
import com.actelion.research.orbit.beans.RawDataFile
import com.actelion.research.orbit.beans.RawAnnotation
import com.actelion.research.orbit.imageAnalysis.components.ImageFrame
import com.actelion.research.orbit.imageAnalysis.components.OrbitImageAnalysis
import com.actelion.research.orbit.imageAnalysis.models.ImageAnnotation
import com.actelion.research.orbit.imageAnalysis.models.IScaleableShape
import com.actelion.research.orbit.imageprovider.ImageProviderOmero
import com.actelion.research.orbit.imageprovider.OmeroConf
import ome.model.roi.Roi
import omero.gateway.Gateway
import omero.gateway.SecurityContext
import omero.model.ImageI
import omero.model.RoiI
import omero.model.PolygonI
import omero.gateway.Gateway
import omero.gateway.model.TagAnnotationData;
import omero.model.TagAnnotation;
import omero.gateway.facility.ROIFacility
import omero.gateway.model.ROIData

import static omero.rtypes.rstring
import static omero.rtypes.rint
import com.actelion.research.orbit.imageAnalysis.dal.DALConfig

import java.io.*;
import java.util.*;
//import omero.gateway.util.ROIComponent

//import omero.gateway.BlitzGateway

//import omero.scripts as scripts
//import omero.gateway.BlitzGateway
//import omero.rtypes.rlong
//, rint, rstring, robject, unwrap
import omero.model.RectangleI
//EllipseI, LineI, PolygonI, PolylineI, MaskI, LabelI, PointI
//import math
//, pi
import omero.gateway.model.ROIResult

// Edit these parameters
String USERNAME = "root"
String PASSWORD = "omero-root-password"

// Use the currently opened image...
//final OrbitImageAnalysis OIA = OrbitImageAnalysis.getInstance()
//ImageFrame iFrame = OIA.getIFrame()
//println("selected image: " + iFrame)
//RawDataFile rdf = iFrame.rdf

//Boucle



//if(j<351 || j>390 && j!=604 && j!=605 && j!=606 || j==355 || j==360 || j==361) {
//print("Skip :"+j);
//continue;
//}
//else{
// Get the OMERO Image ID
//int omeroImageId = rdf.getRawDataFileId()
int omeroImageId = 352
println("ID:" + omeroImageId)

// Login to create a new connection with OMERO
ImageProviderOmero imageProvider = new ImageProviderOmero()
imageProvider.authenticateUser(USERNAME, PASSWORD)
Gateway gateway = imageProvider.getGatewayAndCtx().getGateway()
SecurityContext ctx = imageProvider.getGatewayAndCtx().getCtx()

List<ROIResult> roiresults = gateway.getFacility(ROIFacility).loadROIs(ctx, omeroImageId)
int Count = gateway.getFacility(ROIFacility).getROICount(ctx, omeroImageId)
println("Found " +Count+ " Rois ")

ROIResult r = roiresults.iterator().next();
if (r == null) return;
Collection<ROIData> rois = r.getROIs();

List<Shape> list;
Iterator<RoiI> j = rois.iterator();
while (j.hasNext()) {
	
  roi = j.next();
  list = roi.getShapes() 
  label = list[0].getText()
 points = list[0].getPoints()

 
label = label.toString()
points = points.toString()
points = points.replace("Point2D.Double", "")
points = points.replace("], [", " ; ")
points = points.replace("[", "")
points = points.replace("]", "")
points = points.replace(" ", "")
points = points.replace(".0", "")
points = points.split(';')
  // Do something
  
  println("Found " +label)
  println("Found " +points)
  println("Found " +roi)

//Create Polygon in OrbitImageAnalysis
PolygonExt polygon = new PolygonExt()
for (i=0; i<points.size(); i++) {
	point = points[i].split(',')
	 int a = Integer.parseInt(point[0])
	 int b = Integer.parseInt(point[1])
polygon.addPoint(a, b)
//println("Point: " +points[i])

}
polygon.setClosed(true)

ImageAnnotation annotation = new ImageAnnotation("ROI",polygon,ImageAnnotation.SUBTYPE_NORMAL, Color.yellow)
// you might add further shapes like SUBTYPE_EXCLUSION to exclude parts inside a ROI or SUBTYPE_INCLUSION to include a part in an exclusion
// or just use SUBTYPE_NORMAL to add an informative annotation which does not influence the ROI at all

RawAnnotation rawAnnotation = new RawAnnotation()
rawAnnotation.setRawDataFileId(omeroImageId)//image id
rawAnnotation.setData(annotation.getData())
rawAnnotation.setDescription(label)
rawAnnotation.setUserId(USERNAME)
rawAnnotation.setModifyDate(new Date())

// store in DB
DALConfig.getImageProvider().InsertRawAnnotation(rawAnnotation);

// insert further annotations...

}
DALConfig.getImageProvider().close();
