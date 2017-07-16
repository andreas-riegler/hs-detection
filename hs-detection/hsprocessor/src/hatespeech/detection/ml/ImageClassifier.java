package hatespeech.detection.ml;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IPosting;

import org.apache.commons.io.FileUtils;

import com.codahale.metrics.ConsoleReporter.Builder;
import com.stromberglabs.jopensurf.Surf;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.AbstractLocalDocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.builders.LocalDocumentBuilder;
import net.semanticmetadata.lire.builders.SimpleDocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.classifiers.KMeans;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleFeature;
import net.semanticmetadata.lire.imageanalysis.features.local.surf.SurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.surf.SurfFeature;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import net.semanticmetadata.lire.indexers.parallel.ImagePreprocessor;

public class ImageClassifier {
	JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();
	private SimpleDocumentBuilder builder = new SimpleDocumentBuilder();

	public static void main(String[] args){
		ImageClassifier classifier = new ImageClassifier();
		List<FBComment> trainingSamples = new ArrayList<>();

		classifier.daoFB.getClassifiedImages().stream()
		.forEach(c -> trainingSamples.add(c));

		System.out.println(trainingSamples.size());

		//trainingSamples.forEach(x -> System.out.println(FileUtils.getFile(x.getAttachmentMediaImageSrc()).isFile()));

		ArrayList<File> trainingSamplesImageFiles = trainingSamples.stream()
				.map((x -> FileUtils.getFile(x.getAttachmentMediaImageSrc())))
				.collect(Collectors.toCollection(ArrayList::new));



		SurfExtractor se = new SurfExtractor();

		ConcurrentHashMap<String, List<? extends LocalFeature>> codebookHashMap = new ConcurrentHashMap<String, List<? extends LocalFeature>>(trainingSamples.size());

		AbstractLocalDocumentBuilder documentBuilder = new LocalDocumentBuilder();

		for(File f : trainingSamplesImageFiles){
			BufferedImage bi = null;

			try {
				bi = ImageIO.read(f);
			} catch (IOException e) {
				e.printStackTrace();
			}

			codebookHashMap.put(f.getAbsolutePath(), documentBuilder.extractLocalFeatures(bi, se).getFeatures());
		}
		
		Cluster[] codebook = codebookGenerator(codebookHashMap, 10);
		
		
	    LocalFeatureExtractor localFeatureExtractor = new SurfExtractor();
	    Aggregator aggregator = new BOVW();

	    BufferedImage image = null;
	    double[] featureVector;
	    List<? extends LocalFeature> listOfLocalFeatures;
	    for (File path : trainingSamplesImageFiles) {
	        try {
				image = ImageIO.read(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        localFeatureExtractor.extract(image);
	        listOfLocalFeatures = localFeatureExtractor.getFeatures();
	        aggregator.createVectorRepresentation(listOfLocalFeatures, codebook);
	        featureVector = aggregator.getVectorRepresentation();

	        System.out.println(path.getName() + " ~ " + Arrays.toString(featureVector));
	    }



	}

	private static void writeToArffFile(String data) {
		try {
			Files.write(Paths.get("images.arff"), data.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Cluster[] codebookGenerator(ConcurrentHashMap<String, List<? extends LocalFeature>> sampleMap, int numClusters) {
		KMeans k = new KMeans(numClusters);

		// fill the KMeans object:
		List<? extends LocalFeature> tempList;
		for (Map.Entry<String, List<? extends LocalFeature>> stringListEntry : sampleMap.entrySet()) {
			tempList = stringListEntry.getValue();
			for (LocalFeature aTempList : tempList) {
				k.addFeature(aTempList.getFeatureVector());
			}
		}

		System.out.println("Starting clustering");

		if (k.getFeatureCount() < numClusters) {
			// this cannot work. You need more data points than clusters.
			throw new UnsupportedOperationException("Only " + k.getFeatureCount() + " features found to cluster in " + numClusters + ". Try to use less clusters or more images.");
		}

		// do the clustering:
		System.out.println("Number of local features: " + k.getFeatureCount());
		System.out.println("Starting clustering ...");
		k.init();
		System.out.println("Step.");
		long start = System.currentTimeMillis();
		double lastStress = k.clusteringStep();

		System.out.println("Step 1 finished");

		System.out.println(convertTime(System.currentTimeMillis() - start) + " -> Next step.");
		start = System.currentTimeMillis();
		double newStress = k.clusteringStep();

		System.out.println("Step 2 finished");

		// critical part: Give the difference in between steps as a constraint for accuracy vs. runtime trade off.
		double threshold = Math.max(20d, (double) k.getFeatureCount() / 1000d);
		System.out.println("Threshold = " + threshold);
		int cStep = 3;

		while (Math.abs(newStress - lastStress) > threshold && cStep < 12) {
			System.out.println(convertTime(System.currentTimeMillis() - start) + " -> Next step. Stress difference ~ |" + (int) newStress + " - " + (int) lastStress + "| = " + Math.abs(newStress - lastStress));
			start = System.currentTimeMillis();
			lastStress = newStress;
			newStress = k.clusteringStep();

			System.out.println("Step " + cStep + " finished");

			cStep++;
		}

		return k.getClusters();
	}

	private static String convertTime(long time) {
		double h = time / 3600000.0;
		double m = (h - Math.floor(h)) * 60.0;
		double s = (m - Math.floor(m)) * 60;

		return String.format("%s%02d:%02d", (((int) h > 0) ? String.format("%02d:", (int) h) : ""), (int) m, (int) s);
	}

}
