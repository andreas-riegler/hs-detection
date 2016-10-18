package hatespeech.detection.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.BinaryPatternsPyramid;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.FuzzyColorHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.FuzzyOpponentHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.Gabor;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.JpegCoefficientHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.LocalBinaryPatterns;
import net.semanticmetadata.lire.imageanalysis.features.global.LuminanceLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.OpponentHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.imageanalysis.features.global.RotationInvariantLocalBinaryPatterns;
import net.semanticmetadata.lire.imageanalysis.features.global.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.features.global.SimpleColorHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.Tamura;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SimpleCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SpatialPyramidCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.JointHistogram;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.LocalBinaryPatternsAndOpponent;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.RankAndOpponent;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPFCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPJCD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPLBP;
import weka.classifiers.functions.SMO;
import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.ml.WekaImageClassifier;
import hatespeech.detection.model.IImagePosting;

public class ImageClassifierFacebook {


	public static void main(String[] args) {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();

		List<IImagePosting> trainingSamples = new ArrayList<>();

		daoFB.getClassifiedImages().stream()
		.forEach(trainingSamples::add);

		WekaImageClassifier classifier = new WekaImageClassifier(trainingSamples, new SMO());
		
		classifier.setUseSurfFeatureVector(true);
		
		classifier.setUseGlobalFeatureVectors(true);
		classifier.setGlobalFeaturesList(Arrays.asList(new AutoColorCorrelogram(), new BinaryPatternsPyramid(), new CEDD(), new ColorLayout(), new EdgeHistogram(), new FCTH(),
				new JCD(), new JointHistogram(), new OpponentHistogram(), new PHOG(), new ScalableColor(), new SimpleCentrist(), new SPACC(), new SpatialPyramidCentrist(), 
				new SPCEDD(), new SPFCTH(), new SPJCD()));
		
		//new FuzzyColorHistogram(), new FuzzyOpponentHistogram(), new Gabor(), new LocalBinaryPatterns(), new LocalBinaryPatternsAndOpponent(), new JpegCoefficientHistogram(),
		//new LuminanceLayout(), new RankAndOpponent(), new RotationInvariantLocalBinaryPatterns(), new SimpleColorHistogram(), new SPLBP(), new Tamura()
		
		classifier.setUseFBCommentCount(true);
		classifier.setUseFBFractionOfUserReactionOnTotalReactions(true);
		classifier.setUseFBLikeCount(true);
		classifier.setUseFBPostReactionType(true);
		
		classifier.evaluate();
		classifier.learn();
		classifier.saveInstancesToArff();
	}
}
