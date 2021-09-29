import com.actelion.research.orbit.imageAnalysis.models.ImageAnnotation
import com.actelion.research.orbit.imageAnalysis.models.PolygonExt
import com.actelion.research.orbit.imageAnalysis.models.ClassShape

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
import omero.model.RectangleI
import omero.gateway.model.ROIResult

// Edit these parameters
String USERNAME = "USERNAME"
String PASSWORD = "PASSWORD"




//if(j<351 || j>390 && j!=604 && j!=605 && j!=606 || j==355 || j==360 || j==361) {
//print("Skip :"+j);
//continue;
//}
//else{
// Get the OMERO Image ID
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

ClassShape cs = new ClassShape("Annotation Shape",Color.yellow,ClassShape.UNDEFINED);
cs.getShapeList().add(polygon);
ImageAnnotation annotation = new ImageAnnotation("Annotation",cs);

//RawAnnotation rawAnnotation = new RawAnnotation()
annotation.setRawDataFileId(omeroImageId)//image id
annotation.setDescription(label)
annotation.setUserId(USERNAME)
annotation.setModifyDate(new Date())

// store in DB
imageProvider.InsertRawAnnotation(annotation);


}
DALConfig.getImageProvider().close();
