package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.hsprocessor.ImageFeatureExtractor;
import hatespeech.detection.ml.WekaImageClassifier;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.IImagePosting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.BinaryPatternsPyramid;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.OpponentHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.imageanalysis.features.global.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SimpleCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SpatialPyramidCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.JointHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPFCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPJCD;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.RandomForest;

public class ImageClassifierFacebook {

	public static void main(String[] args) throws IOException {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();

		List<IImagePosting> trainingSamples = new ArrayList<>();

		//daoFB.getClassifiedImages().stream()
		//.forEach(trainingSamples::add);

		List<FBComment> classifiedFBCommentsForTrendanalysis1training = daoFB.getClassifiedImagesForTrendanalysis3();
		classifiedFBCommentsForTrendanalysis1training.stream().forEach( c -> {
			if(c.getResult() == 10){
				c.setResult(0);
			}
			else if(c.getResult() != 10){
				c.setResult(1);
			}
			else {
				throw new IllegalStateException("not 30, 31, 32, 33");
			}
			
			trainingSamples.add(c);
		});
		
		System.out.println("training samples size: " + trainingSamples.size());
		
		WekaImageClassifier classifier = new WekaImageClassifier(trainingSamples, new RandomForest());
		
		classifier.setUseSurfFeatureVector(true); //true
		
		classifier.setUseGlobalFeatureVectors(true);
		
		classifier.setGlobalFeaturesList(Arrays.asList(new ColorLayout()));
		//best RF
		classifier.setGlobalFeaturesList(Arrays.asList(new SPFCTH(), new JCD()));
		
//		classifier.setGlobalFeaturesList(Arrays.asList(new AutoColorCorrelogram(), new BinaryPatternsPyramid(), new CEDD(), new ColorLayout(), new EdgeHistogram(), new FCTH(),
//				new JCD(), new JointHistogram(), new OpponentHistogram(), new PHOG(), new ScalableColor(), new SimpleCentrist(), new SPACC(), new SpatialPyramidCentrist(), 
//				new SPCEDD(), new SPFCTH(), new SPJCD()));
		
		//new FuzzyColorHistogram(), new FuzzyOpponentHistogram(), new Gabor(), new LocalBinaryPatterns(), new LocalBinaryPatternsAndOpponent(), new JpegCoefficientHistogram(),
		//new LuminanceLayout(), new RankAndOpponent(), new RotationInvariantLocalBinaryPatterns(), new SimpleColorHistogram(), new SPLBP(), new Tamura()
		
		classifier.setUseFBCommentCount(false);
		classifier.setUseFBFractionOfUserReactionOnTotalReactions(false);
		classifier.setUseFBLikeCount(false);
		classifier.setUseFBPostReactionType(false);
		
		classifier.setUseDeepConvolutionalNeuralNetworkCaffeNet(false);
		classifier.setUseDeepConvolutionalNeuralNetworkGoogleNet(true); // true
		classifier.setUseDeepConvolutionalNeuralNetworkResNet(false);
		
		classifier.evaluate();
		classifier.learn();
		classifier.saveInstancesToArff();
		
		/*Path path = Paths.get("classified_images_output.txt");
		
		List<FBComment> classifiedFBCommentsForTrendanalysis1 = daoFB.getClassifiedImagesForTrendanalysis1();
		long negativeCorrect1 = classifiedFBCommentsForTrendanalysis1.stream()
				.filter(c -> c.getResult() == 30)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 0.0)
				.count();
		
		long positiveCorrect1 = classifiedFBCommentsForTrendanalysis1.stream()
				.filter(c -> c.getResult() != 30)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 1.0)
				.count();
		
		System.out.println("negativeCorrect1: " + negativeCorrect1);
		System.out.println("positiveCorrect1: " + positiveCorrect1);
		System.out.println("result1: " + ((double)(negativeCorrect1 + positiveCorrect1)) / classifiedFBCommentsForTrendanalysis1.size());
		
		List<FBComment> classifiedFBCommentsForTrendanalysis2 = daoFB.getClassifiedImagesForTrendanalysis2();
		long negativeCorrect2 = classifiedFBCommentsForTrendanalysis2.stream()
				.filter(c -> c.getResult() == 20)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 0.0)
				.count();
		
		long positiveCorrect2 = classifiedFBCommentsForTrendanalysis2.stream()
				.filter(c -> c.getResult() != 20)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 1.0)
				.count();
		
		System.out.println("negativeCorrect2: " + negativeCorrect2);
		System.out.println("positiveCorrect2: " + positiveCorrect2);
		System.out.println("result2: " + ((double)(negativeCorrect2 + positiveCorrect2)) / classifiedFBCommentsForTrendanalysis2.size());
		
		List<FBComment> classifiedFBCommentsForTrendanalysis3 = daoFB.getClassifiedImagesForTrendanalysis3();
		long negativeCorrect3 = classifiedFBCommentsForTrendanalysis3.stream()
				.filter(c -> c.getResult() == 10)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 0.0)
				.count();
		
		long positiveCorrect3 = classifiedFBCommentsForTrendanalysis3.stream()
				.filter(c -> c.getResult() != 10)
				.map(c -> classifier.classify(c))
				.filter(c -> c.doubleValue() == 1.0)
				.count();
		
		System.out.println("negativeCorrect3: " + negativeCorrect3);
		System.out.println("positiveCorrect3: " + positiveCorrect3);
		System.out.println("result3: " + ((double)(negativeCorrect3 + positiveCorrect3)) / classifiedFBCommentsForTrendanalysis3.size());
		*/
		

//		BufferedWriter writer = Files.newBufferedWriter(path);

//		daoFB.getFBCommentsByResult(-5).stream()
//		.limit(3000)
//		.forEach(c -> {
//			Double classifyValue = classifier.classify(c);
//			System.out.println("/home/andreas/repos/hs-detection/hs-detection/images/images/" + c.getImage().substring(10));
//			System.out.println(classifyValue + "\n" + c.getMessage() + "\n\n\n");
//			try {
//				writer.write(classifyValue + "\n" + c.getMessage() + "\n\n");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		});
	}
}
