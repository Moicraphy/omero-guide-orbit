import java.awt.Polygon
import java.awt.Shape
import java.awt.Color
import com.actelion.research.orbit.beans.RawDataFile
import com.actelion.research.orbit.beans.RawAnnotation
import com.actelion.research.orbit.imageAnalysis.components.ImageFrame
import com.actelion.research.orbit.imageAnalysis.components.OrbitImageAnalysis
import com.actelion.research.orbit.imageAnalysis.models.ImageAnnotation
import com.actelion.research.orbit.imageprovider.ImageProviderOmero
import com.actelion.research.orbit.imageprovider.OmeroConf
import com.actelion.research.orbit.imageAnalysis.models.PolygonExt
import com.actelion.research.orbit.imageAnalysis.models.ClassShape
import omero.gateway.Gateway
import omero.gateway.SecurityContext
import omero.model.RoiI
import omero.gateway.facility.ROIFacility
import omero.gateway.model.ROIData
import omero.gateway.model.ROIResult

// Example script to show how to load Orbit ROI annotations from OMERO
// and convert them to Polygons on the Image.

// Edit these parameters
String username = "trainer-1"
String password = "password"
String hostname = "wss://workshop.openmicroscopy.org/omero-ws"

// Use the currently opened image...
final OrbitImageAnalysis OIA = OrbitImageAnalysis.getInstance()
ImageFrame iFrame = OIA.getIFrame()
println("selected image: " + iFrame)
RawDataFile rdf = iFrame.rdf

// Get the OMERO Image ID
int omeroImageId = rdf.getRawDataFileId()
println("ID:" + omeroImageId)

// Login to create a new connection with OMERO
OmeroConf conf = new OmeroConf(hostname, 443, true)
conf.setUseWebSockets(true)
ImageProviderOmero imageProvider = new ImageProviderOmero(conf)
imageProvider.authenticateUser(username, password)
Gateway gateway = imageProvider.getGatewayAndCtx().getGateway()
SecurityContext ctx = imageProvider.getGatewayAndCtx().getCtx()

// Load all annotations on the OMERO Image
List<ROIResult> roiresults = gateway.getFacility(ROIFacility).loadROIs(ctx, omeroImageId)
int Count = gateway.getFacility(ROIFacility).getROICount(ctx, omeroImageId)
println(omeroImageId+" Found: " +Count+ " Rois ")

// Convert "[Point2D.Double[x, y], Point2D.Double[x, y]...]" format to "[x,y;x,y...]" for OrbitImageAnalysis polygone and extract name
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
  // Print informations
println("Roi: " +label)
println("Found points: " +points)

//Create Polygon in OrbitImageAnalysis
PolygonExt polygon = new PolygonExt()
for (i=0; i<points.size(); i++) {
// Convert "[x,y]" to "a=x" and "b=y"
	point = points[i].split(',')
	 int a = Integer.parseInt(point[0])
	 int b = Integer.parseInt(point[1])
polygon.addPoint(a, b)
//println("Point: " +points[i])
}
polygon.setClosed(true)

// Add polygon to an Annotation on the Image
ClassShape cs = new ClassShape("Annotation Shape",Color.yellow,ClassShape.UNDEFINED);
cs.getShapeList().add(polygon);
ImageAnnotation annotation = new ImageAnnotation("Annotation",cs);
annotation.setRawDataFileId(omeroImageId)//image id
annotation.setDescription(label)//label
annotation.setUserId(USERNAME)
annotation.setModifyDate(new Date())

// store in DB
imageProvider.InsertRawAnnotation(annotation);
}
println("Close...")
imageProvider.close()
