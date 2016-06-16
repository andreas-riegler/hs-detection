package hatespeech.detection.main;

import hatespeech.detection.dao.JDBCFBCommentDAO;
import hatespeech.detection.dao.JDBCHSPostDAO;
import hatespeech.detection.ml.WekaBowClassifier;
import hatespeech.detection.model.FBComment;
import hatespeech.detection.model.HatePost;
import hatespeech.detection.model.PostType;
import hatespeech.detection.model.Posting;

import java.util.ArrayList;
import java.util.List;

import weka.classifiers.functions.SMO;

public class ClassifierFacebook {
	
	public static void main(String[] args) {
		JDBCFBCommentDAO daoFB= new JDBCFBCommentDAO();
		JDBCHSPostDAO daoHP= new JDBCHSPostDAO();

		List<Posting> trainingSamples = new ArrayList<Posting>();

		for(FBComment post: daoFB.getFBComments())
		{
			if(post.getResult()!=-1)
			{
				if(post.getResult()==0)
				{
					trainingSamples.add(new Posting(post.getMessage(), post.getTypedDependencies(), PostType.NEGATIVE));				
				}	
				else if(post.getResult() == 1 || post.getResult() == 2 || post.getResult() == 3)
				{
					trainingSamples.add(new Posting(post.getMessage(), post.getTypedDependencies(), PostType.POSITIVE));
				}
			}

		}

		for(HatePost hatePost: daoHP.getAllPosts())
		{
			if(hatePost.getResult() == 1 || hatePost.getResult() == 2 || hatePost.getResult() == 3){
				trainingSamples.add(new Posting(hatePost.getPost(), hatePost.getTypedDependencies(), PostType.POSITIVE));
			}
		}

		WekaBowClassifier classifier1 = new WekaBowClassifier(trainingSamples, new SMO());
		classifier1.evaluate();

		//WekaBowClassifier classifier2 = new WekaBowClassifier(trainingSamples, new SMO());
		//classifier2.setMessageExactMatch(false);
		//classifier2.evaluate();

		//classifier1.learn();

		//classifier1.findFalsePositives(5);
	}
}
