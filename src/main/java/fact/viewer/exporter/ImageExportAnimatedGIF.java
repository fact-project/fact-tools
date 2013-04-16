/**
 * 
 */
package fact.viewer.exporter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.Task;

/**
 * @author chris
 *
 */
public class ImageExportAnimatedGIF
	extends Task
{
	
	static Logger log = LoggerFactory.getLogger( ImageExportAnimatedGIF.class );
	
	CameraPixelMap map;
	File image;
	int start = 0;
	int end = 1;
	int stepping = 1;
	Double completed = 0.0d;
	
	public ImageExportAnimatedGIF( CameraPixelMap map, File image, int start, int end, int stepping ){
		this.map = map;
		this.image = image;
		this.start = start;
		this.end = end;
		this.stepping = stepping;
	}
	

	public BufferedImage exportToGIF() throws Exception {
		
		BufferedImage buf = new BufferedImage( map.getWidth(), map.getHeight() + 8, BufferedImage.TYPE_INT_ARGB );
		ImageWriter gifWriter =  (ImageWriter) ImageIO.getImageWritersBySuffix( "GIF" ).next();
		ImageWriteParam imageWriteParam = gifWriter.getDefaultWriteParam();
		ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(buf);

		IIOMetadata imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier, imageWriteParam);
		String metaFormatName = imageMetaData.getNativeMetadataFormatName();

		IIOMetadataNode root = (IIOMetadataNode) imageMetaData.getAsTree(metaFormatName);
		IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

		graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
		graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString( 20 ));
		
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "Setting delay to 20" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		log.info( "#" );
		graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

		IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
		commentsNode.setAttribute("CommentExtension", "Created by MAH");

		IIOMetadataNode appEntensionsNode = getNode(root, "ApplicationExtensions");
		IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

		child.setAttribute("applicationID", "NETSCAPE");
		child.setAttribute("authenticationCode", "2.0");

		int loop = 0;
		child.setUserObject(new byte[]{ 0x1, (byte) (loop & 0xFF), (byte) ((loop >> 8) & 0xFF)});
		appEntensionsNode.appendChild(child);

		imageMetaData.setFromTree(metaFormatName, root);

		ImageOutputStream ios = ImageIO.createImageOutputStream( new FileOutputStream( image ) );
		gifWriter.setOutput(ios);

		Graphics2D g = buf.createGraphics();
		gifWriter.prepareWriteSequence(null);

		//int slice = this.currentSlice;
		
		Double total = new Double( end );
		
		for (int i = start; i < end; i += stepping ) {
			// Draw into the BufferedImage, and then do
			this.completed = 100.0 * ( (new Double(i)) / total.doubleValue() );
			this.advanced();
			
			log.debug( "Drawing slice {}", i );
			map.drawImage( g, buf.getWidth(), buf.getHeight(), i );
			gifWriter.writeToSequence(new IIOImage(buf, null,imageMetaData),imageWriteParam);
		}
		gifWriter.endWriteSequence();
		ios.flush();
		ios.close();
		//selectSlice( slice );
		
		return buf;
	}
	
	
	protected static IIOMetadataNode getNode(IIOMetadataNode rootNode,String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)
					== 0) {
				return((IIOMetadataNode) rootNode.item(i));
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return(node);
	}


	/**
	 * @see fact.viewer.ui.Task#percentageCompleted()
	 */
	@Override
	public double percentageCompleted() {
		return completed;
	}


	/**
	 * @see fact.viewer.ui.Task#getStatus()
	 */
	@Override
	public String getStatus() {
		return "Exporting PixelMap to animated GIF...";
	}


	/**
	 * @see fact.viewer.ui.Task#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
