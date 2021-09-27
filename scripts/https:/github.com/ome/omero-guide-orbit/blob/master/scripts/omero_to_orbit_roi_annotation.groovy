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

import omero.gateway.Gateway
import omero.gateway.SecurityContext
import omero.model.ImageI
import omero.model.RoiI
import omero.model.PolygonI

import omero.gateway.Gateway
import omero.gateway.model.TagAnnotationData;
import omero.model.TagAnnotation;
import omero.gateway.facility.ROIFacility

import static omero.rtypes.rstring
import static omero.rtypes.rint
import com.actelion.research.orbit.imageAnalysis.dal.DALConfig

// Edit these parameters
String USERNAME = "USERNAME "
String PASSWORD = "PASSWORD "

// Use the currently opened image...
//final OrbitImageAnalysis OIA = OrbitImageAnalysis.getInstance()
//ImageFrame iFrame = OIA.getIFrame()
//println("selected image: " + iFrame)
//RawDataFile rdf = iFrame.rdf

//For each image in range:
for (int j=351; j<=606; j++) {
// Get the OMERO Image ID
//int omeroImageId = rdf.getRawDataFileId()
int omeroImageId = j
println("ID:" + omeroImageId)

// Login to create a new connection with OMERO
ImageProviderOmero imageProvider = new ImageProviderOmero()
imageProvider.authenticateUser(USERNAME, PASSWORD)
Gateway gateway = imageProvider.getGatewayAndCtx().getGateway()
SecurityContext ctx = imageProvider.getGatewayAndCtx().getCtx()

// Load all rois on the Orbit:
List rois = imageProvider.LoadRawAnnotationsByRawDataFile(omeroImageId)
println("Found " + rois.size() + " files")

List AnnToSave = new ArrayList()
for (RawRois ann: rois) {
// Cast to ImageAnnotation, scale to 100 and get Points
ImageAnnotation ia = new ImageAnnotation(ann)
Polygon poly = ((IScaleableShape) ia.getFirstShape()).getScaledInstance(100d, new Point(0, 0))
String points = poly.listPoints()
println(points)

//Create Polygon in OrbitImageAnalysis
//PolygonExt polygon = new PolygonExt();
polygon = new PolygonI()
polygon.setPoints(rstring(points))
polygon.setTheT(rint(0))
polygon.setTheZ(rint(0))
polygon.setStrokeColor(rint(-65281))   // yellow
polygon.setTextValue(rstring(ia.description))
polygon.setClosed(true);


ImageAnnotation annotation = new ImageAnnotation("ROI",polygon,ImageAnnotation.SUBTYPE_ROI, Color.magenta);
// you might add further shapes like SUBTYPE_EXCLUSION to exclude parts inside a ROI or SUBTYPE_INCLUSION to include a part in an exclusion
// or just use SUBTYPE_NORMAL to add an informative annotation which does not influence the ROI at all

RawAnnotation rawAnnotation = new RawAnnotation();
rawAnnotation.setData(annotation.getData());
rawAnnotation.setDescription(annotation.getDescription());
rawAnnotation.setUserId(USERNAME);
rawAnnotation.setModifyDate(new Date());

// store in DB
DALConfig.getImageProvider().InsertRawAnnotation(rawAnnotation);
}
//println(AnnToSave)
println("Close...")
}
imageProvider.close()
println("END")
