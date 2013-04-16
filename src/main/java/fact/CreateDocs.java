/**
 * 
 */
package fact;

import java.io.File;

import stream.doc.DocGenerator;
import stream.doc.DocTree;
import stream.util.URLUtilities;

/**
 * @author chris
 * 
 */
public class CreateDocs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			File outDir = new File("/tmp");

			String[] params = new String[] { "fact.io", "fact.data",
					"fact.image" };

			URLUtilities.copy(CreateDocs.class.getResource("/FACT-API.tex"),
					new File(outDir.getAbsolutePath() + File.separator
							+ "FACT-API.tex"));

			URLUtilities.copy(CreateDocs.class.getResource("/streams.pkg"),
					new File(outDir.getAbsolutePath() + File.separator
							+ "streams.pkg"));

			DocTree tree = DocTree.findDocs(DocGenerator.CLASSES, params);
			tree.print("  ");
			tree.generateDocs(new File("/tmp"));
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
