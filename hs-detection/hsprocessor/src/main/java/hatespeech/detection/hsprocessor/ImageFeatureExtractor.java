package hatespeech.detection.hsprocessor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.AbstractLocalDocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.builders.LocalDocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.classifiers.KMeans;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.surf.SurfExtractor;
import net.semanticmetadata.lire.utils.ImageUtils;
import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IImagePosting;

public class ImageFeatureExtractor {

	private static final int NUM_SURF_CLUSTERS = 350;
	private static final String CAFFE_OUTPUT_PATH_CAFFE_NET = "../caffe/output_images_caffenet/";
	private static final String CAFFE_OUTPUT_PATH_GOOGLE_NET = "../caffe/output_images_googlenet/";
	private static final String CAFFE_OUTPUT_PATH_RES_NET = "../caffe/output_images_resnet/";

	private static JDBCFBCommentDAO fbCommentDao = new JDBCFBCommentDAO();
	private static List<FBComment> fbComments = new ArrayList<>();
	private static ArrayList<File> fbCommentsImageFiles;
	private static ConcurrentHashMap<String, List<? extends LocalFeature>> codebookHashMap;
	private static LocalFeatureExtractor surfFeatureExtractor = new SurfExtractor();
	private static Aggregator surfAggregator = new BOVW();
	private static AbstractLocalDocumentBuilder documentBuilder = new LocalDocumentBuilder();
	private static Cluster[] codebook;

	public enum DeepConvolutionalNeuralNetworkModelType{
		CAFFE_NET,
		GOOGLE_NET,
		RES_NET
	}

	public enum DeepConvolutionalNeuralNetworkFeatureType{
		TOP_1,
		TOP_3,
		TOP_5,
		OVER_30_PERCENT
	}

	private ImageFeatureExtractor(){}

	static{
		init();
		buildSurfCodebook();
	}

	private static void init(){
		fbCommentDao.getClassifiedImages().stream()
		.forEach(fbComments::add);

		fbCommentsImageFiles = fbComments.stream()
				.map((x -> FileUtils.getFile(x.getAttachmentMediaImageSrc())))
				.collect(Collectors.toCollection(ArrayList::new));

		codebookHashMap = new ConcurrentHashMap<String, List<? extends LocalFeature>>(fbCommentsImageFiles.size());
	}

	private static void buildSurfCodebook(){
		for(File f : fbCommentsImageFiles){
			BufferedImage bi = null;

			try {
				bi = ImageIO.read(f);
			} catch (IOException e) {
				e.printStackTrace();
			}

			codebookHashMap.put(f.getAbsolutePath(), documentBuilder.extractLocalFeatures(bi, surfFeatureExtractor).getFeatures());
		}

		codebook = codebookGenerator(codebookHashMap, NUM_SURF_CLUSTERS);
	}

	public static int getSurfFeatureVectorCount(){
		return NUM_SURF_CLUSTERS;
	}

	public static Map<String, Double> getSurfFeatureVector(IImagePosting imagePosting){		
		BufferedImage image = null;
		double[] featureVector;
		List<? extends LocalFeature> listOfLocalFeatures;
		Map<String, Double> surfFeatureVectorMap = new HashMap<>();

		try {
			image = ImageIO.read(new FileInputStream(imagePosting.getImage()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		surfFeatureExtractor.extract(image);
		listOfLocalFeatures = surfFeatureExtractor.getFeatures();
		surfAggregator.createVectorRepresentation(listOfLocalFeatures, codebook);
		featureVector = surfAggregator.getVectorRepresentation();

		for(int i = 0; i < featureVector.length; i++){
			surfFeatureVectorMap.put("surfFV" + (i+1), featureVector[i]);
		}

		return surfFeatureVectorMap;
	}

	public static Map<String, Double> getGlobalFeatureVectors(IImagePosting imagePosting, List<GlobalFeature> globalFeatureList){
		GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();
		Map<String, Double> globalFeatureVectorMap = new HashMap<>();
		BufferedImage image = null;
		double[] featureVector;

		try {
			image = ImageUtils.createWorkingCopy(ImageIO.read(new FileInputStream(imagePosting.getImage())));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(GlobalFeature gf : globalFeatureList){
			GlobalFeature extractedFeature = globalDocumentBuilder.extractGlobalFeature(image, gf);
			featureVector = extractedFeature.getFeatureVector();

			System.out.println(extractedFeature.getFeatureName() + " : " + featureVector.length + " : " + extractedFeature.getClass().getSimpleName());

			for(int i = 0; i < featureVector.length; i++){
				globalFeatureVectorMap.put(extractedFeature.getFeatureName() + (i+1), featureVector[i]);
			}
		}

		return globalFeatureVectorMap;
	}

	public static String getDeepConvolutionalNeuralNetworkImageFeatures(String postingId, DeepConvolutionalNeuralNetworkModelType modelType, DeepConvolutionalNeuralNetworkFeatureType featureType){
		String outputImagePathString = null;
		String returnString = null;

		if(modelType == DeepConvolutionalNeuralNetworkModelType.CAFFE_NET){
			outputImagePathString = CAFFE_OUTPUT_PATH_CAFFE_NET;
		}
		else if(modelType == DeepConvolutionalNeuralNetworkModelType.GOOGLE_NET){
			outputImagePathString = CAFFE_OUTPUT_PATH_GOOGLE_NET;
		}
		else if(modelType == DeepConvolutionalNeuralNetworkModelType.RES_NET){
			outputImagePathString = CAFFE_OUTPUT_PATH_RES_NET;
		}

		outputImagePathString = outputImagePathString + postingId;
		Path filePath = Paths.get(outputImagePathString);

		if (Files.exists(filePath)){
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toString()), "UTF-8"));
				StringBuilder sb = new StringBuilder("");

				reader.readLine();
				if(featureType == DeepConvolutionalNeuralNetworkFeatureType.TOP_5){
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						String [] splitLine = line.split(" ");

						//sb.append(splitLine[2].replaceFirst("\"", "") + " " + splitLine[0] +";");
						sb.append(splitLine[2].replaceFirst("\"", "") + " ");	
					}
					if(sb.length() > 0){
						sb.deleteCharAt(sb.length()-1);
					}			
					returnString = sb.toString();
				}
				if(featureType == DeepConvolutionalNeuralNetworkFeatureType.TOP_3){
					int count = 0;
					for (String line = reader.readLine(); line != null && count <= 2; line = reader.readLine(), count++) {
						String [] splitLine = line.split(" ");

						//sb.append(splitLine[2].replaceFirst("\"", "") + " " + splitLine[0] +";");
						sb.append(splitLine[2].replaceFirst("\"", "") + " ");	
					}
					if(sb.length() > 0){
						sb.deleteCharAt(sb.length()-1);
					}			
					returnString = sb.toString();
				}
				else if(featureType == DeepConvolutionalNeuralNetworkFeatureType.TOP_1){
					String line = reader.readLine();
					String [] splitLine = line.split(" ");

					//sb.append(splitLine[2].replaceFirst("\"", "") + " " + splitLine[0] +";");
					sb.append(splitLine[2].replaceFirst("\"", ""));	
		
					returnString = sb.toString();
				}

				reader.close();
			} catch (Exception e) {
				System.out.println(filePath.toString());
				e.printStackTrace();
			}
		}
		else{
			throw new RuntimeException("image file not found");
		}

		System.out.println(returnString);
		return returnString;
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
